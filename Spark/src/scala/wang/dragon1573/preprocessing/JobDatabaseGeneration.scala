/*
 * Copyright (c) 2021 Dragon1573. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is “Incompatible With Secondary Licenses”,
 * as defined by the Mozilla Public License, v. 2.0.
 */

package wang.dragon1573.preprocessing

import org.apache.spark.SparkContext
import org.apache.spark.sql.functions.{col, expr, regexp_replace, udf}
import org.apache.spark.sql.{Row, SaveMode, SparkSession}

/**
 * <h1>构建职位数据库</h1>
 * <p>
 * 构建包含以下3张数据表：
 * <ul>
 * <li><code>tb_company_info</code></li>
 * <li><code>tb_job_info</code></li>
 * <li><code>tb_job_salary</code></li>
 * </ul>
 * </p>
 *
 * @author Dragon1573
 */
object JobDatabaseGeneration {
  def main(args: Array[String]): Unit = {
    // 创建 SparkSession
    val spark = SparkSession.builder().appName("Generating: jip4pg").enableHiveSupport()
                            .getOrCreate()

    // 读取数据集
    val scrapyRaw = spark.read.table("jip4pg.etb_scrapy_raw")

    /* 开始：构造 UDF */
    // 获取省市行政区划映射表
    val cityProvinceMap = spark.sparkContext.broadcast(
      spark.read.table("jip4pg.etb_all_city").select("name", "parentname").rdd.
           map{ case Row(city: String, province: String) => (city, province) }.
           toLocalIterator.toMap
      ).value

    // 地名规范化函数

    val cityGenerify = udf((rawLocation: String) => {
      // 按分隔符拆分行政区划
      val locationArray = rawLocation.replaceAll("，", "/").split("/")
      // 提取最小行政区划
      val location = (if (locationArray.nonEmpty) locationArray.last else "").replace("市", "")
      if (location.matches("[\\u4e00-\\u9fa5]+")) {
        // 合法地理信息
        if (cityProvinceMap.contains(location)) {
          // 地级行政区划
          s"${cityProvinceMap(location)}/$location"
        } else {
          // 省级行政区划
          s"$location/全区"
        }
      } else {
        "全国/全区"
      }
    })

    // 招聘模式标签化
    val workNatureTag = udf((rawType: String) => {
      if ("intern" == rawType) 0 else 1
    })

    // 薪资标准化
    val salaryGenerify = udf((rawSalary: String) => {
      // 正则提取3个部分
      val array = "(\\d+k?)(-\\d+k?)?/([天月])".r.findFirstMatchIn(rawSalary) match {
        case Some(x) => (x.group(1), x.group(2), x.group(3))
        case None => (null, null, null)
      }

      // 无法正则匹配
      if (array._1 == null) {
        "薪资面议"
      } else {
        // 使用下限填充空白上限
        val maxSalaryStr = if (array._2 != null) array._2.substring(1) else array._1

        // 单位转换
        var minSalaryFloat = if (array._1.matches("\\d+k")) {
          array._1.substring(0, array._1.length - 1).toFloat * 1000
        } else array._1.toFloat
        var maxSalaryFloat = if (maxSalaryStr.matches("\\d+k")) {
          maxSalaryStr.substring(0, maxSalaryStr.length - 1).toFloat * 1000
        } else maxSalaryStr.toFloat
        if (array._3 == "天") {
          minSalaryFloat *= 22
          maxSalaryFloat *= 22
        }

        "%.1fK-%.1fK/月".format(minSalaryFloat / 1000, maxSalaryFloat / 1000)
      }
    })
    /* 结束：构造 UDF */

    // 数据规范化
    val scrapyCleaned = scrapyRaw
      .withColumn("company_location", cityGenerify(col("company_location").cast("string"))).
      withColumn("job_position", cityGenerify(col("job_position").cast("string"))).
      withColumn("salary", salaryGenerify(col("job_money"))).
      withColumn("work_nature", workNatureTag(col("job_type")))

    /* 开始：拆表 */
    // 企业信息表
    val companyInfo = scrapyCleaned.select(
      col("company_name") as "ci_company_name", col("company_description") as "ci_company_info",
      col("company_category") as "ci_industry_type", col("company_location") as "ci_location"
      ).distinct().
                                   withColumn("ci_company_id",
                                              regexp_replace(expr("uuid()"), "-", ""))

    // 企业信息表
    val jobInfo = scrapyCleaned.join(companyInfo).where(
      """company_name = ci_company_name AND ci_company_info = company_description AND
        | company_category = ci_industry_type AND company_location = ci_location""".stripMargin
      ).selectExpr(
      "ci_company_id AS ji_company_id", "ci_company_name AS ji_company_name",
      "new_job_name AS ji_job_name",
      "'' AS ji_job_type", "job_detail AS ji_job_desc", "job_position AS ji_location",
      "work_nature AS ji_work_nature",
      "'实习僧' AS origin", "details_url AS origin_url"
      ).distinct().
                               withColumn("ji_job_id", regexp_replace(expr("uuid()"), "-", ""))

    // 薪资信息表
    val jobSalary = scrapyCleaned.join(jobInfo).where(
      """ji_job_desc = job_detail AND ji_job_name = new_job_name AND
        | ji_location = job_position AND ji_company_name = company_name""".stripMargin
      ).selectExpr(
      "ji_job_id AS js_job_id", "salary AS js_salary", "0 AS js_sup", "0 AS js_non_sup",
      "job_date AS js_create_dt"
      ).distinct().
                                 withColumn("js_salary_id", regexp_replace(expr("uuid()"), "-", ""))
    /* 结束：拆表 */

    /* 开始：导出到数据库 */
    companyInfo.selectExpr(
      "ci_company_id AS company_id", "ci_company_name AS company_name",
      "ci_company_info AS company_info",
      "ci_industry_type AS industry_type", "ci_location AS location"
      ).write.mode(SaveMode.Overwrite).saveAsTable("jip4pg.tb_company_info")
    Clustering.cluster(jobInfo.selectExpr(
      "ji_job_id AS job_id", "ji_company_id AS company_id", "ji_company_name AS company_name",
      "ji_job_name AS job_name", "ji_job_type AS job_type", "ji_job_desc AS job_desc",
      "ji_location AS location", "ji_work_nature AS work_nature", "origin", "origin_url"
      ), spark).write.mode(SaveMode.Overwrite).saveAsTable("jip4pg.tb_job_info")
    jobSalary.selectExpr(
      "js_salary_id AS salary_id", "js_job_id AS job_id", "js_salary AS salary",
      "js_sup AS sup", "js_non_sup AS non_sup", "js_create_dt AS create_dt"
      ).write.mode(SaveMode.Overwrite).saveAsTable("jip4pg.tb_job_salary")
    /* 结束：导出到数据库 */
  }
}

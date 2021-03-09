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

package wang.dragon1573.recommendation

import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{IndexToString, StringIndexer}
import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{SaveMode, SparkSession}

import wang.dragon1573.utils.loadProperties

/**
 * <h1>使用ALS完成岗位推荐</h1>
 *
 * @author Dragon1573
 */
object Recommendation {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("ALS: Job Recommendation").
                            enableHiveSupport().getOrCreate()

    // 加载配置文件
    val properties = loadProperties

    // 读入数据集
    val userClicks = spark.read.jdbc(
      properties.getProperty("url"), "tb_user_job_demand_count", properties
      ).persist()

    // 构建机器学习流水线
    val userIndexer = new StringIndexer().setInputCol("user_id").setOutputCol("user_index").
                                         fit(userClicks)
    val userIndexed = userIndexer.transform(userClicks)
    val keywordIndexer = new StringIndexer().setInputCol("keyword").setOutputCol("keyword_index").
                                            fit(userIndexed)
    val keywordIndexed = keywordIndexer.transform(userIndexed)
    val alsModel = new ALS().setColdStartStrategy("drop").setUserCol("user_index").
                            setItemCol("keyword_index").setRatingCol("freq").
                            setPredictionCol("prediction").fit(keywordIndexed)

    // 对 ALS 模型进行评估
    val predictedTable = alsModel.transform(keywordIndexed)
    val rmse = new RegressionEvaluator().setMetricName("rmse").setLabelCol("freq").
                                        setPredictionCol("prediction").evaluate(predictedTable)
    println(s"Regular Mean Square Error of ALS Model: $rmse")

    // 获取推荐给用户的关键字
    val keywordRecommendations = alsModel.recommendForAllUsers(1).select(
      col("user_index"),
      explode(col("recommendations.keyword_index")) as "keyword_index"
      ).persist()
    val userRetrieved = new IndexToString().setInputCol("user_index").setOutputCol("user_id").
                                           setLabels(userIndexer.labels).
                                           transform(keywordRecommendations)
    val keywordRetrieved = new IndexToString().setInputCol("keyword_index").
                                              setOutputCol("keyword").
                                              setLabels(keywordIndexer.labels).
                                              transform(userRetrieved).persist()
    println("Keyword Recommendations：")
    keywordRetrieved.show(false)
    userClicks.unpersist()

    // 笛卡尔积
    val jobInfo = spark.read.jdbc(properties.getProperty("url"), "tb_job_info", properties).
                       persist()
    val windowSpec = Window.partitionBy("user_id").orderBy(col("edit_distance").asc)
    val jobRecommendations =
      keywordRetrieved.crossJoin(jobInfo).
                      withColumn(
                        "edit_distance",
                        // 计算关键词与实际职位的编辑距离
                        levenshtein(col("keyword"), col("job_name"))
                        ).
                        // 按用户ID分组，编辑距离降序排列，为每组指定行号
                      withColumn("rank", row_number() over windowSpec).
                        // 提取每组前5行
                      where("rank <= 5").
                        // 匹配 MySQL 字段
                      select(
                        regexp_replace(expr("uuid()"), "-", "") as "recommend_id",
                        col("user_id"), col("company_name"), col("job_name"),
                        col("job_id"), lit(0) as "visited"
                        ).persist()
    println("职位推荐结果：")
    jobRecommendations.show(false)
    jobInfo.unpersist()
    keywordRetrieved.unpersist()

    // 为防止覆盖 MySQL 表结构，使用追加方式导出结果
    jobRecommendations.write.mode(SaveMode.Append).
                      jdbc(properties.getProperty("url"), "tb_job_recommend", properties)
  }
}

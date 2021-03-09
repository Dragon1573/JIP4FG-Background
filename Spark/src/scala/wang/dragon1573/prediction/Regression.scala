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

package wang.dragon1573.prediction

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.regression.RandomForestRegressor
import org.apache.spark.sql.functions.{col, regexp_extract, udf}
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.{SaveMode, SparkSession}

import wang.dragon1573.utils.loadProperties

/**
 * <h1>使用随机森林模型预测2021～2023年薪资水平</h1>
 *
 * @author Dragon1573
 */
object Regression {
  val number2String = udf((value: Double, range: Double) => {
    s"${(value - range).round}k-${(value + range).round}k"
  })

  def main(args: Array[String]): Unit = {
    // 初始化 SparkSession
    val spark = SparkSession.builder().appName("RandomForest: Salary Prediction").
                            enableHiveSupport().getOrCreate()

    // 加载并预处理数据集
    val timeLine = spark.read.table("jip4pg.etb_time_line").
                        withColumn(
                          "salary_low",
                          regexp_extract(col("salary"), "(\\d+)k-(\\d+)k", 1).cast(DoubleType)
                          ).
                        withColumn(
                          "salary_high",
                          regexp_extract(col("salary"), "(\\d+)k-(\\d+)k", 2).cast(DoubleType)
                          ).
                        withColumn("salary_val", (col("salary_low") + col("salary_high")) / 2).
                        withColumn("range", col("salary_val") - col("salary_low"))
    val Array(trainSet, testSet) = timeLine.where("year <= 2020").randomSplit(Array(0.9, 0.1))
    val unknownSet = timeLine.where("year > 2020")

    // 构造机器学习流水线
    val jobNameIndexer = new StringIndexer().setInputCol("job_name").setOutputCol("job_name_idx")
    val jobTypeIndexer = new StringIndexer().setInputCol("job_type").setOutputCol("job_type_idx")
    val assembler = new VectorAssembler().setInputCols(
      Array("job_name_idx", "job_type_idx", "year")
      ).setOutputCol("features")
    val salaryRF = new RandomForestRegressor().setFeaturesCol("features").setLabelCol("salary_val").
                                              setPredictionCol("salary_pred")
    val rangeRF = new RandomForestRegressor().setFeaturesCol("features").setLabelCol("range").
                                             setPredictionCol("range_pred")
    val pipeline = new Pipeline().setStages(
      Array(jobNameIndexer, jobTypeIndexer, assembler, salaryRF, rangeRF)
      )

    // 对训练集和测试集进行预测
    val model = pipeline.fit(trainSet)
    val trainSetPred = model.transform(trainSet)
    val testSetPred = model.transform(testSet)

    // 对模型进行评估
    val salaryEval = new RegressionEvaluator().setLabelCol("salary_val")
                                              .setPredictionCol("salary_pred").
                                              setMetricName("rmse")
    val trainSalaryRMSE = salaryEval.evaluate(trainSetPred)
    val testSalaryRMSE = salaryEval.evaluate(testSetPred)
    println(s"Root Mean Square Error of train data with salary is $trainSalaryRMSE")
    println(s"Root Mean Square Error of test data with salary is $testSalaryRMSE")
    val rangeEval = new RegressionEvaluator().setLabelCol("range").setPredictionCol("range_pred").
                                             setMetricName("rmse")
    val trainRangeRMSE = rangeEval.evaluate(trainSetPred)
    val testRangeRMSE = rangeEval.evaluate(testSetPred)
    println(s"Root Mean Square Error of train data with range is $trainRangeRMSE")
    println(s"Root Mean Square Error of test data with range is $testRangeRMSE")

    // 对未知集进行预测，并写入 MySQL
    val properties = loadProperties
    println("Prediction Results:")
    model.transform(unknownSet).
         withColumn("salary_str", number2String(col("salary_pred"), col("range_pred"))).
         selectExpr("job_type", "job_name", "year", "salary_str AS salary").
         write.mode(SaveMode.Append).
         jdbc(properties.getProperty("url"), "tb_job_salary_pred", properties)
  }
}

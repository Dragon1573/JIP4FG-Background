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
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.evaluation.ClusteringEvaluator
import org.apache.spark.ml.feature.Word2Vec
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import com.huaban.analysis.jieba.JiebaSegmenter

/**
 * <h1>对职位信息表进行聚类</h1>
 *
 * @author Dragon1573
 */
object Clustering {
  /** <h2>添加职位类型标签</h2> */
  def cluster(dataset: DataFrame, spark: SparkSession): DataFrame = {
    /* 开始：完成中文分词 */
    // 构建分布式分词器
    val jiebaSegmenter = new JiebaSegmenter()
    // 分词
    val wordsDataset = spark.createDataFrame(dataset.collect().map{
      case Row(
      job_id: String, company_id: String, company_name: String, job_name: String, job_type: String,
      job_desc: String, location: String, work_nature: Int, origin: String, originUrl: String
      ) => (
        job_id, company_id, company_name, job_name, job_type,
        job_desc, location, work_nature, origin, originUrl,
        jiebaSegmenter.sentenceProcess(job_name).toArray.map{ _.asInstanceOf[String] }
      )
    }).toDF(
      "job_id", "company_id", "company_name", "job_name", "job_type", "job_desc",
      "location", "work_nature", "origin", "origin_url", "jn_words"
      )
    /* 结束：完成中文分词 */

    /* 开始：构建机器学习流水线 */
    val word2Vec = new Word2Vec().setInputCol("jn_words").setOutputCol("features")
    val kMeans = new KMeans().setK(5).setFeaturesCol("features").setPredictionCol("job_type_tagged")
    /* 结束：构建机器学习流水线 */

    // 处理数据集
    val results = new Pipeline().setStages(Array(word2Vec, kMeans)).fit(wordsDataset)
                                .transform(wordsDataset)

    // KMeans 模型评估
    val score = new ClusteringEvaluator().setFeaturesCol("features")
                                         .setPredictionCol("job_type_tagged")
                                         .evaluate(results)
    println(s"Silhouette with Squared Euclidean Distance: $score")

    // 写出数据
    results.withColumn("job_type", col("job_type_tagged").cast(StringType)).selectExpr(
      "job_id", "company_id", "company_name", "job_name", "job_type",
      "job_desc", "location", "work_nature", "origin", "origin_url"
      )
  }
}

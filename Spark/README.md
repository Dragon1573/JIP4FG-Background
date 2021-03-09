# Spark 数据预处理与机器学习

## 前言

&emsp;&emsp; `Apache Spark` 是一款建立在 `Apache Hadoop` 生态圈之上的、内存迭代型的、用于大规模数据处理的通用分析引擎。

&emsp;&emsp; `Apache Spark` 借助 `DAG Scheduler` 有向无环图任务调度器、 `Query Optimizer` 查询优化器和 `Physical Execution Engine` 物理执行引擎，实现了批处理数据和流数据的高性能处理，内置超过 80 种高阶数据操作方法让开发者能够更加快速、便捷地开发并行应用程序，甚至在 `Java`/ `Scala` / `R` / `SQL` 环境中获得交互式开发体验 。其次， `Apache Spark` 依托 `Spark SQL` 、 `Spark Streaming` 、 `MLlib` 、 `GraphX` 四个上层组件，提供了结构化数据分析、流数据分析、机器学习和图计算的全面兼容。

&emsp;&emsp; `Apache Spark` 的运行环境是自由的，开发者可以在仅含 `Spark` 自身的独立集群中运行，也可以和 `Amazon EC2` / `Apache YARN` / `Apache Messos` 集群共存，甚至部署在 `Kubernates` 容器之中。 `Apache Spark` 原生兼容 `HDFS` / `Alluxio` / `Apache Cassandra` / `Apache HBase` / `Apache Hive` 等数据源，还能依托 `JDBC` 标准连接上百种通用数据源。

## 数据预处理－聚类

&emsp;&emsp; 在数据预处理阶段，我们需要将爬虫获得的原始数据集 `etb_scrapy_raw` 按 `MySQL` 侧要求拆分为 3 张数据表：企业信息表 `tb_company_info` 、职位信息表 `tb_job_info` 和薪资信息表 `tb_job_salary` ，还需要借助全国地级行政区划映射表 `etb_all_city` 实现上述 3 张数据表的地理信息标准化，方便后续使用 `ECharts` 进行数据可视化。

&emsp; 聚类分析是本项目数据预处理阶段的重点。由于数据来源限制，原始数据集不提供各职位的职位类型（技术岗、公关岗、管理岗等分类），因此只能使用聚类的方式为原始数据集添加近似的类别编号。原始数据集提供的职位名称是字符串形式的，我们首先选用了 [`JiebaSegmenter`](https://github.com/huaban/jieba-analysis/blob/master/src/main/java/com/huaban/analysis/jieba/JiebaSegmenter.java) 实现中文分词，然后用 [`Word2Vec`](http://spark.apache.org/docs/2.4.0/ml-features.html#word2vec) 实现词字符串数组到稠密特征向量的转化；随后，我们使用了 `Spark` 内置的 [`KMeans`](http://spark.apache.org/docs/2.4.0/ml-clustering.html#k-means) 进行聚类分析，并将所有职位聚合为 5 类。在学校部署的集群中运行约 4 分钟，平方欧氏距离剪影分析的评估结果约为 0.4356 。

## 机器学习－协同过滤

&emsp;&emsp; 在 `Spark` 机器学习阶段，我们需要从 `MySQL` 数据库中获取用户访问平台、搜索职位关键词的相关日志信息，并使用协同过滤算法为用户推荐可能感兴趣的职位。

&emsp;&emsp; 与淘宝、天猫、京东等大型电子商务网站的商品推荐系统不同，本项目前端平台的用户访问日志只能获得用户搜索各关键字的频次，因此协同过滤算法也只能为用户推荐可能感兴趣的搜索关键字，再根据关键字获取相关性最大的实际职位推荐给用户。

&emsp;&emsp; 在协同过滤方面，我们使用 [`StringIndexer`](http://spark.apache.org/docs/2.4.0/ml-features.html#stringindexer) 和 [`IndexToString`](http://spark.apache.org/docs/2.4.0/ml-features.html#indextostring) 完成字符串字段和整数索引值之间的双向转换；使用 [`ALS`](http://spark.apache.org/docs/2.4.0/ml-collaborative-filtering.html) 完成协同过滤计算，得到 1 个推荐关键词；使用 [`levenshtein`](http://spark.apache.org/docs/2.4.0/api/scala/index.html#org.apache.spark.sql.functions$@levenshtein(l:org.apache.spark.sql.Column,r:org.apache.spark.sql.Column):org.apache.spark.sql.Column) 计算关键词与全部职位名称的编辑距离，获得 5 个距离最小的职位写回 `MySQL` 并推荐给用户。在学校部署的集群中运行约 10 分钟，均方误差的评估结果约为 0.0179 。

## 机器学习－回归

&emsp;&emsp; 在 `Spark` 机器学习阶段，我们需要从 `Hive` 数据仓库中获取 9 个职位近 10 年的历史薪资数据，并以此为基础预测未来 3 年各职位的薪资情况。

&emsp;&emsp; 我们使用 [`StringIndexer`](http://spark.apache.org/docs/2.4.0/ml-features.html#stringindexer) 和 [`IndexToString`](http://spark.apache.org/docs/2.4.0/ml-features.html#indextostring) 完成字符串字段 `job_type` 和 `job_name` 的（反）数值化、借助 [`VectorAssembler`](http://spark.apache.org/docs/2.4.0/ml-features.html#vectorassembler) 构造特征向量，我们使用了 2 个随机森林回归器 [`RandomForestRegressor`](http://spark.apache.org/docs/2.4.0/ml-classification-regression.html#random-forest-regression) 分别实现薪资水平中位数回归预测和薪资水平浮动范围回归预测，并借助用户自定义函数构造标准格式的薪资范围。

&emsp;&emsp; 本阶段在学校部署的集群中运行约 3 分钟，均方误差评估结果如下：

|     训练集     | 均方误差 |     测试集     | 均方误差 |
| :------------: | :------: | :------------: | :------: |
| 薪资水平中位数 |  1.0265  | 薪资水平中位数 |  1.3603  |
|    浮动范围    |  0.7842  |    浮动范围    |  0.8990  |

## 使用方法

1. 确保您的 Apache Maven 版本为 v3.x 系列（推荐使用 v3.6.3 ）

2. 克隆本项目至本地

   ```shell
   # SSH 地址
   git clone git@github.com:Dragon1573/JIP4FG-Spark.git
   # HTTPS 地址
   git clone https://github.com/Dragon1573/JIP4FG-Spark.git
   ```
   
3. 本项目需要借助 JDBC 直接读写 MySQL 数据库，请确保 `mysql-connector-java.jar` 已添加到 Spark 类路径

4. 本项目使用 XML 配置数据库相关信息，请按照以下格式编写配置文件 `JIP4FG-Spark/src/scala/wang/dragon1573/jdbc.xml`

   ```xml
   <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">  
   <properties>
     <!-- 数据库的 JDBC URL -->
     <entry key="url"></entry>
     <!-- 登陆用户名 -->
     <entry key="user"></entry>
     <!-- 登录密码 -->
     <entry key="password"></entry>
   </properties>
   ```

5. 依次执行以下命令构建项目

   ```shell
   # 进入项目
   cd JIP4FG-Spark
   # 构建项目
   mvn clean package
   ```

6. 使用任意方法检查 `target/jip4fg-1.0.jar` ，确保其中除 `wang.dragon1573.*` 以外还打入了依赖项相关的类文件，构建产物的文件大小应在 5MB 以上

7. 将 `jip4fg-1.0.jar` 上传到 Spark 客户端节点

8. 使用如下命令提交 Spark Applications

   ```shell
   # 数据预处理和 KMeans 聚类
   spark-submit --class wang.dragon1573.preprocessing.JobDatabaseGeneration jip4fg-1.0.jar
   
   # 随机森林回归
   spark-submit --class wang.dragon1573.regression.prediction.Regression jip4fg-1.0.jar
   
   # ALS 协同过滤推荐
   spark-submit --class wang.dragon1573.recommendation.Recommendation jip4fg-1.0.jar
   ```

9. 待上述 3 个 Application 全部运行完成，检查 Hive 数据仓库与 MySQL 数据库，应当存在以下数据表：

   - Hive: `jip4pg.tb_company_info` , `jip4pg.tb_job_info` , `jip4pg.tb_salary_info` 
   - MySQL: `jip4fg.tb_job_salary_pred` , `jip4fg.tb_job_recommend`

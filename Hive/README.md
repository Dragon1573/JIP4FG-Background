# Hive数据分析

## 前言

`Apache Hive` 是隶属于 `Apache Hadoop` 生态圈的一款数据仓库，它基于 `Apache HDFS` 和 `Apache Hadoop MapReduce` 运行，为用户提供与 `Transact-SQL` 相似的 `HiveQL` 查询语言支持，用 `HiveQL` 实现 `CSV` / `SequenceFile` / `JSON` / `JSON Lines` 等结构化数据表、半结构化数据表甚至是 `Apache HBase` 等非结构化数据表的『类 `SQL` 语法』查询支持。

## 目录

1.  [Hive 字段全览](docs/columns.md) 
2.  [任务需求](docs/tasks.md) 

## 使用方法

&emsp;&emsp; 本项目提供的 `*.sql` 数据仓库脚本和 `*.sh` 命令行脚本可以选择通过 Hue 或任意 Shell 终端运行，以下方法为 Shell 终端使用方法。

1. 克隆本项目

   ```shell
   # SSH 方式
   git clone git@github.com:Dragon1573/JIP4FG-Hive.git
   
   # HTTPS 方式
   git clone https://github.com/Dragon1573/JIP4FG-Hive.git
   ```

2. 将本项目的 `scripts/` 目录上传到 Hive 客户端（集群主节点或任何能够直接访问集群的设备）

3. 将 [Dragon1573/JIP4FG-Scrapy](https://github.com/Dragon1573/JIP4FG-Scrapy) 运行得到的 `*.csv` 数据集上传到 HDFS 的 `/user/dragon1573/hdfs2hive/scrapy_raw/` 目录下

4. 前往 [全国省市县行政区划 - 进制数据](https://www.binstd.com/api/area.html) ，根据平台指引调用 API ，处理得到如下形式的全国地级行政区划信息表，保存为 `*.jsonl` ，上传到 HDFS 的 `/user/dragon1573/hdfs2hive/all_city/` 目录下

   ```json
   {"id": "148","name": "郑州","parentid": "10","parentname": "河南","areacode": "0371","zipcode": "450000","depth": "2"}
   {"id": "149","name": "洛阳","parentid": "10","parentname": "河南","areacode": "0379","zipcode": "471000","depth": "2"}
   {"id": "150","name": "开封","parentid": "10","parentname": "河南","areacode": "0378","zipcode": "475100","depth": "2"}
   ```

5. 由于本项目需要加载 JSON Lines 格式的数据集，请确保 `hive-hcatalog-core-<version>.jar` 已被添加到 Hive 的类路径中

6. 执行以下命令完成 Hive 数据加载

   ```shell
   hive -f scripts/hive-utilities.sql
   ```
   
7. 待完成 [Dragon1573/JIP4FG-Spark](https://github.com/Dragon1573/JIP4FG-Spark) 的数据清洗部分后，依次执行以下命令完成 Hive 数据分析

   ```shell
   # 数据分析
   hive -f scripts/hive-queries.sql
   # 将数据表导出到 HDFS
   hive -f scripts/hive-export.sql
   ```
   
8. 使用 [JIP4FG/jip4fg.sql · Cyxxxxx/JIP4FG](https://github.com/Cyxxxxx/JIP4FG/blob/main/database/mysql/jip4fg.sql) 准备好 MySQL 数据库后，依次执行以下命令导出数据库

   ```shell
   # 设置 MySQL 数据库地址（示例）
   export jdbcURL="jdbc:mysql://<host>:<port>/<database>"
   # 设置数据库登陆用户名
   export jdbcUser="<user>"
   # 设置数据库登陆密码
   export jdbcToken="<password>"
   # 执行导出
   sh scripts/sqoop-export.sh
   ```

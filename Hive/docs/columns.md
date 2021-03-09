# Hive 数据表字段全览

## 原始数据表

### 原始爬虫表

-   名称：`etb_scrapy_raw`

|        列名         |   释义   |        列名         |         释义          |
| :-----------------: | :------: | :-----------------: | :-------------------: |
|  company_category   | 企业领域 |    job_good_list    |       职位标签        |
| company_description | 企业描述 |    job_graduates    |       是否毕业        |
|  company_location   | 企业位置 |      job_money      |       实习薪资        |
|    company_name     | 企业名称 |    job_position     |       工作地点        |
|    company_scale    | 企业性质 |      job_time       |       实习时长        |
|    company_tags     | 企业标签 |      job_type       | 职位类别（实习/校招） |
|      dead_line      | 截止日期 |      job_week       |        工作日         |
|     details_url     | 详情URL  |    new_job_name     |       职位名称        |
|    job_academic     | 最低学历 | profile_requirement |       简历需求        |
|      job_date       | 发布日期 |    staff_amount     |       员工人数        |
|     job_detail      | 职位描述 |                     |                       |

&emsp;&emsp; 此数据表使用 `Scrapy` 爬虫框架从 [实习僧](https://www.shixiseng.com/interns) 全量采集获得，存储格式为带引号的 `*.csv` 逗号分隔文件，数据量为 16358 行，包含本次项目所需的全部职位信息。在 `Hive` 中载入外部表时，我们将使用 `org.apache.hadoop.hive.serde2.OpenCSVSerde` 解析数据集。

### 全国城市信息表

-   名称：`etb_all_city`

|    列名    |   释义   |   列名   |   释义   |
| :--------: | :------: | :------: | :------: |
|     id     | 城市编号 | areacode | 电话区号 |
|    name    | 城市名称 | zipcode  | 邮政编码 |
|  parentid  | 省份编号 |  depth   | 检索深度 |
| parentname | 省份名称 |          |          |

&emsp;&emsp; 此数据表使用 `requests` + `json` 从 [全国省市县行政区划 - 进制数据](https://www.binstd.com/api/area.html) 公共接口获取，存储格式为 `*.jsonl` ，用于后续的职位地理信息标准化。在 `Hive` 中载入外部表时，我们将使用 `org.apache.hive.hcatalog.data.JsonSerDe` 解析数据集。

### 历史薪资表

-   名称：`etb_time_line`

|   列名   |   释义   |  列名  |   释义   |
| :------: | :------: | :----: | :------: |
| job_type | 职位类型 |  year  | 所属年份 |
| job_name | 职位名称 | salary | 薪资水平 |

## 基础表

&emsp;&emsp; 在使用诸如 `MySQL` 的关系型数据库或诸如 `Hive` 的数据仓库时，我们更多的是将位于多个表的字段连接在一起形成一张宽表。但在本项目中，原始数据集是从源网页以宽表的形式进行采集的。

&emsp;&emsp; 综合考虑，我们选择使用 `Spark` 完成数据预处理工作，并在数据预处理过程中完成以下内容：

1.  将宽表拆分为以下 3 张基础表；
2.  使用 `Spark MLlib` 将所有职位分为 5 类，标签存储到 `tb_job_info.job_type` 字段；
3.  借助 `etb_all_city` 完成 `tb_company_info.location` 和 `tb_job_info.location` 字段的地理信息标准化。

具体数据预处理流程请参阅 [Spark 相关内容](../../spark/README.md) 。

### 企业信息表

- 名称：`tb_company_info`

|     列名     |   释义    |     列名      |   释义   |
| :----------: | :-------: | :-----------: | :------: |
|  company_id  | 企业 UUID | industry_type | 企业类别 |
| company_name | 企业名称  |   location    | 企业地址 |
| company_info | 企业简述  |               |          |

### 岗位信息表

- 名称：`tb_job_info`

|     字段     |   释义    |    字段     |        释义        |
| :----------: | :-------: | :---------: | :----------------: |
|    job_id    | 岗位 UUID |  job_desc   |      职位详情      |
|  company_id  | 企业 UUID |  location   |      工作地点      |
| company_name | 企业名称  | work_nature | 0 - 实习，1 - 校招 |
|   job_name   | 职位名称  |   origin    |      信息来源      |
|   job_type   | 职位类别  | origin_url  |      来源链接      |

### 岗位薪资表

- 名称：`tb_job_salary`

|   字段    |   释义    |   字段    |   释义   |
| :-------: | :-------: | :-------: | :------: |
| salary_id | 薪资 UUID |    sup    | 点赞人数 |
|  job_id   | 岗位 UUID |  non_sup  | 点踩人数 |
|  salary   |   薪资    | create_dt | 发布时间 |

在本地测试过程中发现， `Sqoop` 无法直接从 `Hive` / `HCatalog` 导出数据，只有 `HDFS` 文件能够从 `Sqoop` 完成导出。因此，我们选择借助 `Hive` 提供的 `INSERT OVERWRITE` 功能，将 `Parquet` 转换为没有列包围符的 `TSV` 文件，存储于 `HDFS` 的非 `Hive` 内部表区域，方便 `Sqoop` 的解析和 `MySQL` 的导入。

/*
 * Copyright (c) 2021 Dragon1573. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

-- 切换数据库
USE jip4pg;
-- 企业信息表
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/tb_company_info.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS TEXTFILE
SELECT *
FROM tb_company_info;
-- 职位信息表
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/tb_job_info.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS TEXTFILE
SELECT *
FROM tb_job_info;
-- 职位薪资表
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/tb_job_salary.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS TEXTFILE
SELECT *
FROM tb_job_salary;

-- 任务 1-1
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/rs_ci_1.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS
    TEXTFILE
SELECT *
FROM rs_ci_1;
-- 任务 1-2
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/rs_ci_2.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS
    TEXTFILE
SELECT *
FROM rs_ci_2;
-- 任务 1-3
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/tb_industry_rank.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS
    TEXTFILE
SELECT *
FROM tb_industry_rank;
-- 任务 1-4
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/tb_city_company_count.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS
    TEXTFILE
SELECT *
FROM tb_city_company_count;
-- 任务 2-1
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/rs_ji_1.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS
    TEXTFILE
SELECT *
FROM rs_ji_1;
-- 任务 2-2
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/rs_ji_2.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS
    TEXTFILE
SELECT *
FROM rs_ji_2;
-- 任务 2-3
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/rs_ji_3.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS
    TEXTFILE
SELECT *
FROM rs_ji_3;
-- 任务 2-4
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/rs_ji_4.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS
    TEXTFILE
SELECT *
FROM rs_ji_4;
-- 任务 2-5
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/rs_ji_5.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS
    TEXTFILE
SELECT *
FROM rs_ji_5;
-- 任务 3-1
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/rs_js_1.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS
    TEXTFILE
SELECT *
FROM rs_js_1;
-- 任务 3-2
INSERT OVERWRITE DIRECTORY '/user/root/hive2hdfs/rs_js_2.tsv' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS
    TEXTFILE
SELECT *
FROM rs_js_2;

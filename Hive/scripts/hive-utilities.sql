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

-------------------------------------------------------------------------------
-- 清空并移除数据库
DROP DATABASE IF EXISTS jip4pg CASCADE;
-- 重新创建数据库
CREATE DATABASE IF NOT EXISTS jip4pg;
-- 切换到数据库
USE jip4pg;
-- 移除表
DROP TABLE IF EXISTS etb_scrapy_raw;
-- 新建外部原始数据表（方便反复删库测试）
CREATE EXTERNAL TABLE etb_scrapy_raw
(
    company_category    VARCHAR(50) COMMENT '企业领域',
    company_description VARCHAR(4096) COMMENT '企业描述',
    company_location    VARCHAR(200) COMMENT '企业位置',
    company_name        VARCHAR(50) COMMENT '企业名称',
    company_scale       STRING COMMENT '企业性质',
    company_tags        STRING COMMENT '企业标签',
    dead_line           STRING COMMENT '截止日期',
    details_url         STRING COMMENT '详情URL',
    job_academic        STRING COMMENT '最低学历',
    job_date            TIMESTAMP COMMENT '发布日期',
    job_detail          STRING COMMENT '职位描述', -- TEXT in MySQL
    job_good_list       STRING COMMENT '职位标签',
    job_graduates       STRING COMMENT '是否毕业',
    job_money           VARCHAR(10) COMMENT '实习薪资',
    job_position        VARCHAR(200) COMMENT '工作地点',
    job_time            STRING COMMENT '实习时长',
    job_type            STRING COMMENT '职位类别（实习/校招）',
    job_week            STRING COMMENT '工作日',
    new_job_name        VARCHAR(100) COMMENT '职位名称',
    profile_requirement STRING COMMENT '简历需求',
    staff_amount        STRING COMMENT '员工人数'
) ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde' STORED AS TEXTFILE LOCATION '/user/dragon1573/hdfs2hive/scrapy_raw';
-- 省市行政区划映射表
DROP TABLE IF EXISTS etb_all_city;
CREATE EXTERNAL TABLE etb_all_city
(
    id         INT COMMENT '市级编号',
    name       STRING COMMENT '市级名称',
    parentid   INT COMMENT '省级编号',
    parentname STRING COMMENT '省级名称',
    areacode   STRING COMMENT '市级区号',
    zipcode    STRING COMMENT '邮政编码',
    depth      INT COMMENT '检索深度'
) ROW FORMAT SERDE 'org.apache.hive.hcatalog.data.JsonSerDe' STORED AS TEXTFILE LOCATION '/user/dragon1573/hdfs2hive/all_city';
DROP TABLE IF EXISTS etb_time_line;
CREATE EXTERNAL TABLE etb_time_line
(
    job_type STRING COMMENT '职位类别',
    job_name STRING COMMENT '职位名称',
    year     INT COMMENT '薪资发布年份',
    salary   STRING COMMENT '薪资水平'
) ROW FORMAT SERDE 'org.apache.hive.hcatalog.data.JsonSerDe' STORED AS TEXTFILE LOCATION '/user/dragon1573/hdfs2hive/time_line';

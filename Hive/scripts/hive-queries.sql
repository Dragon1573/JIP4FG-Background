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
-- 任务 1-1
DROP TABLE IF EXISTS rs_ci_1;
CREATE TABLE IF NOT EXISTS rs_ci_1 STORED AS PARQUET AS
SELECT count(DISTINCT company_id) AS total_count
FROM tb_company_info;
-- 任务 1-2
DROP TABLE IF EXISTS rs_ci_2;
CREATE TABLE IF NOT EXISTS rs_ci_2 STORED AS PARQUET AS
SELECT company_name, count(*) AS counts
FROM tb_company_info
GROUP BY company_name
HAVING counts >= 2
ORDER BY counts DESC;
-- 任务 1-3
DROP TABLE IF EXISTS tb_industry_rank;
CREATE TABLE IF NOT EXISTS tb_industry_rank STORED AS PARQUET AS
SELECT regexp_replace(reflect('java.util.UUID', 'randomUUID'), '-', '') AS industry_id,
       industry_type,
       count(*)                                                         AS count
FROM tb_company_info
GROUP BY industry_type
LIMIT 10;
-- 任务 1-4
DROP TABLE IF EXISTS tb_city_company_count;
CREATE TABLE IF NOT EXISTS tb_city_company_count STORED AS PARQUET AS
SELECT location AS city, count(*) AS com_count
FROM tb_company_info
GROUP BY location
ORDER BY com_count DESC;

-- 任务 2-1
DROP TABLE IF EXISTS rs_ji_1;
CREATE TABLE IF NOT EXISTS rs_ji_1 STORED AS PARQUET AS
SELECT count(DISTINCT job_id) AS counts
FROM tb_job_info;
-- 任务 2-2
DROP TABLE IF EXISTS rs_ji_2;
CREATE TABLE IF NOT EXISTS rs_ji_2 STORED AS PARQUET AS
SELECT company_id, company_name, count(*) AS counts
FROM tb_job_info
GROUP BY company_id, company_name
HAVING counts >= 2
ORDER BY counts DESC;
-- 任务 2-3
DROP TABLE IF EXISTS rs_ji_3;
CREATE TABLE IF NOT EXISTS rs_ji_3 STORED AS PARQUET AS
SELECT job_name, count(*) AS counts, dense_rank() OVER (ORDER BY count(*) DESC) AS ranks
FROM tb_job_info
GROUP BY job_name
LIMIT 10;
-- 任务 2-4
DROP TABLE IF EXISTS rs_ji_4;
CREATE TABLE IF NOT EXISTS rs_ji_4 STORED AS PARQUET AS
SELECT count(*) AS counts
FROM tb_job_info AS tji
         JOIN tb_company_info AS tci ON tji.company_id = tci.company_id
WHERE tji.location = tci.location;
-- 任务 2-5
DROP TABLE IF EXISTS rs_ji_5;
CREATE TABLE IF NOT EXISTS rs_ji_5 STORED AS PARQUET AS
SELECT tji.location,
       count(DISTINCT job_id)                                   AS counts,
       round(count(DISTINCT job_id) / sji.total, 3)             AS percentage,
       dense_rank() OVER (ORDER BY count(DISTINCT job_id) DESC) AS ranks
FROM tb_job_info AS tji
         CROSS JOIN (
    SELECT count(DISTINCT job_id) AS total
    FROM tb_job_info
) AS sji
GROUP BY tji.location, sji.total
LIMIT 3;

-- 任务 3-1
DROP TABLE IF EXISTS rs_js_1;
CREATE TABLE IF NOT EXISTS rs_js_1 STORED AS PARQUET AS
SELECT salary, count(DISTINCT job_id) AS counts, dense_rank() OVER (ORDER BY count(DISTINCT job_id) DESC) AS ranks
FROM tb_job_salary
GROUP BY salary
LIMIT 5;
-- 任务 3-2
DROP TABLE IF EXISTS rs_js_2;
CREATE TABLE IF NOT EXISTS rs_js_2 STORED AS PARQUET AS
SELECT concat(string(month(tjs.create_dt)), '月')                       AS month_id,
       count(DISTINCT tjs.salary_id)                                   AS counts,
       round(count(DISTINCT tjs.salary_id) / sq.total, 3)              AS percentage,
       dense_rank() OVER (ORDER BY count(DISTINCT tjs.salary_id) DESC) AS ranks
FROM tb_job_salary AS tjs
         CROSS JOIN (
    SELECT count(DISTINCT salary_id) AS total
    FROM tb_job_salary
) AS sq
GROUP BY month(tjs.create_dt), sq.total;

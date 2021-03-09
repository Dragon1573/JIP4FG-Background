###############################################################################
# Copyright (c) 2021 Dragon1573. All rights reserved.
#
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#
# This Source Code Form is "Incompatible With Secondary Licenses", as
# defined by the Mozilla Public License, v. 2.0.
###############################################################################

# 导出企业信息表
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_company_info --export-dir '/user/root/hive2hdfs/tb_company_info.tsv' --input-fields-terminated-by '\t'
# 导出岗位信息表
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_job_info --export-dir '/user/root/hive2hdfs/tb_job_info.tsv' --input-fields-terminated-by '\t'
# 导出薪资信息表
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_job_salary --export-dir '/user/root/hive2hdfs/tb_job_salary.tsv' --input-fields-terminated-by '\t'

# 任务 1-1
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_company_count --export-dir '/user/root/hive2hdfs/rs_ci_1.tsv' --input-fields-terminated-by '\t'
# 任务 1-2
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_branch_company_count --export-dir '/user/root/hive2hdfs/rs_ci_2.tsv' --input-fields-terminated-by '\t'
# 任务 1-3
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_industry_rank --export-dir '/user/root/hive2hdfs/tb_industry_rank.tsv' --input-fields-terminated-by '\t'
# 任务 1-4
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_city_company_count --export-dir '/user/root/hive2hdfs/tb_city_company_count.tsv' --input-fields-terminated-by '\t'
# 任务 2-1
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_job_count --export-dir '/user/root/hive2hdfs/rs_ji_1.tsv' --input-fields-terminated-by '\t'
# 任务 2-2
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_double_more_job_company --export-dir '/user/root/hive2hdfs/rs_ji_2.tsv' --input-fields-terminated-by '\t'
# 任务 2-3
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_hot_job_top10 --export-dir '/user/root/hive2hdfs/rs_ji_3.tsv' --input-fields-terminated-by '\t'
# 任务 2-4
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_same_location_com_job_count --export-dir '/user/root/hive2hdfs/rs_ji_4.tsv' --input-fields-terminated-by '\t'
# 任务 2-5
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_city_job_count --export-dir '/user/root/hive2hdfs/rs_ji_5.tsv' --input-fields-terminated-by '\t'
# 任务 3-1
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_salary_top5 --export-dir '/user/root/hive2hdfs/rs_js_1.tsv' --input-fields-terminated-by '\t'
# 任务 3-2
sqoop-export --connect "$jdbcURL" --username "$jdbcUser" --password "$jdbcToken" --table tb_release_job_by_month --export-dir '/user/root/hive2hdfs/rs_js_2.tsv' --input-fields-terminated-by '\t'

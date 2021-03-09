# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

from scrapy import Field, Item


class ShiXiSengJobItem(Item):
    company_category = Field()  # 企业领域
    company_description = Field()  # 企业描述
    company_location = Field()  # 企业位置
    company_name = Field()  # 企业名称
    company_scale = Field()  # 企业规模
    company_tags = Field()  # 企业标签
    dead_line = Field()  # 截止日期
    details_url = Field()  # 详情链接
    job_academic = Field()  # 最低学历
    job_date = Field()  # 发布日期
    job_detail = Field()  # 职位描述
    job_good_list = Field()  # 职位标签
    job_graduates = Field()  # 是否毕业
    job_money = Field()  # 实习薪资
    job_position = Field()  # 工作地点
    job_time = Field()  # 实习时长
    job_type = Field()  # 职位类型（实习为0，校招为1）
    job_week = Field()  # 工作日
    new_job_name = Field()  # 职位名称
    profile_requirement = Field()  # 简历需求
    staff_amount = Field()  # 员工人数
    pass

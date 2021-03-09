from re import search
from typing import List

from lxml.etree import HTML
from lxml.html import HtmlElement
from scrapy import Spider
from scrapy.http import HtmlResponse

from jobs.items import ShiXiSengJobItem
from jobs.utils.parseFont import parse_font


def strip_func(_string: str) -> str:
    return _string.strip()


class ShiXiSengSpider(Spider):
    """ 实习僧 """
    name = 'ShiXiSeng'
    allowed_domains = ['www.shixiseng.com']
    start_urls = [
        'https://www.shixiseng.com/interns?page=1&type=intern',
        'https://www.shixiseng.com/interns?page=1&type=school'
    ]

    def __init__(self, name=None, **kwargs):
        """启动爬虫

        实习僧的汉字是加密过的，所以在开始爬虫前，我们需要另外地把字体文件准备好
        """
        self.font_map = parse_font(_logger=self.logger, _url=self.start_urls[0])
        super().__init__(name, **kwargs)

    def parse(self, response: HtmlResponse, **kwargs):
        """ 页面解析 """
        # 提取请求的列表种类（实习/校招）
        _job_type = search(r'type=([a-z]+)', response.url).group(1)
        # 存储详情页
        details_url: List[str] = response.xpath('//a[@class="title ellipsis font"]/@href').getall()
        for u in details_url:
            yield response.follow(url=u, callback=self.parse_details, meta={
                'job_type': _job_type
            })

        # 获取当前页码
        current_page = search(pattern=r'page=(\d+)', string=response.url).group(1)
        if current_page != '1':
            # 当前已经是终点页，直接返回
            return

        # 获取总页数
        max_page = int(response.xpath('//li[@class="number"][6]/text()')[0].get())
        # 直接并行爬取所有页面
        for r in response.follow_all(
                urls=[f'interns?page={p}&type={_job_type}' for p in range(2, max_page + 1)],
                callback=self.parse
        ):
            yield r

    def parse_details(self, response: HtmlResponse):
        """解析详情页

        Parameters
        ----------
        response: HtmlResponse
            响应
        """
        # HTML字体解码
        decoded_html: str = response.text
        for k, v in self.font_map.items():
            decoded_html = decoded_html.replace(k, v)
        decoded_html = decoded_html.replace('&nbsp;', ' ')
        decoded_html: HtmlElement = HTML(text=decoded_html)

        # 导出数据记录，交给管道进行清洗操作
        return ShiXiSengJobItem(
            job_type=response.meta['job_type'],  # 实习 = 0 ，校招 = 1
            details_url=response.url,  # 详情 URL
            new_job_name=decoded_html.xpath('//div[@class="new_job_name"]/@title'),  # 职位名称
            job_date=decoded_html.xpath('//div[contains(@class, "job_date")]/span/text()'),  # 发布日期
            job_money=decoded_html.xpath('//span[@class="job_money cutom_font"]/text()'),  # 实习薪资
            job_position=decoded_html.xpath('//span[@class="job_position"]/@title'),  # 工作地点
            job_graduates=decoded_html.xpath('//span[@class="job_graduates"]/text()'),  # 是否毕业
            job_academic=decoded_html.xpath('//span[@class="job_academic cutom_font"]/text()'),  # 最低学历
            job_week=decoded_html.xpath('//span[@class="job_week cutom_font"]/text()'),  # 工作日
            job_time=decoded_html.xpath('//span[@class="job_time cutom_font"]/text()'),  # 实习时长
            # 职位标签
            job_good_list=decoded_html.xpath('//div[@class="job_good_list"]/span/text()'),
            # 职位描述
            job_detail=decoded_html.xpath('//div[@class="job_detail"][1]'),
            # 简历需求
            profile_requirement=decoded_html.xpath('//div[@class="con-job"][2]/div[2]/text()'),
            # 截止日期
            dead_line=decoded_html.xpath('//div[@class="con-job"][2]/div[3]/text()'),
            company_name=decoded_html.xpath('//a[contains(@class, "com-name")]/text()'),  # 企业名称
            company_description=decoded_html.xpath('//div[@class="com-desc"]/text()'),  # 企业描述
            company_tags=decoded_html.xpath('//div[@class="com-tags"]/div/text()'),  # 企业标签
            company_category=decoded_html.xpath(
                # 企业领域
                '//i[contains(@class, "iconhangyelingyu")]/following-sibling::text()'
            ),
            company_scale=decoded_html.xpath(
                # 企业性质
                '//i[contains(@class, "iconqiyexingzhi")]/following-sibling::text()'
            ),
            staff_amount=decoded_html.xpath(
                # 企业规模（员工人数）
                '//i[contains(@class, "iconqiyeguimo")]/following-sibling::text()'
            ),
            company_location=decoded_html.xpath(
                # 企业位置
                '//i[contains(@class, "iconsuozaichengshi")]/following-sibling::text()'
            )
        )

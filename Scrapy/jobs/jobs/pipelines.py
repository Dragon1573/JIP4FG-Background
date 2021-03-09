# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html


# useful for handling different item types with a single interface
from itemadapter import ItemAdapter
from lxml.etree import tostring

from jobs.items import ShiXiSengJobItem
from jobs.spiders import ShixiSeng


def trim_all(_string: str) -> str:
    return _string.strip().replace('\n', '').replace('\r', '').replace('\t', '')


class ShiXiSengPipeline:
    """ 实习僧数据处理管道 """

    @staticmethod
    def process_item(_item: ShiXiSengJobItem, _spider: ShixiSeng) -> ShiXiSengJobItem:
        """ 数据处理逻辑 """
        # 将空白数据置为None
        adapter = ItemAdapter(_item)
        for i in adapter.keys():  # type: str
            if len(adapter[i]) == 0:
                adapter[i] = None

        # 数据合法性校验
        for i in adapter.keys():
            if adapter[i] is None or i in ('details_url', 'job_type'):
                pass  # 详情URL原样保留
            elif i in ('job_good_list', 'company_tags'):
                # 两类标签列表批量转换，去除首尾空白符
                adapter[i] = list(map(trim_all, adapter[i]))
                pass
            elif i == 'job_detail':
                # 这一项需要以HTML源代码形式存储，借助lxml进行代码规范化
                adapter[i] = tostring(element_or_tree=adapter[i][0], encoding=str)
                # 链式移除其中遗留的神奇的格式控制符，它们会对后续Hive数据载入产生影响
                adapter[i] = trim_all(adapter[i])
            else:
                # 其他元素则统一提取字符串
                adapter[i] = trim_all(adapter[i][0])
                if i in ('dead_line', 'profile_requirement'):
                    # 两个日期需要截断，只保留日期的部分，文字注释丢弃
                    adapter[i] = adapter[i].split('：')[1]

        # 进行后续处理
        return adapter.item

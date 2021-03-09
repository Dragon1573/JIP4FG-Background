from io import BytesIO
from logging import Logger
from re import search

from fontTools.ttLib import TTFont
from lxml.etree import HTML
from requests import get


def parse_font(_logger: Logger, _url: str):
    """ 解析字体 """
    # 设置请求头
    headers = {
        'User-Agent': ('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) '
                       'Chrome/86.0.4240.198 Safari/537.36 Edg/86.0.622.69')
    }

    # 请求首页
    response = get(url=_url, headers=headers)
    # 得到字体包链接
    xpath_selector = HTML(response.text)
    style: str = xpath_selector.xpath('/html/head/style[1]')[0].text
    font_source_url = search(pattern=r'url\((.*)\)', string=style).group(1)
    # 下载字体包
    response = get(url=f'https://www.shixiseng.com{font_source_url}', headers=headers)
    woff_file = BytesIO(initial_bytes=response.content)
    _logger.info('字体包下载完成！')

    # 解码字体包
    fonts = TTFont(woff_file)
    # 注意前2个字是空的，那个在线字体预览站没有把第1个字显示出来，导致后面字体还原的时候全部凯撒偏移了
    font_base_order = fonts.getGlyphOrder()[2:]
    font_list = [
        *list(map(str, range(10))), u'一', u'师', 'X', u'会', u'四', u'计', u'财', u'场', 'D', 'H',
        'L', 'P', 'T', u'聘', u'招', u'工', 'd', u'周', 'I', u'端', 'p', u'年', 'h', 'x', u'设', u'程',
        u'二', u'五', u'天', 't', 'C', 'G', u'前', 'K', 'O', u'网', 'S', 'W', 'c', 'g', 'k', 'o', 's',
        'w', u'广', u'市', u'月', u'个', 'B', 'F', u'告', 'N', 'R', 'V', 'Z', u'作', 'b', 'f', 'j', 'n',
        'r', 'v', 'z', u'三', u'互', u'生', u'人', u'政', 'A', 'J', 'E', 'I', u'件', 'M', '行', 'Q', 'U',
        'Y', 'a', 'e', 'i', 'm', u'软', 'q', 'u', u'银', 'y', u'联'
    ]
    font_dict = {v: '&#' + hex(k)[1:] for k, v in fonts.getBestCmap().items()}
    _logger.info('字体包安装完成！')
    return {font_dict[key]: value for key, value in zip(font_base_order, font_list)}

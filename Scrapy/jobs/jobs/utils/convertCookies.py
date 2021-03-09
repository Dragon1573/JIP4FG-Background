from json import dumps


def convert_cookies(cookie_str: str) -> dict:
    """
    解析Cookies字符串

        将Cookies字符串还原为Python字典形式，供Scrapy使用

    Parameters
    ----------
    cookie_str: str
        字符串
    """
    obj = [i.split('=') for i in cookie_str.split(';')]
    return {k.strip(): v.strip() for (k, v) in obj}


if __name__ == '__main__':
    string = input('请在此粘贴 Cookies 字符串：\n')
    print(dumps(convert_cookies(string), ensure_ascii=False, indent=4))
    pass

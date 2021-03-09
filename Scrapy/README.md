# Scrapy 数据采集

## 前言

&emsp;&emsp; Scrapy 是适用于 Python 的一个快速、高层次的屏幕抓取和 Web 页面抓取框架，主要用于抓取 Web 站点并从页面中提取结构化数据。它用途广泛，在数据挖掘、检测和自动化测试中较为常见。

&emsp;&emsp; Scrapy 的优势在于其『框架』式设计，开发者可以根据业务需求自由地实现与扩展。同时， Scrapy 还提供了多种类型的爬虫基类，例如 `BaseSpider` 和 `SiteMapSpider` ，在最新版本中又添加了对 Web 2.0 爬虫的支持。

## 项目内容

&emsp;&emsp; 本项目实现了一个简易的 Scrapy 爬虫，从 [实习僧](https://www.shixiseng.com/) 爬取其公开的职位信息并存储为 `*.csv` 逗号分隔文件。

## 使用方法

1. 推荐使用 Python v3.7.9 。`Scrapy` 框架依赖于 `Twisted` ，而 PyPI 官方软件源仅提供 Python 3.7.x 及以下版本的 `*.whl` 软件包；如果您正在使用 Python 3.8.x 及以上版本，请自行考虑解决方案：

   1. 安装 [Microsoft Visual C++ Build Tools 2015](http://go.microsoft.com/fwlink/?LinkId=691126) ，由 `Twisted` 内部的 `setup.py` 调用运行，本地编译 `*.whl` 
   2. 前往 [Twisted - Unofficial Windows Binaries for Python Extension Packages](https://www.lfd.uci.edu/~gohlke/pythonlibs/#twisted) 下载安装预编译 `*.whl` 

2. 克隆本项目

   ```shell
   # （推荐）使用 SSH 方式克隆本项目
   git clone git@github.com:Dragon1573/JIP4FG-Scrapy.git
   
   # 使用 HTTPS 方式克隆本项目
   git clone https://github.com/Dragon1573/JIP4FG-Scrapy.git
   ```

3. 在本项目内新建 Virtualenv 虚拟环境，并安装所需依赖项

   ```sh
   # 切换至项目根目录
   cd JIP4FG-Scrapy
   
   # 新建 virtualenv 虚拟环境
   virtualenv venv
   
   # 激活虚拟环境（Windows Command Prompt）
   ./venv/Scripts/activate.bat
   # 激活虚拟环境（Powershell）
   ./venv/Scripts/Activate.ps1
   # 激活虚拟环境（Linux 和 macOS）
   source venv/Scripts/activate
   
   # 更新虚拟环境中的 pip
   pip install -U pip
   # 安装依赖项
   pip install -r requirements.txt
   ```

4. 待所有依赖项安装完成后，使用如下命令启动爬虫并指定数据集存储位置

   ```sh
   # 切换到 Scrapy 项目
   cd jobs
   
   # 启动爬虫
   scrapy crawl ShiXiSeng -o datasets/raw.csv
   ```

&emsp;&emsp; 本项目使用全量爬取逻辑，每次爬虫数据量约为 1.6 万行，爬虫结束后，在 `datasets/raw.csv` 应能看到生成的原始数据集。

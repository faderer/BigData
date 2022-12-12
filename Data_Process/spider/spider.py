import requests
from bs4 import BeautifulSoup
import urllib.parse
import pandas as pd
import queue
from time import sleep
from lxml import etree
import json
import re
import random

job_dist = {}

# content crawled on the first level
result = {
    "big_category": [],
    "small_category": [],
    "job_name": [],
    "company_name": [],
    "salary": [],
    "work_area": [],
    "company_type": [],
    "work_year": [],
    "education": [],
    "recruitment_scale": [],
    "welfare": [],
    "company_size": []
}

def initial():
    job_dist["后端开发"] = ["0121", "0120", "0122", "0124", "0126", "0153", "0151", "0152", "0130", "0129", "0131", "0132", "0133", "0155", "0154", "0117", "0128", "0106", "0107", "0143"]
    # page_dist["后端开发"] = ["50", "3", "14", "5", "8", "2", "1", "2", "7", "1", "1", "0132", "0133", "0155", "0154", "0117", "0128", "0106", "0107", "0143"]
    job_dist["移动开发"] = ["7701", "7702", "7705"]
    job_dist["前端开发"] = ["7201", "7202"]
    job_dist["人工智能"] = ["7301", "7302", "7303", "7304", "7305", "7306", "7307", "7308", "7309", "7310", "7311"]
    job_dist["游戏"] = ["7809", "7810", "7811", "7823", "7812", "7813", "7821"]
    job_dist["测试"] = ["2707", "2718", "2719", "2724", "2720", "2721", "2722", "2706", "2704", "2725"]
    job_dist["运维/技术支持"] = ["7901", "7920", "7902", "7903", "7904", "7906", "7915", "7907", "7909", "7910", "7913"]
    job_dist["数据"] = ["7502", "7501", "7503", "7504", "7506", "7507", "7508"]


# the first layer of data crawling
def spider_layer1():

    header = {
        'User-Agent': 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36',
        'Host': 'search.51job.com',
        'Cookie': 'guid=6f4e5520fcbe74534a3d7680476f6eca; nsearch=jobarea%3D%26%7C%26ord_field%3D%26%7C%26recentSearch0%3D%26%7C%26recentSearch1%3D%26%7C%26recentSearch2%3D%26%7C%26recentSearch3%3D%26%7C%26recentSearch4%3D%26%7C%26collapse_expansion%3D; search=jobarea%7E%60020000%7C%21ord_field%7E%600%7C%21recentSearch0%7E%60020000%A1%FB%A1%FA000000%A1%FB%A1%FA0121%A1%FB%A1%FA01%A1%FB%A1%FA99%A1%FB%A1%FA%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA9%A1%FB%A1%FA99%A1%FB%A1%FA%A1%FB%A1%FA0%A1%FB%A1%FA%A1%FB%A1%FA2%A1%FB%A1%FA1%7C%21recentSearch1%7E%60020000%A1%FB%A1%FA000000%A1%FB%A1%FA7502%A1%FB%A1%FA01%A1%FB%A1%FA99%A1%FB%A1%FA%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA9%A1%FB%A1%FA99%A1%FB%A1%FA%A1%FB%A1%FA0%A1%FB%A1%FA%A1%FB%A1%FA2%A1%FB%A1%FA1%7C%21recentSearch2%7E%60020000%A1%FB%A1%FA000000%A1%FB%A1%FA7508%A1%FB%A1%FA01%A1%FB%A1%FA99%A1%FB%A1%FA%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA9%A1%FB%A1%FA99%A1%FB%A1%FA%A1%FB%A1%FA0%A1%FB%A1%FA%A1%FB%A1%FA2%A1%FB%A1%FA1%7C%21recentSearch3%7E%60020000%A1%FB%A1%FA000000%A1%FB%A1%FA7507%A1%FB%A1%FA01%A1%FB%A1%FA99%A1%FB%A1%FA%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA9%A1%FB%A1%FA99%A1%FB%A1%FA%A1%FB%A1%FA0%A1%FB%A1%FA%A1%FB%A1%FA2%A1%FB%A1%FA1%7C%21recentSearch4%7E%60020000%A1%FB%A1%FA000000%A1%FB%A1%FA7506%A1%FB%A1%FA01%A1%FB%A1%FA99%A1%FB%A1%FA%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA99%A1%FB%A1%FA9%A1%FB%A1%FA99%A1%FB%A1%FA%A1%FB%A1%FA0%A1%FB%A1%FA%A1%FB%A1%FA2%A1%FB%A1%FA1%7C%21collapse_expansion%7E%601%7C%21'
    }

    list1 = ["后端开发", "移动开发", "前端开发", "人工智能", "游戏", "测试", "运维/技术支持", "数据"]
    list2 = ["back_end", "mobile", "front_end", "ai", "game", "test", "support", "data"]

    for i in range(0, 8):
        result = {
            "big_category": [],
            "small_category": [],
            "job_name": [],
            "company_name": [],
            "salary": [],
            "work_area": [],
            "company_type": [],
            "work_year": [],
            "education": [],
            "recruitment_scale": [],
            "welfare": [],
            "company_size": []
        }
        for s in job_dist[list1[i]]:
            page = 1

            while True:

                url = "https://search.51job.com/list/090200,000000," + s + ",01,9,99,+,2," + str(page) + ".html?lang=c&postchannel=0000&workyear=99&cotype=99&degreefrom=99&jobterm=99&companysize=99&ord_field=0&dibiaoid=0&line=&welfare="
                sleep(random.random()*5)
                response = requests.get(url, headers=header)
                soup = BeautifulSoup(response.content.decode('gbk'), 'lxml')
                title = str(soup.title)
                title = title.split(',')[1]
                # print(title)

                data = re.findall('window.__SEARCH_RESULT__ =(.+)}</script>', str(response.text))[0] + "}"
                data = json.loads(data)

                if len(data["engine_search_result"]) == 0:
                    break

                for list in data["engine_search_result"]:
                    print(list)
                    print("")

                    result["big_category"].append(list1[i])
                    result["small_category"].append(title)
                    result["job_name"].append(list["job_name"])
                    result["company_name"].append(list["company_name"])
                    result["salary"].append(list["providesalary_text"])
                    result["work_area"].append(list["workarea_text"])
                    result["company_type"].append(list["companytype_text"])
                    result["work_year"].append(list["attribute_text"][1])
                    result["education"].append(list["degreefrom"])
                    result["recruitment_scale"].append(list["attribute_text"][-1])
                    result["welfare"].append(list["jobwelf"])
                    result["company_size"].append(list["companysize_text"])

                page = page + 1

        df = pd.DataFrame(result)
        df.to_csv(list2[i] + ".csv", encoding="utf-8_sig")


# the second layer of data crawling
def spider_layer2(cookies):
    header = {
        'User-Agent': 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36'
    }

    # remove the URL from the queue and crawl until the queue is empty
    while q.empty() == False:
        sleep(0.1)
        url = q.get()

        response = requests.get(url, cookies=cookies, headers=header)
        soup = BeautifulSoup(response.content.decode('utf-8'), 'html.parser')

        contents = soup.find('div', id="content")
        # get the name of the movie
        name = contents.find('h1').find_all('span')[0].text.split()[0]
        print(name)

        # get short comments of the movie
        comments = contents.find('div', id="hot-comments")
        for each_comment in comments.find_all('div', class_="comment-item"):
            all_span_tag = each_comment.find_all('span')
            comment = all_span_tag[-1].text
            print(comment)
            # store the above information in a dictionary
            result_layer2["name"].append(name)
            result_layer2["comment"].append(comment)

        result_layer2["name"].append("")
        result_layer2["comment"].append("")

    # store the information crawled on the second layer in csv format locally
    df = pd.DataFrame(result_layer2)
    df.to_csv("layer2.csv", encoding="utf-8_sig")


if __name__ == '__main__':
    initial()
    spider_layer1()
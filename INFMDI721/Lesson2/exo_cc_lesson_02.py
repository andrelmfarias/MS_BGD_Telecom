# coding: utf-8
import requests
from bs4 import BeautifulSoup

urls = {"acer": "https://www.fnac.com/SearchResult/ResultList.aspx?ItemPerPage=100&SCat=8!1%2c8002!2&Search=ordinateur+portable+acer&SFilt=55661!23&sft=1&sl",
       "HP": "https://www.fnac.com/SearchResult/ResultList.aspx?SCat=8!1%2c8002!2&Search=ordinateur+portable+hp&SFilt=57046!23&sft=1&sl"}

def get_soup(url):
    res = requests.get(url)
    if res.status_code == 200:
        html_code = res.text
        soup = BeautifulSoup(html_code,"html.parser")
    return soup

def get_discount_avg(brand):
    url = urls[brand]
    soup = get_soup(url)
    html_code = soup.findAll("div", class_ = "red")
    discount_list = []
    for item in html_code:
        html_text = item.text
        discount = float(html_text[11:-2])
        discount_list.append(discount)
    return sum(discount_list)/len(discount_list)

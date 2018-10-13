# coding: utf-8
import requests
from bs4 import BeautifulSoup

urls = {"acer": "https://www.fnac.com/SearchResult/ResultList.aspx?PageIndex={page}&SCat=8!1%2c8002!2&Search=ordinateur+portable+acer&SFilt=55661!23&sft=1&sl",
       "HP": "https://www.fnac.com/SearchResult/ResultList.aspx?PageIndex={page}&SCat=8!1%2c8002!2&Search=ordinateur+portable+hp&SFilt=57046!23&sft=1&sl"}

n_pages = {"acer": 5, "HP": 16}

def get_soup(url):
    res = requests.get(url)
    if res.status_code == 200:
        html_code = res.text
        soup = BeautifulSoup(html_code,"html.parser")
    return soup

def get_discount_avg(brand):
    url = urls[brand]
    discount_list = []
    for page in range(1,n_pages[brand]+1):
        soup = get_soup(url.format(page=page))
        html_code = soup.findAll("div", class_ = "red")
        for item in html_code:
            html_text = item.text
            discount = float(html_text[11:-2])
            discount_list.append(discount)
    if discount_list: return sum(discount_list)/len(discount_list)
    else: return 0

HP_discount_avg = round(get_discount_avg("HP"),2)
Acer_discount_avg = round(get_discount_avg("acer"),2)
print(f"HP average discount: {HP_discount_avg}%")
print(f"Acer average discount: {Acer_discount_avg}%")

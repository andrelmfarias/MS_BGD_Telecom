# coding: utf-8
import requests
import unittest
from bs4 import BeautifulSoup

website_prefix = "https://www.reuters.com/finance/stocks/financial-highlights/"
companies_url = {"LVMH":"LVMH.PA","Airbus":"AIR.PA","Danone":"DANO.PA"}

def string_to_int_SP(string_SP):




def get_soup(url):
    res = requests.get(url)
    if res.status_code == 200:
        html_code = res.text
        soup = BeautifulSoup(html_code,"html.parser")
    return soup

def get_share_price(company):
    url = website_prefix + companies_url[company]
    soup = get_soup(url)
    specific_style = "font-size: 23px;"
    share_price = soup.find("span", style = specific_style).text
    return float(share_price[5:])

# coding: utf-8
import requests
import unittest
from bs4 import BeautifulSoup

website_prefix = "https://www.reuters.com/finance/stocks/financial-highlights/"
companies_url = {"LVMH":"LVMH.PA","Airbus":"AIR.PA","Danone":"DANO.PA"}

def get_soup(url):
    res = requests.get(url)
    if res.status_code == 200:
        html_code = res.text
        soup = BeautifulSoup(html_code,"html.parser")
    return soup

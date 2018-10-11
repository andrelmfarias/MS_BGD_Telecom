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

def get_share_price(company):
    url = website_prefix + companies_url[company]
    soup = get_soup(url)
    specific_style = "font-size: 23px;"
    share_price = soup.find("span", style = specific_style).text
    return float(share_price.strip())

def get_pct_change(company):
    url = website_prefix + companies_url[company]
    soup = get_soup(url)
    specific_class = "valueContentPercent"
    pct_change = soup.find("span", class_ = specific_class).text
    return float(pct_change.strip()[1:-2]) # returns float x for a pct x%

def get_q4_sales(company):
    url = website_prefix + companies_url[company]
    soup = get_soup(url)
    specific_class = "stripe"
    q4_sales = soup.find("tr", class_ = specific_class).text
    q4_sales = q4_sales.split("\n")[3] # obtaining sales qty as string
    return float(q4_sales.replace(",","")) # transforming to float number

def get_shares_owned(company):
    url = website_prefix + companies_url[company]
    soup = get_soup(url)
    specific_class = "dataTable"
    specific_width = "100%"
    specific_cellpadding="0"
    specific_cellspacing="1"
    shares_owned = soup.find("table", class_ = specific_class, width = specific_width, \
                            cellpadding = specific_cellpadding, cellspacing = specific_cellspacing).text
    shares_owned = shares_owned.strip().split("\n")[1]
    return float(shares_owned[:-1]) # returns float x for a pct x%

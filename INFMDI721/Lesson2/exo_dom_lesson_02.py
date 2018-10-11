# coding: utf-8
import requests
from bs4 import BeautifulSoup
import pandas as pd

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
    html_code = soup.find("tr", class_ = specific_class).text
    q4_sales = html_code.split("\n")[3] # obtaining sales qty as string
    return float(q4_sales.replace(",","")) # transforming to float number

def get_shares_owned(company):
    url = website_prefix + companies_url[company]
    soup = get_soup(url)
    spc_class = "dataTable"
    spc_width = "100%"
    spc_cellpadding = "0"
    spc_cellspacing = "1"
    html_code = soup.find("table", class_ = spc_class, width = spc_width, \
                        cellpadding = spc_cellpadding, \
                        cellspacing = spc_cellspacing).text
    shares_owned = html_code.strip().split("\n")[1]
    return float(shares_owned[:-1]) # returns float x for a pct x%

def get_div_yield(company):
    url = website_prefix + companies_url[company]
    soup = get_soup(url)
    spc_class = "dataTable"
    spc_width = "100%"
    spc_cellpadding = "1"
    spc_cellspacing = "0"
    html_code = soup.findAll("table", class_ = spc_class, width = spc_width, \
                        cellpadding = spc_cellpadding, \
                        cellspacing = spc_cellspacing)[1].text
    # taking the 3 first div_yields as list of string
    div_yield_str = html_code.strip().split("\n")[6:9]
    # transforming list of string in a list of floats using map
    div_yield = list(map(lambda div: float(div), div_yield_str))
    return div_yield

def get_financials(company):
    financials = {}
    financials["SP (€)"] = get_share_price(company)
    financials["% change"] = get_pct_change(company)
    financials["Q4 sales (m€)"] = get_q4_sales(company)
    financials["% of shares owned by inst. inv."] = get_shares_owned(company)

    financials["DY Company(%)"], financials["DY Industry(%)"], \
    financials["DY Sector(%)"] = get_div_yield(company)
    return financials

all_financials = {}

for company in companies_url.keys():
    all_financials[company] = get_financials(company)

financials_df = pd.DataFrame(all_financials).transpose()
print(financials_df)

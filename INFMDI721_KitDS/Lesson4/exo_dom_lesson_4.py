import pandas as pd
import numpy as np
import requests
from bs4 import BeautifulSoup
import multiprocessing as mp
import re

url = "https://www.lacentrale.fr/listing?makesModelsCommercialNames=RENAULT%3AZOE&options=&page={page}&regions={region}"
regions = ['FR-IDF', 'FR-PAC', 'FR-NAQ']
ad_prefix = 'https://www.lacentrale.fr'


def get_soup(url):
    ''' return html_code from url if status_code == 200 and return None if not'''

    res = requests.get(url)
    if res.status_code == 200:  # if request is effectivelly done
        html_code = res.text
        soup = BeautifulSoup(html_code, "html.parser")
        return soup
    else:
        print("Error: HTML code not available")
        print("Status code: " + str(res.status_code))
        return None


def get_url_list(url):
    ''' return the url links to the page of each car '''

    soup = get_soup(url)
    html_code = soup.find_all("a", class_="linkAd ann")
    url_list = list(map(lambda x: x.attrs['href'], html_code))
    return url_list


def append_urls_to_list(urls_list, page, region):
    # concatenation list1 + list2 does not work with mp.list types
    tmp_list = (get_url_list(url.format(page=page, region=region)))
    for element in tmp_list:
        urls_list.append(element)


def get_tot_num_pages(url):
    soup = get_soup(url)
    html_code = soup.find("span", class_="numAnn")
    num_ad = int(html_code.text)
    pages = np.ceil(num_ad/16)  # there are 16 ads per page
    return int(pages)


def get_version(soup):
    html_code = soup.find("div", class_="versionTxt txtGrey7C sizeC mB10 hiddenPhone")
    version = html_code.text.strip()
    return version


def get_year(soup):
    html_code = soup.find("span", class_="clearPhone lH35")
    year = int(html_code.text)
    return year


def get_km(soup):
    km_str = soup.find("span", class_="clearPhone lH40").text
    km_lst = re.findall(r"(\d+)", km_str)
    km = int("".join(km_lst))
    return km


def get_price(soup):
    price_str = soup.find("strong", class_="sizeD lH35 inlineBlock vMiddle ").text.strip()
    price_lst = re.findall(r"(\d+)", price_str)
    price = int("".join(price_lst))
    return price


def get_number(soup):
    number_str = soup.find("div", class_="phoneNumber1").text.strip()
    number_lst = re.findall(r"(\d+)", number_str)
    number = "".join(number_lst)
    return number


def get_info_per_ad(ad_link):
    soup = get_soup(ad_prefix + ad_link)
    version = get_version(soup)
    year = get_year(soup)
    km = get_km(soup)
    price = get_price(soup)
    number = get_number(soup)
    return [version, year, km, price, number]


def insert_info_in_dict(ads_dict, ad_link, region):
    ads_dict[ad_link] = get_info_per_ad(ad_link) + [region]


# Getting number of pages for each for each region
dict_pages = {}
for region in regions:
    num_pages = get_tot_num_pages(url.format(page=1, region=region))
    dict_pages[region] = num_pages

# Getting url links to all annonces
urls_dict = {}
for region in regions:
    # preparing for process parallelization
    jobs = []
    manager = mp.Manager()
    urls_list = manager.list()
    for page in range(1, dict_pages[region]+1):
        p = mp.Process(target=append_urls_to_list, args=(urls_list, page, region))
        jobs.append(p)
        p.start()
    # waiting for all requests to finish
    for p in jobs:
        p.join()
    urls_dict[region] = list(urls_list)

get_info_per_ad(urls_dict['FR-IDF'][0])

# getting all information from ads into a dictionary, working parallelized
manager = mp.Manager()
ads_dict = manager.dict()
jobs = []
for region in regions:
    for ad_link in urls_dict[region]:
        p = mp.Process(target=insert_info_in_dict, args=(ads_dict, ad_link, region))
        jobs.append(p)
        p.start()

for p in jobs:
    p.join()

ads_dict = dict(ads_dict)

print(ads_dict)

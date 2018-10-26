import pandas as pd
import numpy as np
import requests
from bs4 import BeautifulSoup
import multiprocessing as mp

url = "https://www.lacentrale.fr/listing?makesModelsCommercialNames=RENAULT%3AZOE&options=&page={page}&regions={region}"
regions = ['FR-IDF', 'FR-PAC', 'FR-NAQ']
ann_prefix = 'https://www.lacentrale.fr/'


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
    urls_list.append(get_url_list(url.format(page=page, region=region)))


def get_tot_num_pages(url):
    soup = get_soup(url)
    html_code = soup.find("span", class_="numAnn")
    num_ann = int(html_code.text)
    pages = np.ceil(num_ann/16)  # there are 16 announces per page
    return int(pages)


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

print(urls_dict)

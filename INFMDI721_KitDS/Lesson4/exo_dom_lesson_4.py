import pandas as pd
import requests
from bs4 import BeautifulSoup

url = "https://www.lacentrale.fr/listing?makesModelsCommercialNames=RENAULT%3AZOE&options=&page={page}&regions={region}"
regions = ['FR-IDF', 'FR-PAC', 'FR-NAQ']


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


urls_dict = {}
for region in regions:
    page = 1
    url_list_per_region = []
    while True:
        url_list = get_url_list(url.format(page=page, region=region))
        if len(url_list) == 0:
            break
        url_list_per_region += url_list
        page += 1
    urls_dict[region] = url_list_per_region

print(urls_dict)

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
    version_txt = html_code.text.strip().upper()
    # working with version wording
    version = version_txt
    if 'LIFE' in version:
        version = 'LIFE'
    if 'ZEN' in version:
        version = 'ZEN'
    if 'INTENS' in version or 'INT' in version:
        version = 'INTENS'
    if 'EDITION ONE' in version:
        version = 'EDITION ONE'
    if 'STAR WARS' in version:
        version = 'STARS WARS'
    # working with type of version
    if 'TYPE 2' in version_txt:
        version += ' TYPE 2'
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
    # Handle when two different numbers
    if len(number) > 10:
        number = number[:11] + " / " + number[11:]
    return number


def get_type_seller(soup):
    return soup.find('div', class_='bold italic mB10').text.strip().split()[0]


def get_argus_price(version, year):
    url_argus = "https://www.lacentrale.fr/cote-auto-renault-zoe-{version}-{year}.html"
    # Handle differences in argus pages for each year
    if year in list(range(2012, 2016)):
        version_str = 'q90+{model}+charge+rapide{type}'
        if 'TYPE 2' not in version:
            version_str = version_str.format(model=version, type='')
        else:
            model = version.split()[0]
            version_str = version_str.format(model=model, type='+type+2')
    elif year == 2016:
        version_str = 'q90+{model}+charge+rapide{type}'
        model = version.split()[0]
        version_str = version_str.format(model=model, type='+type+2')
    elif year == 2017:
        if 'EDITION ONE' in version:
            version_str = 'q90+edition+one+charge+rapide+gamme+2017'
        if 'LIFE' in version:
            version_str = 'r75+life+gamme+2017'
        if 'ZEN' in version:
            version_str = 'r90+zen+gamme+2017'
        if 'INTENS' in version:
            version_str = 'r90+intens+gamme+2017'
        else:
            return None
    elif year == 2018:
        if 'LIFE' in version:
            version_str = 'q90+life+charge+rapide+gamme+2018'
        elif 'STAR WARS' in version:
            version_str = 'q90+star+wars'
        else:
            return None
    else:
        return None

    url_cote = url_argus.format(version=version_str, year=year)
    soup = get_soup(url_cote)
    price_str = soup.find('span', class_="jsRefinedQuot").text.split()
    price = int("".join(price_str))

    return price


def get_info_per_ad(ad_link):
    soup = get_soup(ad_prefix + ad_link)
    version = get_version(soup)
    year = get_year(soup)
    km = get_km(soup)
    price = get_price(soup)
    number = get_number(soup)
    seller = get_type_seller(soup)
    arg_price = get_argus_price(version, year)
    higher_than_arg = higher_than_arg_price(price, arg_price)
    return [version, year, km, price, number, seller, arg_price, higher_than_arg]


def higher_than_arg_price(price, arg_price):
    if arg_price is None:
        return None
    return price > arg_price


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
    urls_dict[region] = list(urls_list)s

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

# transforming dict to a dataframe and organizing it properly
df = pd.DataFrame(ads_dict).transpose()
df.rename(columns={0: "Version", 1: "Year", 2: "KM", 3: "Price",
                   4: "Number", 5: "Seller type", 6: "Argus price",
                   7: "Higher than Argus price?", 8: "Region"}, inplace=True)
df.reset_index(drop=True, inplace=True)

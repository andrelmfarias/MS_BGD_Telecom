# coding: utf-8
import requests
from bs4 import BeautifulSoup

url = "https://gist.github.com/paulmillr/2657075"

def get_soup(url):
    res = requests.get(url)
    if res.status_code == 200:
        html_code = res.text
        soup = BeautifulSoup(html_code,"html.parser")
    return soup

def get_list_of_users(url):
    lst_users = []
    soup = get_soup(url)
    lst_html = soup.find_all("tr")
    for i in range(1,256+1):
        row = lst_html[i].text
        user = row.split(' ')[2]
        lst_users.append(user)
    return lst_users

users = get_list_of_users(url)

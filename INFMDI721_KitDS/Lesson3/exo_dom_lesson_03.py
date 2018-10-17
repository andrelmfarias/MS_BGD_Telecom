# coding: utf-8
import requests
from bs4 import BeautifulSoup

url = "https://gist.github.com/paulmillr/2657075"
url_API = "https://api.github.com/users/{user}/repos"

# reading token from .txt file
with open('token.txt') as f:
    content = f.readlines()
git_token = content[0].strip()

def get_soup(url):
    res = requests.get(url)
    if res.status_code == 200:
        html_code = res.text
        soup = BeautifulSoup(html_code,"html.parser")
        return soup
    else:
        return None

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

def get_json(user):
    url_user = url_API.format(user=user)
    res = requests.get(url_user, headers={"Authorization": git_token})
    if res.status_code == 200:
        return res.json()[0] # return json element
    else:
        return None

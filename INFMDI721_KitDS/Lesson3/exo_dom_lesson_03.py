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

def get_json_list(user):
    url_user = url_API.format(user=user)
    res = requests.get(url_user, headers={"Authorization": 'token ' + git_token})
    if res.status_code == 200:
        return res.json() # return json element
    else:
        return None

def get_stars_mean(json_list):
    stars = 0
    for json_element in json_list:
        stars = stars + json_element['stargazers_count']
    if len(json_list) is not 0:
        return stars/len(json_list)
    else:
        return 0

users = get_list_of_users(url)

users_mean_ratings = {}
for user in users:
    json_list = get_json_list(user)
    mean_rating = get_stars_mean(json_list)
    users_mean_ratings[user] = mean_rating
print(users_mean_ratings)

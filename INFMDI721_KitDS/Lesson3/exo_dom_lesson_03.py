# coding: utf-8
import requests
from bs4 import BeautifulSoup
import pandas as pd
import time

start_time = time.time()

url = "https://gist.github.com/paulmillr/2657075"
url_API = "https://api.github.com/users/{user}/repos?page={page}&per_page=100"

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
        print("Error: HTML code not available")
        print("Status code: " + res.status_code)
        return None

def get_list_of_users(url):
    lst_users = []
    soup = get_soup(url)
    if soup is not None:
        lst_html = soup.find_all("tr")
        for i in range(1,256+1):
            row = lst_html[i].text
            user = row.split(' ')[2]
            lst_users.append(user)
    return lst_users

def get_json_list(user):
    json_list =[]
    page = 1
    stop = False # boolean to keep track of last page of repos
    while not stop:
        url_user = url_API.format(user=user, page=page)
        res = requests.get(url_user, headers={"Authorization": 'token ' + \
                                               git_token})
        if res.status_code == 200:
            current_page_list = res.json()
            # add json list for this page
            json_list = json_list + current_page_list
            if len(current_page_list) < 100: # if we reach last repo, stop process
                stop = True
            page = page + 1
        else:
            print("Error: json list not available for user:" + user)
            print("Status code: " + res.status_code)
    return json_list

def get_tot_stars(json_list):
    stars = 0
    for json_element in json_list:
        stars = stars + json_element['stargazers_count']
    return stars


users = get_list_of_users(url)

users_dict = {}
for user in users:
    json_list = get_json_list(user)
    n_repos = len(json_list)
    stars = get_tot_stars(json_list)
    if n_repos != 0:
        stars_mean = round(stars/n_repos, 2)
    else:
        stars_mean = 0
    users_dict[user] = {'tot_stars' : stars,
                        '#_repos' : n_repos,
                        'mean_rating' : stars_mean}

df = pd.DataFrame.from_dict(users_dict, orient='index',
                            columns=['tot_stars','#_repos','mean_rating'])
df.reset_index(level=0, inplace=True)
df.rename(columns={'index': 'user'}, inplace=True)

sorted_df = df.sort_values(by='mean_rating', ascending=False)

execution_time = (time.time() - start_time) # time in seconds
print(sorted_df)
print("Total time: " + str(execution_time/60) + "min")

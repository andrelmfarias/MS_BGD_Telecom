# coding: utf-8
import pandas as pd
import requests
from bs4 import BeautifulSoup
import multiprocessing as mp
import time


start_time = time.time()

url = "https://gist.github.com/paulmillr/2657075"
url_API = "https://api.github.com/users/{user}/repos?page={page}&per_page=100"

# reading token from .txt file
with open('token.txt') as f:
    content = f.readlines()
git_token = content[0].strip()
headers =  {'Authorization': 'token {}'.format(git_token)}

def get_soup(url):
    res = requests.get(url)
    if res.status_code == 200:
        html_code = res.text
        soup = BeautifulSoup(html_code, "html.parser")
        return soup
    else:
        print("Error: HTML code not available")
        print("Status code: " + str(res.status_code))
        return None

def get_list_of_users(url):
    lst_users = []
    soup = get_soup(url)
    if soup is not None:
        lst_html = soup.find_all("tr")
        for i in range(1, 256+1):
            row = lst_html[i].text
            user = row.split(' ')[2]
            lst_users.append(user)
    return lst_users

def request_json(user,page):
    url_user = url_API.format(user=user, page=page)
    while True:
        res = requests.get(url_user, headers=headers)
        sts_code = res.status_code
        if sts_code == 200:
            break
        time.sleep(1)
    return res.json()

def get_json_list(user):
    json_list =[]
    page = 1
    stop = False # boolean to keep track of last page of repos
    while not stop:
        current_page_list = request_json(user,page)
        json_list = json_list + current_page_list
        if len(current_page_list) < 100: # if we reach last repo, stop process
            stop = True
        page = page + 1

    return json_list

def get_tot_stars(json_list):
    stars = 0
    for json_element in json_list:
        stars = stars + json_element['stargazers_count']
    return stars

def insert_info_in_dict(user,users_dict):
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

users = get_list_of_users(url)

jobs = []
manager = mp.Manager()
users_dict = manager.dict()
pr_tracker = 0 # tracker to avoid launching all processes at once
for user in users[:5]:
    p = mp.Process(target=insert_info_in_dict,args=(user,users_dict))
    jobs.append(p)
    p.start()
    pr_tracker = (pr_tracker+1) % 5 # for each group of 5 processes we wait
    if pr_tracker == 0:
        for prc in jobs:
            prc.join()
        jobs = [] # reinitiate jobs list

# applying join to the remaining process in job list
for p in jobs:
    p.join()

df = pd.DataFrame.from_dict(users_dict, orient='index',
                            columns=['tot_stars','#_repos','mean_rating'])
df.reset_index(level=0, inplace=True)
df.rename(columns={'index': 'user'}, inplace=True)

sorted_df = df.sort_values(by='mean_rating', ascending=False)
print(sorted_df)

execution_time = (time.time() - start_time) # time in seconds
exec_time_min = round(execution_time/60,1)
print("Total time: " + str(exec_time_min) + "min")

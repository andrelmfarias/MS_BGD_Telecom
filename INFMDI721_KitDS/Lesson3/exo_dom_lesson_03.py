# coding: utf-8
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
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
headers =  {'Authorization': 'token {}'.format(git_token)} # to use in request

def get_soup(url):

    ''' return html_code from url if status_code == 200 and return None if not'''

    res = requests.get(url)
    if res.status_code == 200: # if request is effectivelly done
        html_code = res.text
        soup = BeautifulSoup(html_code, "html.parser")
        return soup
    else:
        print("Error: HTML code not available")
        print("Status code: " + str(res.status_code))
        return None

def get_list_of_users(url):

    ''' # get list of users from github page '''

    lst_users = []
    soup = get_soup(url)
    if soup is not None:
        lst_html = soup.find_all("tr")
        for i in range(1, 256+1):
            row = lst_html[i].text
            user = row.split(' ')[2]
            lst_users.append(user)
    return lst_users

def request_json(user,page,t):
    '''
     Return list of json elements for user on the requested page
     t is a list that tracks the timing for each completed request

     It works as a loop that keep requesting until the request is good
     (status_code == 200). The loop is forced to stop after 3min of requesting
     without goog result.
    '''

    url_user = url_API.format(user=user, page=page)
    t_request = 0 # initiate time counter
    while t_request is not 180: # while we do not spend 3min doing the request, we keep doing it
        res = requests.get(url_user, headers=headers)
        sts_code = res.status_code
        if sts_code == 200:
            t.append(time.time()) # append timing of request to t list
            return res.json() # if status_code is ok return json list and get out of loop
        else:
            time.sleep(1) # wait 1 sec if request fails and request again
            t_request += 1
    # if we reach 3min return None
    return None

def get_json_list(user,t):
    '''
    Return list of json elements for all pages of user
    t t is a list that tracks the timing for each completed page request
    '''

    json_list =[]
    page = 1
    stop = False # boolean to keep track of last page of repos
    while not stop:
        current_page_list = request_json(user,page,t)
        json_list = json_list + current_page_list
        if len(current_page_list) < 100: # if we reach last repo, stop process
            stop = True
        page = page + 1 # goes to next page
    print("Request completed for user: " + user)
    return json_list

def get_tot_stars(json_list):
    stars = 0
    for json_element in json_list:
        stars = stars + json_element['stargazers_count']
    return stars

def insert_info_in_dict(user,users_dict,t):
    '''
    Insert information (rating, mean_rating, user, etc...) in dictionary
    passed as argument in order to parallelize using Multiprocessing.Process
    '''

    json_list = get_json_list(user,t)
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
users_dict = manager.dict() # dictionary that keeps all the results
t = manager.list() # list that tracks timing of requests for plotting
for user in users:
    p = mp.Process(target=insert_info_in_dict,args=(user,users_dict,t))
    jobs.append(p)
    p.start()

# waiting for all processes to finish
for p in jobs:
    p.join()

df = pd.DataFrame.from_dict(users_dict, orient='index',
                            columns=['tot_stars','#_repos','mean_rating'])
sorted_df = df.sort_values(by='mean_rating', ascending=False)
sorted_df.reset_index(level=0, inplace=True)
sorted_df.rename(columns={'index': 'user'}, inplace=True)
print("\nList of users sorted by Rating Mean: ")
print(sorted_df)

execution_time = (time.time() - start_time) # time in seconds
exec_time_min = round(execution_time/60,1)
print("\nTotal execution time: " + str(exec_time_min) + "min")

# Plotting
sns.set_style('darkgrid')
t = list(t)
n_request = len(t)
rqst_lst = range(n_request)
pct_request = list(map(lambda x: x/len(t), rqst_lst))
t_rebased = list(map(lambda x: x - t[0], t))

plt.figure(figsize=(12,8))
plt.plot(t_rebased, pct_request)
plt.xlabel("Time in seconds")
plt.ylabel("% of requests done")
plt.title("Evolution of requests done")
plt.show()

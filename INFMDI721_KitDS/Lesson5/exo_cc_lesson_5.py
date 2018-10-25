import pandas as pd
import numpy as np
import requests
from bs4 import BeautifulSoup
import re

url = "https://open-medicaments.fr/api/v1/medicaments?query=paracetamol&page={page}&limit=100"

df = pd.DataFrame()

page = 1
while True:
    df_tmp = pd.read_json(url.format(page=page))
    if len(df_tmp) == 0:
        break
    # print(df_tmp)
    df = pd.concat([df, df_tmp], axis=0)
    page += 1

df.reset_index(drop=True, inplace=True)

serie = df['denomination']
reg = r'([\D]*)(\d+)(.*), ([\w\s]*)'
df = serie.str.extract(reg)

df['multiplicator'] = 1000
df['multiplicator'] = df["multiplicator"].where(df[2].str.strip() == "g", 1)

print(df)

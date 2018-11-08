import pandas as pd
import re
import matplotlib.pyplot as plt

# Reading xls files to DataFrame
df_pop = pd.read_excel('population.xls', sheet_name='2017', header=[3, 4])
df_hon = pd.read_excel('Honoraires_2016.xls', sheet_name='Spécialistes')

# Preprocessing df_pop
df_pop.dropna(inplace=True)
df_pop.index.names = ['Dep. Code']
df_pop.columns.names = [None, None]
df_pop.rename(columns=dict(zip(df_pop.columns[0], ['Départments', 'Dép.'])), inplace=True)
df_pop.index = df_pop.index.map(str)

# Preprocessing df_hon
regex = r'([\d|\w]+)(?:-)(.*)'
df_hon[['Code Dep.', 'DEPARTEMENT']] = df_hon['DEPARTEMENT'].str.extract(regex)
df_hon['Spécialistes'] = df_hon['Spécialistes'].str.extract(r'(\d+)(?:-)(.*)')[1]
df_hon.dropna(inplace=True)
# reording columns
cols = df_hon.columns.tolist()
cols = [cols[-1]]+[cols[1]]+[cols[0]]+cols[2:-1]
df_hon = df_hon[cols]
col = 'DEPASSEMENTS (Euros)'
df_hon[col] = df_hon[col].replace('nc', 0)

# grouping by Code Dep. in order to get the totality of 'Depassement' per department
df_hon_grouped = df_hon.groupby('Code Dep.').sum()

# concatenating DFs to get final analysis
df_concat = pd.concat([df_hon_grouped, df_pop['Ensemble']['Total']], axis=1, sort=True)
df_concat.dropna(inplace=True)
df_concat.rename({'Total': 'Population'}, axis=1, inplace=True)
df_concat.index.names = ['Dep. Code']
# Computing 'Depassement ratio'
df_concat['Depassement ratio'] = df_concat['DEPASSEMENTS (Euros)'] / df_concat['EFFECTIFS']
df_concat['Doctor density'] = df_concat['EFFECTIFS'] / df_concat['Population']

# Plotting results
plt.figure(figsize=(10, 6))
plt.plot(df_concat['Doctor density'], df_concat['Depassement ratio'],
         linestyle='', marker='o', color='b')
plt.xlabel('Doctor density per department')
plt.ylabel('Depassement ratio (Euros per doctor)')
plt.title("Relation between 'depassement' ratio and doctor density per department")
plt.show()

import pandas as pd
import re

# Reading xls files to DataFrame
df_pop = pd.read_excel('population.xls', sheet_name='2017', header=[3, 4])
df_hon = pd.read_excel('Honoraires_2016.xls', sheet_name='Spécialistes')

# Preprocessing df_pop
df_pop.dropna(inplace=True)
df_pop.index.names = ['Dep. Code']
df_pop.columns.names = [None, None]
df_pop.rename(columns=dict(zip(df_pop.columns[0], ['Départments', 'Dép.'])), inplace=True)

# Preprocessing df_hon
df_hon[['Code Dep.', 'DEPARTEMENT']] = df_hon['DEPARTEMENT'].str.extract(r'(\d+)(?:-)(.*)')
df_hon['Spécialistes'] = df_hon['Spécialistes'].str.extract(r'(\d+)(?:-)(.*)')[1]
df_hon.dropna(inplace=True)
# reording columns
cols = df_hon.columns.tolist()
cols = [cols[-1]]+[cols[1]]+[cols[0]]+cols[2:-1]
df_hon = df_hon[cols]

col = 'HONORAIRES SANS DEPASSEMENT (Euros)'
df_hon[col] = df_hon[col].replace('nc', 0)
col = 'DEPASSEMENTS (Euros)'
df_hon[col] = df_hon[col].replace('nc', 0)


df_hon_grouped = df_hon.groupby(['Code Dep.', 'DEPARTEMENT']).sum()

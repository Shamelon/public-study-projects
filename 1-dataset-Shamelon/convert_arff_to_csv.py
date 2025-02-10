import pandas as pd
import numpy as np
import arff

def read_arff(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        data = arff.load(f)

    df = pd.DataFrame(data['data'], columns=[attr[0] for attr in data['attributes']])

    # Приведение типов
    for i, (attr_name, attr_type) in enumerate(data['attributes']):
        if attr_type == 'INTEGER':
            df[attr_name] = df[attr_name].astype('Int64')
        elif attr_type == 'REAL':
            df[attr_name] = df[attr_name].astype('float64')

    return df

def replace_missing_values(dataframe):
    for column in dataframe.columns:
        # Если колонка числовая, заменяем NaN на среднее значение
        if pd.api.types.is_numeric_dtype(dataframe[column]):
            mean_value = dataframe[column].mean()
            if dataframe[column].dtype == 'Int64':
                mean_value = int(mean_value)
            dataframe.fillna({column:mean_value}, inplace=True)
        else:
            # Для строковых колонок заменяем '?' на 'unknown'
            dataframe.replace('?', {column:'unknown'}, inplace=True)
    return dataframe

def categories_into_numeric(dataframe, target_category):
    non_target_columns = dataframe.columns[dataframe.columns != target_category]

    for column in non_target_columns:
        if not pd.api.types.is_numeric_dtype(dataframe[column]):
            dataframe[column], _ = pd.factorize(dataframe[column])

    return dataframe


def normalize_data(dataframe):
    numeric_cols = dataframe.select_dtypes(include=['float64', 'int64']).columns

    # Применяем z-нормализацию
    for col in numeric_cols:
        mean = dataframe[col].mean()
        std = dataframe[col].std()
        dataframe[col] = (dataframe[col] - mean) / std

    return dataframe

def write_csv(dataframe, file):
    dataframe.to_csv(file, index=False, encoding='utf-8')

df = read_arff("data.arff")

df = replace_missing_values(df)

df = categories_into_numeric(df, target_category='genre')

df = normalize_data(df)

write_csv(df, 'data.csv')

# Вывод результата
print(df)

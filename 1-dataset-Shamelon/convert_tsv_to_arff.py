import pandas as pd
import typing as tp

column_types = {
    'id': 'integer',
    'title': 'string',
    'rating': 'numeric',
    'number_of_ratings': 'integer',
    'country': 'string',
    'genre': '{драма,комедия,боевик,фантастика,ужасы,мелодрама,триллер,приключения,документальный,аниме,криминал,история,фэнтези,спорт,мультфильм,детский,музыка,военный,детектив,биография,мюзикл,реальноеТВ,игра}',
    'director': 'string',
    'age_rate': '{0+,6+,12+,16+,18+}',
    'year': 'integer',
    'review_count': 'integer',
    'duration_minutes': 'integer'
}

def make_description():
    return """% 1.Title: Kinopoisk serials database
%
% 2.Sources:
%   (a) Creator: Vysotin Danil
%   (b) Date: October, 2024
%
"""


def normalize(value: tp.Any, type_: str):
    if value is None or value == '' or value == '—' or value == float('nan'):
        return '?'
    if type_ == 'string':
        return f"'{str(value)}'"
    return str(value)


def tsv_to_arff(tsv_file, arff_file, relation_name):
    df = pd.read_csv(tsv_file, sep='\t', encoding='utf-8')

    # Открытие ARFF файла для записи
    with open(arff_file, 'w', encoding='utf-8') as f:
        # Запись описания
        f.write(make_description())

        # Запись заголовка
        f.write(f"@RELATION {relation_name}\n\n")

        # Запись атрибутов
        for column in df.columns:
            f.write(f"@ATTRIBUTE {column} {column_types[column]}\n")  # Предполагаем, что все атрибуты строковые

        f.write("\n@DATA\n")

        # Запись данных
        for index, row in df.iterrows():
            f.write(','.join([normalize(value, column_types[df.columns[i]]) for i, value in enumerate(row)]) + '\n')


# Пример использования
tsv_to_arff('data.tsv', 'data.arff', 'kinopoisk')

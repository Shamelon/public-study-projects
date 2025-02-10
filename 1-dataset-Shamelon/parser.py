import csv
from email.header import make_header
from random import uniform

from selenium import webdriver
from selenium.common import NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
import time

from selenium.webdriver.remote.webelement import WebElement


def sleep():
    time.sleep(uniform(4, 5))


def extract_text_by_class(block: WebElement, class_: str) -> str:
    try:
        return block.find_element(By.CLASS_NAME, class_).text
    except NoSuchElementException:
        return "—"


def extract_country_genre_director(text: str) -> tuple[str, str, str]:
    words = text.split()
    dot_index = words.index("•")
    director_index = words.index('Режиссёр:')
    return ' '.join(words[:dot_index]), ''.join(words[dot_index + 1:director_index]), ' '.join(
        words[director_index + 1:])


def parse_block(block: WebElement) -> list[str]:
    title_class = 'styles_mainTitle__IFQyZ'
    title = extract_text_by_class(block, title_class)

    rating_class = 'styles_kinopoiskValue__nkZEC'
    rating = extract_text_by_class(block, rating_class)

    number_of_ratings_class = 'styles_kinopoiskCount__PT7ZX'
    number_of_ratings = extract_text_by_class(block, number_of_ratings_class)
    if number_of_ratings.endswith('K'):
        number_of_ratings = number_of_ratings.replace('K', ' 000')
    number_of_ratings = number_of_ratings.replace(' ', '')

    country_class = 'desktop-list-main-info_truncatedText__IMQRP'
    country, genre, director = extract_country_genre_director(extract_text_by_class(block, country_class))

    return [title, rating, number_of_ratings, country, genre, director]


def parse_inside(driver: webdriver.Chrome, block: WebElement) -> list[str]:
    link_class = 'base-movie-main-info_link__YwtP1'

    # Переход внутрь
    try:
        link = block.find_element(By.CLASS_NAME, link_class)
        link_url = link.get_attribute('href')
        driver.execute_script("window.open(arguments[0]);", link_url)
        driver.switch_to.window(driver.window_handles[1])
    except NoSuchElementException:
        return []

    # Ожидание загрузки страницы
    sleep()

    try:
        page_block_class = 'styles_root__2kxYy'
        page_block = driver.find_element(By.CLASS_NAME, page_block_class)
    except NoSuchElementException:
        driver.back()
        sleep()
        return []

    age_rate_class = 'styles_ageRate__340KC'
    age_rate = extract_text_by_class(page_block, age_rate_class)

    year = extract_year(page_block)

    review_count_class = 'styles_reviewCountDark__5LMtp'
    review_count = extract_text_by_class(page_block, review_count_class)

    duration = extract_duration(page_block)

    if review_count:
        review_count = review_count.split()[0]

    # Возврат
    driver.close()
    driver.switch_to.window(driver.window_handles[0])

    return [age_rate, year, review_count, duration]


def extract_year(block: WebElement) -> str:
    try:
        name = 'Год производства'
        block1 = block.find_element(By.XPATH, f"//div[contains(text(), '{name}')]")

        parent = block1.find_element(By.XPATH, '..')

        link_class = 'styles_linkLight__cha3C'
        links = parent.find_elements(By.CLASS_NAME, link_class)
        for link in links:
            if link.text.isnumeric():
                return link.text
    except NoSuchElementException:
        return ""


def extract_duration(block: WebElement) -> str:
    try:
        name = 'Время серии'
        block1 = block.find_element(By.XPATH, f"//div[contains(text(), '{name}')]")

        parent = block1.find_element(By.XPATH, '..')

        text_class = 'styles_valueLight__nAaO3'
        text = parent.find_element(By.CLASS_NAME, text_class).text
        parts = text.split()
        if 'ч' in parts:
            if len(parts) == 4:
                return str(int(parts[0]) * 60 + int(parts[2]))
            else:
                return str(int(parts[0]) * 60)
        return parts[0]
    except NoSuchElementException:
        return ""


def make_header():
    return [
        'id',
        'title',
        'rating',
        'number_of_ratings',
        'country',
        'genre',
        'director',
        'age_rate',
        'year',
        'review_count',
        'duration_minutes'
    ]


def crawler(driver: webdriver.Chrome) -> None:
    with open('data.tsv', 'w', newline='', encoding='utf-8') as file:
        writer = csv.writer(file, delimiter='\t')
        writer.writerow(make_header())
        id_ = 1
        while True:
            # Сбор блоков с информацией о сериалах
            blocks = driver.find_elements(By.CLASS_NAME, 'styles_root__ti07r')

            # Проходка по сериалам на текущей странице
            for block in blocks:
                try:
                    info = [id_] + parse_block(block)
                    info += parse_inside(driver, block)
                    writer.writerow(info)
                    print(info)
                except Exception as e:
                    print(e.__str__())
                id_ += 1
                if id_ > 1000:
                    break
            if id_ > 1000:
                break
            # Ищем кнопку далее
            try:
                link = driver.find_element(By.CLASS_NAME, 'styles_end__aEsmB')
                link.click()
                sleep()
            except NoSuchElementException:
                break


# Настройка опций для браузера
chrome_options = Options()
chrome_options.add_argument(
    "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")

service = Service()
driver = webdriver.Chrome(service=service, options=chrome_options)

try:
    # Открытие сайта
    url = 'https://www.kinopoisk.ru/lists/movies/?b=series&ss_subscription=ANY'
    driver.get(url)
    sleep()
    crawler(driver)
finally:
    driver.quit()

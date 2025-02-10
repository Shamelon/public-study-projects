import os
import typing as tp
import aiohttp
from googlesearch import search
from requests import HTTPError

from dotenv import load_dotenv

load_dotenv('secrets.env')

KINOPOISK_API_TOKEN = os.getenv("KINOPOISK_API_TOKEN")


class SearcherException(Exception):
    pass


class Searcher:

    def __init__(self):
        self.films = None
        self.curr_film = None
        self.links = None

    async def search(self, name: str) -> str:
        self.films = await self.search_film(name)
        return self.make_films_pull()

    @staticmethod
    async def search_film(name: str, number_of_results: int = 5) -> list[dict[str, tp.Any]]:
        # URL для поиска фильма
        url = "https://api.kinopoisk.dev/v1.4/movie/search"

        # Параметры запроса
        params = {
            'query': name,
            'token': KINOPOISK_API_TOKEN,
            'limit': number_of_results
        }

        async with aiohttp.ClientSession() as session:
            # Отправка GET-запроса
            async with session.get(url, params=params) as response:
                if response.status != 200:
                    print(f"Ошибка: {response.status}")
                    return []

                data = await response.json()
                if not data.get('docs'):
                    print("Фильмы не найдены.")
                    return []

                return data['docs']

    @staticmethod
    async def get_full_info(film_id: int) -> dict[str, tp.Any]:
        # URL для поиска фильма
        url = f"https://api.kinopoisk.dev/v1.4/movie/{film_id}"

        # Параметры запроса
        params = {
            'token': KINOPOISK_API_TOKEN
        }

        async with aiohttp.ClientSession() as session:
            # Отправка GET-запроса
            async with session.get(url, params=params) as response:
                if response.status != 200:
                    print(f"Ошибка: {response.status}")
                    return {}

                data = await response.json()
                return data

    async def get_google_links(self):
        try:
            query = f"{self.make_title(self.curr_film)} смотреть онлайн бесплатно"
            urls = search(query, num_results=7, lang="ru")
            links = [{'name': '', 'url': link} for link in urls]
            return links
        except HTTPError:
            raise SearcherException("Слишком много запросов")

    @staticmethod
    def make_title(film_info: dict[str, tp.Any]) -> str:
        return f"{film_info['name'] if film_info['name'] else film_info['alternativeName']} ({film_info['year']})"

    def make_films_pull(self) -> str:
        if not self.films:
            return "Ничего не найдено"
        return "Введите номер:\n" + "\n".join([f"{i + 1}. {self.make_title(film_info)}"
                                               for i, film_info in enumerate(self.films)])

    def make_info(self) -> str:
        film = self.curr_film
        info = (
            f"<b>{self.make_title(film).upper()}</b>\n\n"
            f"Рейтинг Кинопоиск: {round(film.get('rating', {}).get('kp', 'неизвестен'), 1)}\n"
            f"Рейтинг Imdb: {round(film.get('rating', {}).get('imdb', 'неизвестен'), 1)}\n"
            f"Жанр: {', '.join(genre['name'] for genre in film.get('genres', []))}\n"
            f"{'Средняя продолжительность серии: ' + str(film.get('seriesLength', '-')) if film.get('isSeries') else
                'Длительность: ' + str(film.get('movieLength', 'неизвестна')) + ' мин'}\n\n"
            f"{film.get('shortDescription', '')}\n"
            f"Где глянуть: {'\n' + self.make_links_pull(5) if self.links else 'не найдено'}"
        )

        return info

    def make_links_pull(self, number_of_links: int) -> str:
        pull = []
        i = 1
        for link in self.links:
            pull.append(f"  {i}) <a href='{link['url']}'>{link['name'] if link['name'] else "ссылка"}</a>")
            if i == number_of_links:
                break
            i += 1
        return '\n'.join(pull)

    async def get_film_info(self, film_order: int) -> str:
        self.curr_film = await self.get_full_info(self.films[film_order]['id'])
        self.links = self.curr_film['watchability']['items']
        if not self.links or len(self.links) < 3:
            try:
                self.links += await self.get_google_links()
            except SearcherException:
                pass
        return self.make_info()

    def get_film_id(self):
        return self.curr_film['id']

    def get_poster_url(self):
        return self.curr_film.get('poster', '').get('url', '')

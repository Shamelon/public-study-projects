import sqlite3


class HistoryDatabase:
    def __init__(self):
        self.database_link = 'database/history.db'
        self.create_database()

    # Создание подключения к базе данных и таблицы

    def create_database(self):
        conn = sqlite3.connect(self.database_link)
        cursor = conn.cursor()

        # Создание таблицы Films
        cursor.execute('''
                    CREATE TABLE IF NOT EXISTS Films (
                        film_id INTEGER PRIMARY KEY,
                        title TEXT NOT NULL
                    )
                ''')

        # Создание таблицы History с внешним ключом на film_id из таблицы Films
        cursor.execute('''
                    CREATE TABLE IF NOT EXISTS History (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        film_id INTEGER NOT NULL,
                        time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (film_id) REFERENCES Films(film_id)
                    )
                ''')

        conn.commit()
        conn.close()

    # Метод для добавления события в историю
    def add_event(self, user_id, film_id, title):
        conn = sqlite3.connect(self.database_link)
        cursor = conn.cursor()
        cursor.execute('''
            INSERT INTO History (user_id, film_id) VALUES (?, ?)
        ''', (user_id, film_id))
        cursor.execute('''
            INSERT OR IGNORE INTO Films (film_id, title) VALUES (?, ?)
        ''', (film_id, title))

        conn.commit()
        conn.close()

    # Метод для вывода истории поиска для конкретного user_id (последние 20 запросов)
    def get_search_history(self, user_id):
        conn = sqlite3.connect(self.database_link)
        cursor = conn.cursor()
        cursor.execute('''
                SELECT H.time, F.title
                FROM History H
                JOIN Films F ON H.film_id = F.film_id
                WHERE H.user_id = ?
                ORDER BY H.time DESC
                LIMIT 20
            ''', (user_id,))
        results = cursor.fetchall()
        conn.close()

        if not results:
            return "Ваша история еще не началась"
        formatted_history = []
        for record in results:
            time_str = record[0]
            title = record[1]
            formatted_history.append(f"{time_str} {title}")
        return "История поиска:\n\n" + '\n'.join(formatted_history)

    # Метод для получения статистики поиска по пользователю
    def get_search_statistics(self, user_id):
        conn = sqlite3.connect(self.database_link)
        cursor = conn.cursor()

        # SQL-запрос для получения статистики поиска
        cursor.execute('''
            SELECT F.title, COUNT(H.film_id) AS search_count
            FROM History H
            JOIN Films F ON H.film_id = F.film_id
            WHERE H.user_id = ?
            GROUP BY H.film_id
            ORDER BY search_count DESC
        ''', (user_id,))

        stats_records = cursor.fetchall()
        conn.close()

        if not stats_records:
            return "Статистика пуста. Попробуйте что-нибудь поискать"

        # Форматирование результатов
        formatted_stats = []
        for record in stats_records:
            title = record[0]  # Название фильма
            count = record[1]  # Количество запросов
            formatted_stats.append(f"  {title} - {count} раз")

        return 'Вы искали:\n\n' + '\n'.join(formatted_stats)

        # Метод для удаления всех таблиц

    def drop_all_tables(self):
        conn = sqlite3.connect(self.database_link)
        cursor = conn.cursor()

        # Удаление таблицы History
        cursor.execute('DROP TABLE IF EXISTS History')

        # Удаление таблицы Films
        cursor.execute('DROP TABLE IF EXISTS Films')

        conn.commit()
        conn.close()

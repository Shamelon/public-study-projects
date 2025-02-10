import asyncio
import logging
import sys
import os

from aiogram import Bot, Router, Dispatcher
from aiogram.client.default import DefaultBotProperties
from aiogram.enums import ParseMode
from aiogram.filters import CommandStart, Command
from aiogram.fsm.context import FSMContext
from aiogram.fsm.state import State, StatesGroup
from aiogram.types import Message, KeyboardButton, ReplyKeyboardMarkup, BotCommand

from searcher import Searcher
from database.database import HistoryDatabase
from lexicon.lexicon_ru import LEXICON_COMMANDS_RU

from dotenv import load_dotenv

load_dotenv('secrets.env')

TELEGRAM_TOKEN = os.getenv("TELEGRAM_TOKEN")

CHANGE_NUMBER_REQUEST = "Ввести другой номер"
CHANGE_SEARCH_REQUEST = "Изменить поисковый запрос"

form_router = Router()

bot = Bot(token=TELEGRAM_TOKEN, default=DefaultBotProperties(parse_mode=ParseMode.HTML))

searcher = Searcher()
database = HistoryDatabase()


class Form(StatesGroup):
    searching_film = State()
    choosing_film = State()
    changing_opinion = State()


async def start_search(message: Message, state: FSMContext):
    await state.set_state(Form.searching_film)
    await message.answer("Введите название фильма или сериала")


async def start_choosing(message: Message, state: FSMContext):
    await state.set_state(Form.choosing_film)
    await message.answer(text=searcher.make_films_pull(),
                         reply_markup=build_number_keyboard(len(searcher.films)))
    if not searcher.films:
        await state.set_state(Form.searching_film)
    else:
        await state.set_state(Form.choosing_film)


def build_number_keyboard(n) -> ReplyKeyboardMarkup:
    kb_list = [[KeyboardButton(text=str(j + 3 * i)) for j in range(1, min(n - 3 * i + 1, 4))] for i in
               range(n // 3 + 1)]
    return ReplyKeyboardMarkup(keyboard=kb_list, one_time_keyboard=True)


async def set_main_menu():
    main_menu_commands = [
        BotCommand(
            command=command,
            description=description
        ) for command, description in LEXICON_COMMANDS_RU.items()
    ]
    await bot.set_my_commands(main_menu_commands)


@form_router.message(CommandStart())
async def command_start(message: Message, state: FSMContext):
    await start_search(message, state)


@form_router.message(Command('history'))
async def command_history(message: Message):
    await message.answer(text=database.get_search_history(message.from_user.id))


@form_router.message(Command('stats'))
async def command_stats(message: Message):
    await message.answer(text=database.get_search_statistics(message.from_user.id))


@form_router.message(Command('help'))
async def command_help(message: Message):
    text = """Бот для поиска фильмов и сериалов
Введите команду /start. После чего выберите нужный вам вариант (обычно первый) и наслаждайтесь просмотром!

Список доступных команд:

"""
    text += '\n'.join([f"{name} {desc}" for name, desc in LEXICON_COMMANDS_RU.items()])
    await message.answer(text=text)


@form_router.message(Form.choosing_film)
async def process_choosing_film(message: Message, state: FSMContext):
    msg = await message.answer("Загрузка...")
    try:
        user_input = int(message.text)
        number_of_films = len(searcher.films)

        if 1 <= user_input <= number_of_films:
            info = await searcher.get_film_info(user_input - 1)

            keyboard = ReplyKeyboardMarkup(keyboard=[[KeyboardButton(text=CHANGE_NUMBER_REQUEST)],
                                                     [KeyboardButton(text=CHANGE_SEARCH_REQUEST)]],
                                           one_time_keyboard=True)
            await state.set_state(Form.changing_opinion)
            poster = searcher.get_poster_url()
            await bot.delete_message(chat_id=message.from_user.id, message_id=msg.message_id)
            if poster:
                await message.answer_photo(caption=info, photo=searcher.get_poster_url(), reply_markup=keyboard,
                                           parse_mode=ParseMode.HTML)
            else:
                await message.answer(text=info, reply_markup=keyboard, parse_mode=ParseMode.HTML)
            database.add_event(message.from_user.id, searcher.get_film_id(), searcher.make_title(searcher.curr_film))
        else:
            await message.answer(f"Пожалуйста, введите число от 1 до {number_of_films}.")
    except ValueError:
        await message.answer("Некорректный ввод. Пожалуйста, введите число.")


@form_router.message(Form.changing_opinion)
async def process_changing_opinion(message: Message, state: FSMContext):
    user_input = message.text
    if user_input == CHANGE_NUMBER_REQUEST:
        await start_choosing(message, state)
    elif user_input == CHANGE_SEARCH_REQUEST:
        await start_search(message, state)
    else:
        await state.set_state(Form.searching_film)
        await process_searching(message, state)


@form_router.message(Form.searching_film)
async def process_searching(message: Message, state: FSMContext):
    msg = await message.answer("Пытаемся что-нибудь для вас найти...")
    await searcher.search(message.text)
    await bot.delete_message(chat_id=message.from_user.id, message_id=msg.message_id)
    await start_choosing(message, state)


async def main() -> None:
    dp = Dispatcher()
    dp.include_router(form_router)
    await set_main_menu()
    await dp.start_polling(bot)


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO, stream=sys.stdout)
    asyncio.run(main())

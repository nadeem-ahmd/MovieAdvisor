import requests as rq

THE_LIST_API_BASE = "https://api.list.co.uk/v1/"
THE_LIST_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMjc2ZDA3MjYtY2IxMC00N2ZhLWIzMTktMzUwZGNmNjlhZGE1Iiwia2V5X2lkIjoiYjQ0OTVjMGQtYTFlYS00YTk5LThiMmMtMTgzYzE0NDM3YzFjIiwiaWF0IjoxNTgxMjAwMzYxfQ.4A1NOn9XryMrJ4qXGqHIO3Ca_sDaTj3FH215YmEfLo8"


def the_list_request(query):
    return rq.get(THE_LIST_API_BASE + query, headers={'Authorization': 'Bearer ' + THE_LIST_API_KEY})


TMDB_API_BASE = "https://api.themoviedb.org/3/"
TMDB_API_KEY = "5b25a4d2bb38b7c8b4eaa8ff60b9a33d"


def tmdb_request(query):
    return rq.get(TMDB_API_BASE + query + "?api_key=" + TMDB_API_KEY)

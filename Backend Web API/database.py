import pandas as pd
import pymysql as sql

HOST = "movieadvisor.c83b5ob4gr7t.us-east-1.rds.amazonaws.com"
USER = "admin"
PASSWORD = "apples123"
DB = "public"

GROUPS_TABLE = 'groups'
GROUPS_USERS_TABLE = 'groups_users'

MOVIES_TABLE = 'movies'

USER_RATINGS_TABLE = 'user_ratings'
TRAINING_RATINGS_TABLE = 'training_ratings'

TESTING_RATINGS_BASE_TABLE = 'testing_ratings_base'
TESTING_RATINGS_TRUE_TABLE = 'testing_ratings_true'


def database():
    return sql.connect(host=HOST, user=USER, password=PASSWORD, db=DB)


def get_train_ratings():
    return pd.read_sql_query("SELECT * FROM " + TRAINING_RATINGS_TABLE, database())


def get_test_ratings_base():
    return pd.read_sql_query("SELECT * FROM " + TESTING_RATINGS_BASE_TABLE, database())


def get_test_ratings_true():
    return pd.read_sql_query("SELECT * FROM " + TESTING_RATINGS_TRUE_TABLE, database())


def get_public_ratings(user_id):
    return pd.read_sql_query("SELECT * FROM " + USER_RATINGS_TABLE + " WHERE user_id = '" + str(user_id) + "'", database())


def get_groups_users(group_id):
    return pd.read_sql_query("SELECT * FROM " + GROUPS_USERS_TABLE + " WHERE group_id = '" + group_id + "'", database())


def get_movie_data():
    return pd.read_sql_query("SELECT * FROM " + MOVIES_TABLE, database())


def fetch(query):
    db = database()
    cursor = db.cursor()
    cursor.execute(query)
    db.close()
    return cursor.fetchall()


def execute(query):
    db = database()
    cursor = db.cursor()
    cursor.execute(query)
    db.commit()
    db.close()

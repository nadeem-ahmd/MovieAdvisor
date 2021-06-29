import traceback

import flask as fl
import pandas as pd

import database
import event
import movie

application = fl.Flask(__name__)

movie_recommender = movie.Recommender()
movie_evaluator = movie.Evaluator()

event_recommender = event.Recommender()


@application.route('/error/', methods=['GET'])
def error():
    error_trace = traceback.format_exc()
    print(error_trace)
    return error_trace, 500


@application.route('/', methods=['GET'])
def index():
    return '''
        <h1>MovieAdvisor</h1>
        <a href="/download/">Download latest Android application (4MB)</a>
        '''


@application.route('/user/rating/', methods=['GET'])
def user_rating():
    user_id = fl.request.args.get('user_id')
    try:
        query = "SELECT * FROM " + database.USER_RATINGS_TABLE + " WHERE user_id = '" + user_id + "'"
        return pd.read_sql_query(query, database.database()).to_json(orient='records')
    except:
        return error()


@application.route('/user/rating/get/', methods=['GET'])
def get_user_rating():
    user_id = fl.request.args.get('user_id')
    tmdb_id = fl.request.args.get('tmdb_id')
    try:
        query = "SELECT * FROM " + database.USER_RATINGS_TABLE + " WHERE user_id = '" + user_id + "' AND tmdb_id = '" + tmdb_id + "'"
        return pd.read_sql_query(query, database.database()).to_json(orient='records')
    except:
        return error()


@application.route('/user/rating/set/', methods=['GET'])
def set_user_ratings():
    user_id = fl.request.args.get('user_id')
    tmdb_id = fl.request.args.get('tmdb_id')
    rating = fl.request.args.get('rating')
    try:
        if int(rating) == 0:
            query = "DELETE FROM " + database.USER_RATINGS_TABLE + " WHERE user_id = '" + user_id + "' AND tmdb_id = '" + tmdb_id + "'"
        else:
            if not database.fetch("SELECT * FROM " + database.USER_RATINGS_TABLE + " WHERE user_id = '" + user_id + "' AND tmdb_id = '" + tmdb_id + "'"):
                query = "INSERT INTO " + database.USER_RATINGS_TABLE + " VALUES ('" + user_id + "', '" + tmdb_id + "', " + rating + ")"
            else:
                query = "UPDATE " + database.USER_RATINGS_TABLE + " SET rating = " + rating + " WHERE user_id = '" + user_id + "' AND tmdb_id = '" + tmdb_id + "'"
        database.execute(query)
        return "OK"
    except:
        return error()


@application.route('/user/rating/count/', methods=['GET'])
def count_user_ratings():
    user_id = fl.request.args.get('user_id')
    try:
        return {"count": database.fetch("SELECT COUNT(*) FROM " + database.USER_RATINGS_TABLE + " WHERE user_id = '" + user_id + "'")[0][0]}
    except:
        return error()


@application.route('/user/group/', methods=['GET'])
def user_groups():
    user_id = fl.request.args.get('user_id')
    try:
        return pd.read_sql_query("SELECT * FROM " + database.GROUPS_TABLE + " WHERE group_id IN (SELECT group_id FROM " + database.GROUPS_USERS_TABLE + " WHERE user_id = '" + user_id + "')", database.database()).to_json(orient='records')
    except:
        return error()


@application.route('/user/recommendation/movie/', methods=['GET'])
def individual_recommendation_movie():
    user_id = fl.request.args.get('user_id')
    try:
        return movie_recommender.get_recommendation(user_id).to_json(orient='records')
    except:
        return error()


@application.route('/user/recommendation/event/', methods=['GET'])
def individual_recommendation_events():
    user_id = fl.request.args.get('user_id')
    latitude = fl.request.args.get('latitude')
    longitude = fl.request.args.get('longitude')
    try:
        return event_recommender.get_recommendation(user_id, latitude, longitude)
    except:
        return error()


@application.route('/group/count/', methods=['GET'])
def group_ratings():
    try:
        group_id = str(fl.request.args.get('group_id')).upper()
        return {
            "size": database.fetch("SELECT COUNT(*) FROM " + database.GROUPS_USERS_TABLE + " WHERE group_id = '" + group_id + "'")[0][0],
            "ratings": database.fetch("SELECT COUNT(*) FROM " + database.USER_RATINGS_TABLE + " WHERE user_id IN (SELECT user_id FROM " + database.GROUPS_USERS_TABLE + " WHERE group_id = '" + group_id + "')")[0][0]
        }
    except:
        return error()


@application.route('/group/recommendation/movie/', methods=['GET'])
def group_recommendation_movie():
    consensus = fl.request.args.get('consensus')
    try:
        group_id = str(fl.request.args.get('group_id')).upper()
        return movie_recommender.get_group_recommendation(group_id, consensus).to_json(orient='records')
    except:
        return error()


@application.route('/group/recommendation/event/', methods=['GET'])
def group_recommendation_events():
    latitude = fl.request.args.get('latitude')
    longitude = fl.request.args.get('longitude')
    try:
        group_id = str(fl.request.args.get('group_id')).upper()
        return event_recommender.get_group_recommendation(group_id, latitude, longitude)
    except:
        return error()


@application.route('/user/group/join/', methods=['GET'])
def group_join():
    user_id = fl.request.args.get('user_id')
    try:
        group_id = str(fl.request.args.get('group_id')).upper()
        if not database.fetch("SELECT * FROM " + database.GROUPS_TABLE + " WHERE group_id = '" + group_id + "'"):
            return "no such group"
        if database.fetch("SELECT * FROM " + database.GROUPS_USERS_TABLE + " WHERE group_id = '" + group_id + "' AND user_id = '" + user_id + "'"):
            return "user already in group"
        database.execute("INSERT INTO " + database.GROUPS_USERS_TABLE + " VALUES ('" + group_id + "', '" + user_id + "')")
        return 'done'
    except:
        return error()


@application.route('/user/group/leave/', methods=['GET'])
def group_leave():
    user_id = fl.request.args.get('user_id')
    try:
        group_id = str(fl.request.args.get('group_id')).upper()
        database.execute("DELETE FROM " + database.GROUPS_USERS_TABLE + " WHERE user_id = '" + user_id + "' AND group_id = '" + group_id + "'")
        return "OK"
    except:
        return error()


@application.route('/user/group/create/', methods=['GET'])
def group_create():
    user_id = fl.request.args.get('user_id')
    try:
        group_id = str(fl.request.args.get('group_id')).upper()
        if database.fetch("SELECT * FROM " + database.GROUPS_TABLE + " WHERE group_id = '" + group_id + "'"):
            return "name in use"
        database.execute("INSERT INTO " + database.GROUPS_TABLE + " VALUES ('" + group_id + "')")
        database.execute("INSERT INTO " + database.GROUPS_USERS_TABLE + " VALUES ('" + group_id + "', '" + user_id + "')")
        return "OK"
    except:
        return error()


@application.route('/evaluate/', methods=['GET'])
def evaluate():
    try:
        k_trials = int(fl.request.args.get('k_trials'))
        users = int(fl.request.args.get('users'))
        users, k_recalls, k_precisions, k_fmeasure, k_mae, k_mse = movie_evaluator.evaluate(k_trials, users)
        return fl.render_template('evaluate', users=users, k_trials="2-" + str(k_trials), k_recalls=str(k_recalls), k_precisions=str(k_precisions), k_fmeasure=str(k_fmeasure), k_mae=k_mae, k_mse=k_mse)
    except:
        return error()


@application.route('/download/')
def download():
    try:
        return fl.send_from_directory('', 'MovieAdvisor.apk', as_attachment=True)
    except:
        return error


if __name__ == '__main__':
    application.run()

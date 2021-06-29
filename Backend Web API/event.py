import pandas as pd

import api
import constants
import database


class Recommender:
    def get_recommendation(self, user_id, latitude, longitude, movies=None):
        # Used to pass the movies rated by a group
        if movies is None:
            ratings = database.get_public_ratings(user_id)
            movies = ratings['tmdb_id'].tolist()

        # Get a list of keywords and genres for rated movies
        keywords = self.get_keywords(movies)
        genres = self.get_genres(movies)

        # Identify the top n keywords and genres
        most_common_keywords = self.get_most_common(keywords, constants.EVENT_N)
        most_common_genres = self.get_most_common(genres, constants.EVENT_N)

        # Identify and return events that match top n keywords and genres
        events = self.get_events(most_common_keywords, most_common_genres, latitude, longitude)

        return events

    def get_group_recommendation(self, group_id, latitude, longitude):
        group_ratings = pd.DataFrame(columns=['user_id', 'tmdb_id', 'rating'])

        # Combine and return the ratings by all group members
        for index, row in database.get_groups_users(group_id).iterrows():
            user_id = row['user_id']
            user_ratings = database.get_public_ratings(user_id)
            group_ratings = group_ratings.append(user_ratings)

        movies = set(group_ratings['tmdb_id'].tolist())

        return self.get_recommendation(None, latitude, longitude, movies)

    def get_events(self, keywords, genres, latitude, longitude):
        # Retrieve and return events from TheList API that match the keywords, genres, and location
        keyword_events = {}
        for keyword in keywords:
            response = api.the_list_request("search?query=" + keyword[0] + "&near=" + latitude + "," + longitude + "/" + str(constants.EVENT_MAX_DISTANCE_MILES)).json()
            if response:
                keyword_events[keyword[0]] = response

        genre_events = {}
        for genre in genres:
            response = api.the_list_request("search?query=" + genre[0] + "&near=" + latitude + "," + longitude + "/" + str(constants.EVENT_MAX_DISTANCE_MILES)).json()
            if response:
                genre_events[genre[0]] = response

        return {
            "keywords": keyword_events,
            "genres": genre_events
        }

    def get_keywords(self, movie_ids):
        # Retrieve and return keywords from TMDb API for movies matching the movie ids
        results = []
        for movie_id in movie_ids:
            response = api.tmdb_request("movie/" + str(movie_id) + "/keywords").json()
            for keyword in response['keywords']:
                if self.is_valid(keyword['name']):
                    results.append(keyword['name'])
        return results

    def get_genres(self, movie_ds):
        # Retrieve and return genres from TMDb API for movies matching the movie ids
        results = []
        for movie_id in movie_ds:
            response = api.tmdb_request("movie/" + str(movie_id)).json()
            for genre in response['genres']:
                results.append(genre['name'])
        return results

    def get_most_common(self, ls, n):
        # Return the most common occurring n items from the list
        counter = {}
        for x in ls:
            if x in counter:
                counter[x] = counter[x] + 1
            else:
                counter[x] = 1
        return sorted(counter.items(), key=lambda y: y[1], reverse=True)[:n]

    def is_valid(self, keyword):
        if " " in keyword:
            return False
        return True

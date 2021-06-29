import random

import numpy as np
import pandas as pd
import sklearn.metrics as metrics

import constants
import database


class Recommender:
    def __init__(self, k=constants.MOVIE_K):
        self.k = k

        # Retrieve movie and training ratings from database
        self.movie_data = database.get_movie_data()
        self.train_ratings = database.get_train_ratings()

        # Calculate the average rating for each user
        self.average_ratings = self.train_ratings.groupby(by="user_id", as_index=False)['rating'].mean()
        self.average_ratings.columns = ['user_id', 'average_rating']

        # Create a normalised rating for each rating
        # normalised_rating = rating - average_user_rating
        self.train_ratings = pd.merge(self.train_ratings, self.average_ratings, on='user_id')
        self.train_ratings['normalised_rating'] = self.train_ratings['rating'] - self.train_ratings['average_rating']
        self.train_ratings = self.train_ratings.drop(columns=['average_rating'])

    def get_recommendation(self, user_id, ratings=None):
        # Used to pass the movies rated by a group
        if ratings is None:
            ratings = database.get_public_ratings(user_id)

        # Merge user ratings with movies data to get movie_id from tmdb_id
        ratings = ratings.merge(self.movie_data, on='tmdb_id')
        ratings['user_id'] = user_id
        ratings = ratings[['user_id', 'movie_id', 'rating']]

        # Add the given users average rating alongside each rating
        average_rating = ratings.groupby(by="user_id", as_index=False)['rating'].mean()
        average_rating.columns = ['user_id', 'average_rating']
        ratings = pd.merge(ratings, average_rating, on='user_id')

        # Create a normalised rating for each of the given users ratings
        ratings['normalised_rating'] = ratings['rating'] - ratings['average_rating']
        ratings = ratings.drop(columns=['average_rating'])

        # Add the given users (normalised) ratings to the other user ratings
        ratings = self.train_ratings.append(ratings)

        # Create matrix made up of the normalised ratings
        # user_id x movie_id = normalised_rating
        ratings_matrix_u = pd.pivot_table(ratings, values='normalised_rating', index='user_id', columns='movie_id')

        # Create a version of the ratings matrix with NaN values filled with their row average
        ratings_matrix_f = ratings_matrix_u.fillna(ratings_matrix_u.mean(axis=0))

        # Calculate the pairwise similarity between the given user and every other user
        # user_id1 x user_id2 = cos(user_id1, user_id2)
        user_ratings = [ratings_matrix_f.loc[user_id]]
        cosine_similarity = metrics.pairwise.cosine_similarity(ratings_matrix_f, user_ratings)
        cosine_similarity = pd.DataFrame(cosine_similarity, index=ratings_matrix_f.index)

        # Create list of movies that all users has rated
        movies_rated_by_all = ratings.astype({"movie_id": str}).groupby(by='user_id')['movie_id'].apply(get_comma_separated_str)

        # Create list of movies that the given user has rated
        movies_rated_by_user = ratings_matrix_u.columns[ratings_matrix_u[ratings_matrix_u.index == user_id].notna().any()].tolist()

        # Identify the k-nearest-neighbours for the given user
        neighbours = self.get_k_nearest_neighbours(cosine_similarity, user_id)

        # Create list of movies that neighbours have rated
        movies_rated_by_neighbours = ','.join(movies_rated_by_all[movies_rated_by_all.index.isin(neighbours)].values).split(',')

        # Create a df containing the similarity between the given user and each neighbour
        similarity_to_neighbours = cosine_similarity.loc[neighbours]

        # Create list of movies that have been rated by neighbours but not by the given user
        # movies_not_rated_by_user = movies_rated_by_neighbours - movies_rated_by_user
        movies_not_rated_by_user = list(map(int, (list(set(movies_rated_by_neighbours) - set(list(map(str, movies_rated_by_user)))))))

        average_rating = average_rating.loc[average_rating['user_id'] == user_id, 'average_rating'].values[0]

        # Predict a rating for every movie in movies_not_rated_by_user
        predictions = []
        for movie_id in movies_not_rated_by_user:
            # Create df containing all users normalised ratings for the current movie
            ratings_by_all = ratings_matrix_f.loc[:, movie_id]

            # Create df containing every neighbours normalised ratings for the current movie
            ratings_by_neighbours = ratings_by_all[ratings_by_all.index.isin(neighbours)]

            # Merge every neighbours normalised ratings with their similarity to the given user
            merged = pd.concat([ratings_by_neighbours, similarity_to_neighbours], axis=1)
            merged.columns = ['normalised_rating', 'similarity']

            # Calculate a weighted rating based on the similarity
            # weighted_rating = normalised_rating x similarity
            merged['weighted_rating'] = merged.apply(calculate_weighted_rating, axis=1)

            # Calculate a final prediction for the movie
            weighted_rating_sum = merged['weighted_rating'].sum()
            similarity_sum = merged['similarity'].sum()
            prediction = average_rating + (weighted_rating_sum / similarity_sum)
            predictions.append(prediction)

        # Sort recommendations by their predicted ratings
        recommendations = pd.DataFrame({'movie_id': movies_not_rated_by_user, 'rating': predictions})
        recommendations = recommendations.sort_values(by='rating', ascending=False)

        # Merge the recommendation movies with the movie data to retrieve the tmdb_id
        recommendations = recommendations.merge(self.movie_data, on="movie_id")

        return recommendations[['tmdb_id', 'rating']]

    def get_group_recommendation(self, group_id, consensus):
        # Create a df made up of all group user ratings
        group_ratings = pd.DataFrame(columns=['user_id', 'tmdb_id', 'rating'])

        for index, row in database.get_groups_users(group_id).iterrows():
            user_id = row['user_id']
            user_ratings = database.get_public_ratings(user_id)
            group_ratings = group_ratings.append(user_ratings)

        group_ratings = group_ratings.astype({"rating": int})

        # Apply a consensus-function to create a model of the group
        if consensus == "min":
            consensus_ratings = group_ratings.groupby(by="tmdb_id", as_index=False)['rating'].min()
        elif consensus == "max":
            consensus_ratings = group_ratings.groupby(by="tmdb_id", as_index=False)['rating'].max()
        else:
            consensus_ratings = group_ratings.groupby(by="tmdb_id", as_index=False)['rating'].mean()

        consensus_ratings['user_id'] = -1

        # Return recommendations for the pseudo group
        return self.get_recommendation(-1, consensus_ratings)

    def get_k_nearest_neighbours(self, cosine_similarity, user_id):
        nearest_neighbours = []

        # Sort the cosine similarity matrix in descending order
        cosine_similarity = cosine_similarity.sort_values(by=cosine_similarity.columns[0], ascending=False)

        # Return the top k neighbours
        for index, row in cosine_similarity.iterrows():
            if len(nearest_neighbours) == self.k:
                return nearest_neighbours
            if index != user_id:
                nearest_neighbours.append(index)


class Evaluator:
    def evaluate(self, trails, users):
        k_trials = [i for i in range(2, trails + 1)]

        # Retrieve base and true movie training ratings from the database
        self.base_ratings = database.get_test_ratings_base()
        self.true_ratings = database.get_test_ratings_true()

        # Select a random sample of users
        users_to_evaluate = random.sample(range(1, constants.MOVIE_DATA_SET_USERS + 1), users)

        summary_file = open("output/evaluate_summary.txt", "w")
        summary_file.write("users: " + str(users_to_evaluate) + "\n\n")

        # Calculate the average rating for each user
        average_ratings = self.base_ratings.groupby(by="user_id", as_index=False)['rating'].mean()
        average_ratings.columns = ['user_id', 'average_rating']

        # Create a normalised rating for each rating
        # normalised_rating = rating - average_user_rating
        base_ratings = pd.merge(self.base_ratings, average_ratings, on='user_id')
        base_ratings['normalised_rating'] = base_ratings['rating'] - base_ratings['average_rating']
        base_ratings = base_ratings.drop(columns=['average_rating'])

        # Create matrix made up of the normalised ratings
        # user_id x movie_id = normalised_rating
        ratings_matrix_u = pd.pivot_table(base_ratings, values='normalised_rating', index='user_id', columns='movie_id')

        # Create a version of the ratings matrix with NaN values filled with their row average
        ratings_matrix_f = ratings_matrix_u.fillna(ratings_matrix_u.mean(axis=0))

        # Calculate the pairwise similarity between a given user and every other user
        cosine_similarity = metrics.pairwise.cosine_similarity(ratings_matrix_f)

        # Create a version of the ratings matrix with NaN values filled with their row average
        np.fill_diagonal(cosine_similarity, 0)

        # Calculate the pairwise similarity between a given user and every other user
        cosine_similarity = pd.DataFrame(cosine_similarity, index=ratings_matrix_f.index)
        cosine_similarity.columns = ratings_matrix_f.index

        # Creates list of movies that each user has rated
        rated_by_all = base_ratings.astype({"movie_id": str}).groupby(by='user_id')['movie_id'].apply(get_comma_separated_str)

        k_recalls = []
        k_precisions = []
        k_fmeasure = []
        k_mae = []
        k_mse = []

        # Evaluate for each n, writing the results to a summary files
        for k in k_trials:
            k_file = open("output/evaluate_" + str(k) + ".txt", "w")
            k_file.write("k=" + str(k) + "\n\n")

            user_recalls = []
            user_precisions = []
            user_fmeasures = []
            user_mae = []
            user_mse = []

            # Calculate prediction accuracy and top-N accuracy for each user
            for user_id in users_to_evaluate:
                print(k, user_id)

                k_file.write("user=" + str(user_id) + "\n")

                # Create list of movies that the current user has rated
                rated_by_user = ratings_matrix_u.columns[ratings_matrix_u[ratings_matrix_u.index == user_id].notna().any()].tolist()

                # Get the nearest n neighbours for the current user
                neighbours = cosine_similarity.apply(lambda x: pd.Series(x.sort_values(ascending=False).iloc[:k].index, index=[i for i in range(1, k + 1)]), axis=1)
                neighbours = neighbours[neighbours.index == user_id].values.squeeze().tolist()

                # Creates list of movies that neighbours have rated
                rated_by_neighbour = ','.join(rated_by_all[rated_by_all.index.isin(neighbours)].values).split(',')

                # Creates a df containing the cosine similarity between the current user and each neighbour
                similarity_to_neighbours = cosine_similarity.loc[user_id, neighbours]

                # Creates list of movies that have been rated by neighbours but not the current user
                # movies_not_rated_by_user = movies_rated_by_neighbours - movies_rated_by_user
                not_rated_by_user = list(map(int, (list(set(rated_by_neighbour) - set(list(map(str, rated_by_user)))))))

                true_movies = self.true_ratings.loc[self.true_ratings['user_id'] == user_id]
                predicted_movies = pd.DataFrame({'movie_id': not_rated_by_user})

                average_rating = average_ratings.loc[average_ratings['user_id'] == user_id, 'average_rating'].values[0]

                true_ratings = true_movies['rating'].to_list()
                predicted_ratings = []
                for movie_id in true_movies['movie_id']:
                    try:
                        # Create df containing all users normalised ratings for the current movie
                        ratings_by_all = ratings_matrix_f.loc[:, movie_id]

                        # Create df containing every neighbours normalised ratings for the current movie
                        ratings_by_neighbours = ratings_by_all[ratings_by_all.index.isin(neighbours)]

                        # Merge every neighbours normalised ratings with their similarity to the current user
                        merged = pd.concat([ratings_by_neighbours, similarity_to_neighbours], axis=1)
                        merged.columns = ['normalised_rating', 'similarity']

                        # Calculate a weighted rating based on the similarity
                        # weighted_rating = normalised_rating x similarity
                        merged['weighted_rating'] = merged.apply(calculate_weighted_rating, axis=1)

                        # Calculate a final prediction for the movie
                        weighted_rating_sum = merged['weighted_rating'].sum()
                        similarity_sum = merged['similarity'].sum()
                        prediction = average_rating + (weighted_rating_sum / similarity_sum)
                        predicted_ratings.append(prediction)
                    except KeyError:
                        # In the event of an error, predict the average user rating
                        predicted_ratings.append(average_rating)

                # Calculate the top-N accuracy for the set for true/predicted movies
                recall, precision, fmeasure = self.calculate_top_n_accuracy(true_movies, predicted_movies)
                user_recalls.append(recall)
                user_precisions.append(precision)
                user_fmeasures.append(fmeasure)
                k_file.write("true_movies=" + str(true_movies['movie_id'].tolist()) + "\n")
                k_file.write("pred_movies=" + str(predicted_movies['movie_id'].tolist()) + "\n")
                k_file.write("recall=" + str(recall) + "\n")
                k_file.write("precision=" + str(precision) + "\n")
                k_file.write("fmeasure=" + str(fmeasure) + "\n")

                # Calculate prediction accuracy for the set of true/predicted ratings
                mae, mse = self.calculate_prediction_accuracy(true_ratings, predicted_ratings)
                user_mae.append(mae)
                user_mse.append(mse)
                k_file.write("true_ratings=" + str(true_ratings) + "\n")
                k_file.write("pred_ratings=" + str(predicted_ratings) + "\n")
                k_file.write("mae=" + str(mae) + "\n")
                k_file.write("mse=" + str(mse) + "\n\n")

            k_file.close()

            # Calculate the average user recall, precision, and f-measure
            recall = sum(user_recalls) / len(user_recalls)
            precision = sum(user_precisions) / len(user_precisions)
            fmeasure = sum(user_fmeasures) / len(user_fmeasures)
            k_recalls.append(recall)
            k_precisions.append(precision)
            k_fmeasure.append(fmeasure)

            # Calculate the average user mae, and mae
            mae = sum(user_mae) / len(user_mae)
            mse = sum(user_mse) / len(user_mse)
            k_mae.append(mae)
            k_mse.append(mse)

            summary_file.write("k=" + str(k) + "\n")
            summary_file.write("recall=" + str(recall) + "\n")
            summary_file.write("precision=" + str(precision) + "\n")
            summary_file.write("fmeasure=" + str(fmeasure) + "\n")
            summary_file.write("mae=" + str(mae) + "\n")
            summary_file.write("mse=" + str(mse) + "\n\n")

        summary_file.write("k_trials=" + str(k_trials) + "\n")
        summary_file.write("k_recalls=" + str(k_recalls) + "\n")
        summary_file.write("k_precision=" + str(k_precisions) + "\n")
        summary_file.write("k_fmeasure=" + str(k_fmeasure) + "\n")
        summary_file.write("k_mae=" + str(k_mae) + "\n")
        summary_file.write("k_mse=" + str(k_mse))

        summary_file.close()

        return users_to_evaluate, k_recalls, k_precisions, k_fmeasure, k_mae, k_mse

    def calculate_top_n_accuracy(self, true, pred):
        # Recall: Number of recommendations that are accurate / Number of all possible accurate items
        # Precision: Number of recommendations that are accurate / Number of items was recommended
        # F-Measure (F1): Combining recall and precision into a single metric

        accurate = pred['movie_id'].isin(true['movie_id'])
        accurate_count = accurate.sum()

        recall = accurate_count / len(true)
        precision = accurate_count / len(pred)
        fmeasure = (2 * recall * precision) / (recall + precision)

        return recall, precision, fmeasure

    def calculate_prediction_accuracy(self, true, pred):
        # Mean Absolute Error (MAE): The average magnitude of errors from the list of predicted values
        # Mean Square Error (MSE): MAE, but distinguish between small and large errors - penalising larger errors

        mae = metrics.mean_absolute_error(true, pred)
        mse = metrics.mean_squared_error(true, pred)

        return mae, mse


def get_comma_separated_str(ls):
    return ','.join(ls)


def calculate_weighted_rating(x):
    return x['normalised_rating'] * x['similarity']

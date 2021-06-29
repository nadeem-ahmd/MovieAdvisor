package com.movieadvisor;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

public class Constants {

    public static final String MOVIE_ADVISOR_API_URL = "http://movieadvisor1-env.eba-ixwd3pwx.us-east-1.elasticbeanstalk.com/";
    public static final int MOVIE_ADVISOR_RECOMMENDATION_LIMIT = 20;
    public static final int MOVIE_ADVISOR_RECOMMENDATION_TIMEOUT = 60000;
    public static final int MOVIE_ADVISOR_RECOMMENDATION_REQUIRED_RATINGS = 10;

    public static final double MOVIE_ADVISOR_RECOMMENDATION_DEFAULT_LAT = 51.507;
    public static final double MOVIE_ADVISOR_RECOMMENDATION_DEFAULT_LONG = 0.1278;

    public static final String TMDB_API_KEY = "5b25a4d2bb38b7c8b4eaa8ff60b9a33d";
    public static final String TMDB_BROWSE_URL = "https://api.themoviedb.org/3/movie/top_rated?api_key=" + TMDB_API_KEY + "&language=en-US";
    public static final String TMDB_IMAGE_URL = "http://image.tmdb.org/t/p/";
    public static final String TMDB_BROWSE_POSTER_SIZE = "w342";

    public static final String ERROR_RESPONSE_MESSAGE = "Something went wrong, try again later.";
    public static final String ERROR_NETWORK_MESSAGE = "No network connection available.";

    public static void hide(View view) {
        view.setVisibility(View.INVISIBLE);
    }

    public static void show(View view) {
        view.setVisibility(View.VISIBLE);
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean hasConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null;
    }

    public static int numberOfColumns(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;

        if (nColumns < 3)
            return 3;

        return nColumns;
    }
}

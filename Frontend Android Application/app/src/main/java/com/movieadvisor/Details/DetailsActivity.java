package com.movieadvisor.Details;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.movieadvisor.Adapters.GenreRecyclerAdapter;
import com.movieadvisor.Constants;
import com.movieadvisor.Model.Movie;
import com.movieadvisor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {

    private RequestQueue requestQueue;

    private FirebaseUser user;

    private float userRating;
    private RatingBar userRatingBar;

    private Movie movie;

    private ImageView backdropImageView;
    private TextView nameTextView;
    private TextView overviewTextView;
    private RecyclerView genreRecyclerView;
    private TextView voteAverage;
    private TextView voteCount;

    private ProgressBar loadingProgressBar;
    private LinearLayout contentLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        requestQueue = Volley.newRequestQueue(this);
        user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        movie = (Movie) intent.getSerializableExtra("movie");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("");
        }

        contentLinearLayout = findViewById(R.id.content);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);

        backdropImageView = findViewById(R.id.backdrop_iv);
        nameTextView = findViewById(R.id.name_et);
        overviewTextView = findViewById(R.id.overview_tv);
        genreRecyclerView = findViewById(R.id.genre_recycle_view);
        voteAverage = findViewById(R.id.average_rating_tv);
        voteCount = findViewById(R.id.total_ratings_tv);

        userRatingBar = findViewById(R.id.user_rating_bar);

        Glide.with(this)
                .load(Constants.TMDB_IMAGE_URL + "original" + movie.getBackdrop())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(backdropImageView);

        String dateYear = movie.getReleaseDate().split("-", 3)[0];
        nameTextView.setText(movie.getName() + " (" + dateYear + ")");

        ArrayList<String> genres = movie.getGenres();
        genreRecyclerView.setLayoutManager(new LinearLayoutManager(DetailsActivity.this, RecyclerView.HORIZONTAL, false));
        GenreRecyclerAdapter genreRecyclerAdapter = new GenreRecyclerAdapter(this, genres);
        genreRecyclerView.setAdapter(genreRecyclerAdapter);

        overviewTextView.setText(movie.getOverview());

        userRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean changedByUser) {
                if (changedByUser) {
                    requestQueue.add(setRatingRequest(rating));
                }
            }
        });

        voteAverage.setText(movie.getVoteAverage());
        voteCount.setText(movie.getVoteCount());

        init();
    }

    private void init() {
        if (Constants.hasConnection(this))
            requestQueue.add(getRatingRequest());
        else
            Constants.toast(this, Constants.ERROR_NETWORK_MESSAGE);
    }

    private void show() {
        Constants.hide(loadingProgressBar);
        Constants.show(contentLinearLayout);
        Constants.show(backdropImageView);
    }

    private StringRequest setRatingRequest(final float rating) {
        return new StringRequest(Constants.MOVIE_ADVISOR_API_URL + "user/rating/set/?user_id=" + user.getEmail() + "&tmdb_id=" + movie.getId() + "&rating=" + Math.round(rating),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Do nothing
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Constants.toast(getApplicationContext(), "Failed to update rating.");
                        userRatingBar.setRating(userRating);
                    }
                });
    }

    private StringRequest getRatingRequest() {
        return new StringRequest(Constants.MOVIE_ADVISOR_API_URL + "user/rating/get/?user_id=" + user.getEmail() + "&tmdb_id=" + movie.getId(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length() != 0) {
                                JSONObject result = jsonArray.getJSONObject(0);
                                double rating = result.getDouble("rating");
                                float parsedRating = Float.parseFloat(String.valueOf(rating));
                                userRatingBar.setRating(parsedRating);
                                userRating = parsedRating;
                            }
                        } catch (JSONException e) {
                            Constants.toast(getApplicationContext(), "Failed to load rating.");
                        }
                        show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Constants.toast(getApplicationContext(), "Failed to load rating.");
                        show();
                    }
                });
    }
}

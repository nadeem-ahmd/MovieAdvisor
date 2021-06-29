package com.movieadvisor.Advisor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.movieadvisor.Adapters.MovieRecyclerAdapter;
import com.movieadvisor.Constants;
import com.movieadvisor.Details.DetailsActivity;
import com.movieadvisor.Model.Group;
import com.movieadvisor.Model.Movie;
import com.movieadvisor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdvisorMovieFragment extends Fragment {

    private RequestQueue requestQueue;
    private int movieDetailRequests = 0;

    private ArrayList<Movie> movieList = new ArrayList<>();

    private RecyclerView movieRecyclerView;
    private MovieRecyclerAdapter movieRecyclerAdapter;

    private ProgressBar loadingProgressBar;

    private Group group = null;
    private FirebaseUser user = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_advisor_movie, container, false);

        requestQueue = Volley.newRequestQueue(getContext());

        user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getActivity().getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("group"))
                group = (Group) intent.getSerializableExtra("group");
        }

        loadingProgressBar = view.findViewById(R.id.loading_progress_bar);

        movieRecyclerView = view.findViewById(R.id.movie_recycler_view);
        movieRecyclerAdapter = new MovieRecyclerAdapter(getContext(), movieList, new MovieRecyclerAdapter.RecyclerClickListener() {
            @Override
            public void onItemClicked(int clickedItemIndex) {
                Movie movie = movieList.get(clickedItemIndex);
                Intent intent = new Intent(getContext(), DetailsActivity.class);
                intent.putExtra("movie", movie);
                startActivity(intent);
            }
        });
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), Constants.numberOfColumns(getActivity()));
        movieRecyclerView.setLayoutManager(layoutManager);
        movieRecyclerView.setHasFixedSize(true);
        movieRecyclerView.setAdapter(movieRecyclerAdapter);

        init();

        return view;
    }

    private void init() {
        requestQueue.add(getMovieRecommendationRequest());
    }

    private void show() {
        Constants.hide(loadingProgressBar);
        Constants.show(movieRecyclerView);
    }

    private String getRecommendationRequestURL() {
        String url = Constants.MOVIE_ADVISOR_API_URL;
        if (group != null)
            url += "group/recommendation/movie/?group_id=" + group.getId();
        else
            url += "user/recommendation/movie/?user_id=" + user.getEmail();
        return url;
    }

    private StringRequest getMovieRecommendationRequest() {
        StringRequest request = new StringRequest(getRecommendationRequestURL(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length() == 0) {
                                Constants.toast(getContext(), "Oops! Try and rate more movies.");
                            } else {
                                for (int i = 0; i < jsonArray.length() && i < Constants.MOVIE_ADVISOR_RECOMMENDATION_LIMIT; i++) {
                                    JSONObject recommendation = jsonArray.getJSONObject(i);
                                    String tmdbId = recommendation.getString("tmdb_id");
                                    movieDetailRequests++;
                                    requestQueue.add(getMovieDetailsRequest(tmdbId));
                                }
                            }
                        } catch (JSONException e) {
                            Constants.toast(getContext(), Constants.ERROR_RESPONSE_MESSAGE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Constants.toast(getContext(), Constants.ERROR_RESPONSE_MESSAGE);
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(Constants.MOVIE_ADVISOR_RECOMMENDATION_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return request;
    }

    private StringRequest getMovieDetailsRequest(final String tmdbId) {
        return new StringRequest("https://api.themoviedb.org/3/movie/" + tmdbId + "?api_key=" + Constants.TMDB_API_KEY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            String id = jsonObject.getString("id");
                            String name = jsonObject.getString("original_title");
                            String poster = jsonObject.getString("poster_path");
                            String overview = jsonObject.getString("overview");
                            String voteAverage = jsonObject.getString("vote_average");
                            String releaseDate = jsonObject.getString("release_date");
                            String voteCount = jsonObject.getString("vote_count");
                            String backdrop = jsonObject.getString("backdrop_path");

                            JSONArray genreIds = jsonObject.getJSONArray("genres");
                            ArrayList<String> genres = new ArrayList<>();
                            for (int j = 0; j < genreIds.length(); j++) {
                                JSONObject genreJsonObject = genreIds.getJSONObject(j);
                                String genreName = genreJsonObject.getString("name");
                                genres.add(genreName);
                            }

                            Movie movie = new Movie(id, name, poster, overview, voteAverage, releaseDate, voteCount, backdrop, genres);
                            if (!movie.getPoster().equals("null") && !movie.getBackdrop().equals("null"))
                                movieList.add(movie);

                            movieDetailRequests--;
                            if (movieDetailRequests == 0) {
                                movieRecyclerAdapter.notifyDataSetChanged();
                                show();
                            }
                        } catch (JSONException e) {
                            Constants.toast(getContext(), "Failed to load movie " + tmdbId + ".");
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Constants.toast(getContext(), "Failed to load movie " + tmdbId + ".");
                        e.printStackTrace();
                    }
                });
    }
}


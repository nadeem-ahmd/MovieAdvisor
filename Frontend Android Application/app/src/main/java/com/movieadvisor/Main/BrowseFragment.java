package com.movieadvisor.Main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.movieadvisor.Adapters.MovieRecyclerAdapter;
import com.movieadvisor.Constants;
import com.movieadvisor.Details.DetailsActivity;
import com.movieadvisor.Model.Movie;
import com.movieadvisor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BrowseFragment extends Fragment implements MovieRecyclerAdapter.RecyclerClickListener {

    private RequestQueue requestQueue;

    private List<Movie> movieList = new ArrayList<>();
    private MovieRecyclerAdapter movieRecyclerAdapter;
    private RecyclerView movieRecyclerView;

    private int page = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        requestQueue = Volley.newRequestQueue(getContext());

        movieRecyclerView = view.findViewById(R.id.movie_recycler_view);
        movieRecyclerAdapter = new MovieRecyclerAdapter(getContext(), movieList, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), Constants.numberOfColumns(getActivity()));
        movieRecyclerView.setLayoutManager(layoutManager);
        movieRecyclerView.setHasFixedSize(true);
        movieRecyclerView.setAdapter(movieRecyclerAdapter);
        movieRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView movieRecycler, int newState) {
                super.onScrollStateChanged(movieRecycler, newState);
                if (!movieRecycler.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    requestQueue.add(getBrowseRequest(page++));
                }
            }
        });

        init();

        return view;
    }

    private void init() {
        if (movieList.isEmpty()) {
            if (Constants.hasConnection(getContext())) {
                requestQueue.add(getBrowseRequest(page++));
            } else {
                Constants.toast(getContext(), Constants.ERROR_NETWORK_MESSAGE);
            }
        }
    }

    private JsonObjectRequest getBrowseRequest(int page) {
        return new JsonObjectRequest(Constants.TMDB_BROWSE_URL + "&page=" + page, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject movieObject = results.getJSONObject(i);

                                String id = movieObject.getString("id");
                                String name = movieObject.getString("original_title");
                                String poster = movieObject.getString("poster_path");
                                String overview = movieObject.getString("overview");
                                String voteAverage = movieObject.getString("vote_average");
                                String releaseDate = movieObject.getString("release_date");
                                String voteCount = movieObject.getString("vote_count");
                                String backdrop = movieObject.getString("backdrop_path");

                                JSONArray genreIds = movieObject.getJSONArray("genre_ids");
                                ArrayList<String> genres = new ArrayList<>();
                                for (int j = 0; j < genreIds.length(); j++) {
                                    String genre = Movie.getGenreTitle(genreIds.getInt(j));
                                    genres.add(genre);
                                }

                                Movie movie = new Movie(id, name, poster, overview, voteAverage, releaseDate, voteCount, backdrop, genres);

                                if (!movie.getPoster().equals("null") && !movie.getBackdrop().equals("null"))
                                    movieList.add(movie);
                            }
                            movieRecyclerAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Constants.toast(getContext(), Constants.ERROR_RESPONSE_MESSAGE);
                    }
                });
    }

    @Override
    public void onItemClicked(int position) {
        Movie movie = movieList.get(position);
        Intent intent = new Intent(getContext(), DetailsActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }
}

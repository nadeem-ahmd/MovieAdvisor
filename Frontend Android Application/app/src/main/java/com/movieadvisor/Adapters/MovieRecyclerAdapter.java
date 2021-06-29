package com.movieadvisor.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.movieadvisor.Constants;
import com.movieadvisor.Model.Movie;
import com.movieadvisor.R;

import java.util.List;

public class MovieRecyclerAdapter extends RecyclerView.Adapter<MovieRecyclerAdapter.MovieViewHolder> {

    private Context context;
    private List<Movie> movieList;
    private RecyclerClickListener recyclerClickListener;

    public MovieRecyclerAdapter(Context context, List<Movie> movieList, RecyclerClickListener recyclerClickListener) {
        this.context = context;
        this.movieList = movieList;
        this.recyclerClickListener = recyclerClickListener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MovieViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_movie, null));
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        Glide.with(context)
                .load(Constants.TMDB_IMAGE_URL + Constants.TMDB_BROWSE_POSTER_SIZE + movie.getPoster())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.posterImageView);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public interface RecyclerClickListener {
        void onItemClicked(int clickedItemIndex);
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView posterImageView;

        MovieViewHolder(View view) {
            super(view);
            posterImageView = view.findViewById(R.id.poster_iv);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            recyclerClickListener.onItemClicked(getAdapterPosition());
        }
    }
}

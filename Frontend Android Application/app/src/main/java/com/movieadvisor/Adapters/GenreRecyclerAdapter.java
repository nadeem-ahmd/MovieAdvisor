package com.movieadvisor.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.movieadvisor.R;

import java.util.ArrayList;

public class GenreRecyclerAdapter extends RecyclerView.Adapter<GenreRecyclerAdapter.GenreViewHolder> {

    private Context context;
    private ArrayList<String> genreList;

    public GenreRecyclerAdapter(Context context, ArrayList<String> genreList) {
        this.context = context;
        this.genreList = genreList;
    }

    @Override
    public GenreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GenreViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_item_genre, parent, false));
    }

    @Override
    public void onBindViewHolder(GenreViewHolder holder, int position) {
        holder.genreTextView.setText(genreList.get(position));
    }

    @Override
    public int getItemCount() {
        return genreList.size();
    }

    class GenreViewHolder extends RecyclerView.ViewHolder {
        TextView genreTextView;

        GenreViewHolder(View itemView) {
            super(itemView);
            genreTextView = itemView.findViewById(R.id.genre_tv);
        }
    }
}

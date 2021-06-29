package com.movieadvisor.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.movieadvisor.Model.Group;
import com.movieadvisor.R;

import java.util.ArrayList;

public class GroupListAdapter extends ArrayAdapter<Group> {

    public GroupListAdapter(Context context, ArrayList<Group> groups) {
        super(context, 0, groups);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_group, parent, false);

        Group group = getItem(position);

        TextView titleTextView = view.findViewById(R.id.name_tv);
        titleTextView.setText(group.getId());

        TextView membersTextView = view.findViewById(R.id.members_tv);
        membersTextView.setText("Members: " + group.getSize());

        TextView ratingsTextView = view.findViewById(R.id.ratings_tv);
        ratingsTextView.setText("Ratings: " + group.getRatings());

        return view;
    }
}

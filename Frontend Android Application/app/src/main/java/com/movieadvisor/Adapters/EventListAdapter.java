package com.movieadvisor.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.movieadvisor.Model.Event;
import com.movieadvisor.R;

import java.util.ArrayList;

public class EventListAdapter extends ArrayAdapter<Event> {

    public EventListAdapter(Context context, ArrayList<Event> groups) {
        super(context, 0, groups);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_event, parent, false);

        final Event event = getItem(position);

        TextView titleTextView = view.findViewById(R.id.title_tv);
        titleTextView.setText(event.getName());

        TextView placeTextView = view.findViewById(R.id.place_tv);
        placeTextView.setText(event.getPlace());

        TextView startTextView = view.findViewById(R.id.start_tv);
        startTextView.setText("Start: " + event.getStart());

        TextView endTextView = view.findViewById(R.id.end_tv);
        endTextView.setText("End: " + event.getEnd());

        TextView descriptionTextView = view.findViewById(R.id.tags_tv);
        descriptionTextView.setText("Tags: " + event.getTags());

        TextView rationalTextView = view.findViewById(R.id.rational_tv);
        rationalTextView.setText(event.getRational());

        return view;
    }
}

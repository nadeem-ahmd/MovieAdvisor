package com.movieadvisor.Main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.movieadvisor.Authentication.AuthenticationActivity;
import com.movieadvisor.Constants;
import com.movieadvisor.R;

import org.json.JSONArray;
import org.json.JSONException;

public class ProfileFragment extends Fragment {

    private RequestQueue requestQueue;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private View view;

    private ConstraintLayout contentConstraintLayout;
    private ProgressBar loadingProgressBar;

    private TextView nameTextView;
    private TextView emailTextView;
    private TextView ratingsTextView;
    private TextView groupsTextView;
    private Button signOutButton;

    private boolean ratingsLoaded;
    private boolean groupsLoaded;
    private boolean loaded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        requestQueue = Volley.newRequestQueue(getContext());

        ratingsLoaded = false;
        groupsLoaded = false;

        loadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        contentConstraintLayout = view.findViewById(R.id.content);

        nameTextView = view.findViewById(R.id.name_et);
        emailTextView = view.findViewById(R.id.email_tv);
        ratingsTextView = view.findViewById(R.id.ratings_tv);
        groupsTextView = view.findViewById(R.id.groups_tv);

        signOutButton = view.findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(getContext(), AuthenticationActivity.class));
                getActivity().finish();
            }
        });

        nameTextView.setText(user.getDisplayName());
        emailTextView.setText(user.getEmail());

        loaded = true;

        init();

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (loaded) {
            if (isVisibleToUser)
                init();
            else
                hide();
        }
    }

    private void init() {
        if (Constants.hasConnection(requireContext())) {
            requestQueue.add(getRatingsRequest());
            requestQueue.add(getGroupsRequest());
        } else {
            Constants.toast(getContext(), Constants.ERROR_NETWORK_MESSAGE);
        }
    }

    private void show() {
        Constants.hide(loadingProgressBar);
        Constants.show(contentConstraintLayout);
    }

    private void hide() {
        Constants.show(loadingProgressBar);
        Constants.hide(contentConstraintLayout);
    }

    private StringRequest getRatingsRequest() {
        return new StringRequest(Constants.MOVIE_ADVISOR_API_URL + "user/rating/?user_id=" + user.getEmail(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            ratingsTextView.setText(String.valueOf(jsonArray.length()));
                            ratingsLoaded = true;

                            if (groupsLoaded)
                                show();
                        } catch (JSONException e) {
                            Constants.toast(getContext(), "Failed to load user ratings.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Constants.toast(getContext(), "Failed to load user ratings.");
                    }
                });
    }

    private StringRequest getGroupsRequest() {
        return new StringRequest(Constants.MOVIE_ADVISOR_API_URL + "user/group/?user_id=" + user.getEmail(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            groupsTextView.setText(String.valueOf(jsonArray.length()));
                            groupsLoaded = true;

                            if (ratingsLoaded)
                                show();
                        } catch (JSONException e) {
                            Constants.toast(getContext(), "Failed to load user groups.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Constants.toast(getContext(), "Failed to load user groups.");
                    }
                });
    }
}
package com.movieadvisor.Main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.movieadvisor.Adapters.GroupListAdapter;
import com.movieadvisor.Advisor.AdvisorActivity;
import com.movieadvisor.Constants;
import com.movieadvisor.Model.Group;
import com.movieadvisor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdvisorFragment extends Fragment {

    private RequestQueue requestQueue;
    private int groupInformationRequests = 0;

    private FirebaseUser user;
    private int userRatings;

    private ArrayList<Group> groupList;
    private GroupListAdapter groupListAdapter;
    private ListView groupListView;

    private ProgressBar loadingProgressBar;
    private LinearLayout advisorLinearLayout;
    private LinearLayout groupLinearLayout;

    private TextView emptyListTextView;

    private Button individualAdvisorButton;
    private Button groupAdvisorButton;
    private Button cancelButton;

    private boolean loaded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_advisor, container, false);

        requestQueue = Volley.newRequestQueue(getContext());

        user = FirebaseAuth.getInstance().getCurrentUser();

        groupList = new ArrayList<>();
        groupListAdapter = new GroupListAdapter(getActivity(), groupList);
        groupListView = view.findViewById(R.id.groups_list_view);
        groupListView.setAdapter(groupListAdapter);
        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Group group = groupList.get(position);
                Intent intent = new Intent(getContext(), AdvisorActivity.class);
                intent.putExtra("group", group);
                startActivity(intent);
            }
        });

        loadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        advisorLinearLayout = view.findViewById(R.id.advisor_linear_layout);
        groupLinearLayout = view.findViewById(R.id.group_linear_layout);
        emptyListTextView = view.findViewById(R.id.empty_tv);

        individualAdvisorButton = view.findViewById(R.id.individual_button);
        groupAdvisorButton = view.findViewById(R.id.group_button);
        cancelButton = view.findViewById(R.id.cancel_button);

        TextView greeting = view.findViewById(R.id.greeting_tv);
        greeting.setText("Welcome, " + user.getDisplayName());

        individualAdvisorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userRatings < Constants.MOVIE_ADVISOR_RECOMMENDATION_REQUIRED_RATINGS) {
                    Constants.toast(getContext(), "Rate " + (Constants.MOVIE_ADVISOR_RECOMMENDATION_REQUIRED_RATINGS - userRatings) + " more movies to use this feature.");
                } else {
                    startActivity(new Intent(getContext(), AdvisorActivity.class));
                }
            }
        });

        groupAdvisorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userRatings < Constants.MOVIE_ADVISOR_RECOMMENDATION_REQUIRED_RATINGS) {
                    Constants.toast(getContext(), "Rate " + (Constants.MOVIE_ADVISOR_RECOMMENDATION_REQUIRED_RATINGS - userRatings) + " more movies to use this feature.");
                } else {
                    Constants.hide(advisorLinearLayout);
                    Constants.show(loadingProgressBar);

                    groupList.clear();
                    groupListAdapter.notifyDataSetChanged();

                    requestQueue.add(getUserGroupsRequest());
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAdvisor();
            }
        });

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
        if (Constants.hasConnection(getContext()))
            requestQueue.add(checkRatingRequest());
        else
            Constants.toast(getContext(), Constants.ERROR_NETWORK_MESSAGE);
    }

    private void showAdvisor() {
        Constants.hide(loadingProgressBar);
        Constants.hide(groupLinearLayout);
        Constants.show(advisorLinearLayout);
    }

    private void showGroup() {
        Constants.hide(loadingProgressBar);
        Constants.hide(advisorLinearLayout);
        Constants.show(groupLinearLayout);
    }

    private void hide() {
        Constants.show(loadingProgressBar);
        Constants.hide(advisorLinearLayout);
        Constants.hide(groupLinearLayout);
    }

    private StringRequest checkRatingRequest() {
        return new StringRequest(Constants.MOVIE_ADVISOR_API_URL + "user/rating/count/?user_id=" + user.getEmail(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            userRatings = jsonObject.getInt("count");
                            showAdvisor();
                        } catch (JSONException e) {
                            Constants.toast(getContext(), "Failed to count user ratings.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Constants.toast(getContext(), "Failed to count user ratings.");
                    }
                });
    }

    private StringRequest getUserGroupsRequest() {
        return new StringRequest(Constants.MOVIE_ADVISOR_API_URL + "user/group/?user_id=" + user.getEmail(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length() != 0) {
                                Constants.hide(emptyListTextView);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject JSONObject = jsonArray.getJSONObject(i);
                                    String groupId = JSONObject.getString("group_id");

                                    groupInformationRequests++;
                                    requestQueue.add(getGroupInformationRequest(groupId));
                                }
                            } else {
                                Constants.show(emptyListTextView);
                                showGroup();
                            }
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

    private StringRequest getGroupInformationRequest(final String groupId) {
        return new StringRequest(Constants.MOVIE_ADVISOR_API_URL + "group/count/?group_id=" + groupId,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject JSONObject = new JSONObject(response);
                            int size = JSONObject.getInt("size");
                            int ratings = JSONObject.getInt("ratings");
                            Group group = new Group(groupId, size, ratings);
                            groupList.add(group);
                            groupListAdapter.notifyDataSetChanged();

                            groupInformationRequests--;
                            if (groupInformationRequests == 0)
                                showGroup();
                        } catch (JSONException e) {
                            Constants.toast(getContext(), "Failed to load group information.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Constants.toast(getContext(), "Failed to load group information.");
                    }
                });
    }

}
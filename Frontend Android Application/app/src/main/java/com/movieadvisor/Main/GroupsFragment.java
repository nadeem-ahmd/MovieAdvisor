package com.movieadvisor.Main;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.movieadvisor.Adapters.GroupListAdapter;
import com.movieadvisor.Constants;
import com.movieadvisor.Model.Group;
import com.movieadvisor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GroupsFragment extends Fragment {

    private RequestQueue requestQueue;
    private int groupInformationRequests = 0;

    private FirebaseUser user;

    private ArrayList<Group> groupList;
    private GroupListAdapter groupListAdapter;
    private ListView groupListView;

    private TextView emptyListTextView;

    private LinearLayout contentLinearLayout;
    private ProgressBar loadingProgressBar;

    private boolean loaded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        requestQueue = Volley.newRequestQueue(getContext());

        user = FirebaseAuth.getInstance().getCurrentUser();

        groupList = new ArrayList<>();
        groupListAdapter = new GroupListAdapter(getActivity(), groupList);
        groupListView = view.findViewById(R.id.genre_list_view);
        groupListView.setAdapter(groupListAdapter);
        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Group group = groupList.get(position);
                pressLeave(group.getId());
            }
        });

        loadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        contentLinearLayout = view.findViewById(R.id.advisor_linear_layout);
        emptyListTextView = view.findViewById(R.id.message_tv);

        Button createButton = view.findViewById(R.id.create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressCreate();
            }
        });

        Button joinButton = view.findViewById(R.id.join_button);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressJoin();
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
        if (Constants.hasConnection(getContext())) {
            refresh();
        } else {
            Constants.toast(getContext(), Constants.ERROR_NETWORK_MESSAGE);
        }
    }

    private void refresh() {
        hide();
        groupList.clear();
        groupListAdapter.clear();
        requestQueue.add(getUserGroupsRequest());
    }

    private void hide() {
        Constants.show(loadingProgressBar);
        Constants.hide(contentLinearLayout);
    }

    private void show() {
        Constants.hide(loadingProgressBar);
        Constants.show(contentLinearLayout);
    }

    private StringRequest getUserGroupsRequest() {
        return new StringRequest(Constants.MOVIE_ADVISOR_API_URL + "user/group/?user_id=" + user.getEmail(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length() != 0) {
                                emptyListTextView.setVisibility(View.INVISIBLE);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject JSONObject = jsonArray.getJSONObject(i);
                                    String groupId = JSONObject.getString("group_id");

                                    groupInformationRequests++;
                                    requestQueue.add(getGroupInformationRequest(groupId));
                                }
                            } else {
                                Constants.show(emptyListTextView);
                                show();
                            }
                        } catch (JSONException e) {
                            Constants.toast(getContext(), "Failed to load user groups.");
                            show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Constants.toast(getContext(), "Failed to load user groups.");
                        show();
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
                                show();
                        } catch (JSONException e) {
                            Constants.toast(getContext(), "Failed to load group information.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Constants.toast(getContext(), "Failed to load group information.");
                        show();
                    }
                });
    }

    private StringRequest createGroupRequest(final String groupId) {
        return new StringRequest(Constants.MOVIE_ADVISOR_API_URL + "user/group/create/?user_id=" + user.getEmail() + "&group_id=" + groupId,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("name in use"))
                            Constants.toast(getContext(), "That group already exists.");

                        refresh();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Constants.toast(getContext(), "Failed to create group.");
                        show();
                    }
                });
    }

    private StringRequest joinGroupRequest(String groupId) {
        return new StringRequest(Constants.MOVIE_ADVISOR_API_URL + "user/group/join/?user_id=" + user.getEmail() + "&group_id=" + groupId,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("no such group"))
                            Constants.toast(getContext(), "That group does not exist.");
                        else if (response.equals("user already in group"))
                            Constants.toast(getContext(), "You're already in that group.");

                        refresh();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Constants.toast(getContext(), "Failed to join group.");
                        show();
                    }
                });
    }

    private StringRequest leaveGroupRequest(String groupId) {
        return new StringRequest(Constants.MOVIE_ADVISOR_API_URL + "user/group/leave/?user_id=" + user.getEmail() + "&group_id=" + groupId,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        refresh();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Constants.toast(getContext(), "Failed to leave group.");
                    }
                });
    }

    private void pressCreate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter a group name:");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_input_builder, (ViewGroup) getView(), false);
        final EditText input = view.findViewById(R.id.input_et);

        builder.setView(view);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                String groupId = input.getText().toString();
                if (groupId.contains(" ")) {
                    Constants.toast(getContext(), "Name may not contain spaces.");
                } else {
                    requestQueue.add(createGroupRequest(groupId));
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void pressJoin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter the group name:");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_input_builder, (ViewGroup) getView(), false);
        final EditText input = view.findViewById(R.id.input_et);

        builder.setView(view);
        builder.setPositiveButton("Join", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                String groupId = input.getText().toString();
                if (groupId.contains(" ")) {
                    Constants.toast(getContext(), "Name may not contain spaces.");
                } else {
                    requestQueue.add(joinGroupRequest(groupId));
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void pressLeave(final String groupId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Leave \"" + groupId + "\"?");
        builder.setPositiveButton("Leave", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                requestQueue.add(leaveGroupRequest(groupId));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
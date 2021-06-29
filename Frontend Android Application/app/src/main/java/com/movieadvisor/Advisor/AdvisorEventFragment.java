package com.movieadvisor.Advisor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.movieadvisor.Adapters.EventListAdapter;
import com.movieadvisor.Constants;
import com.movieadvisor.Model.Event;
import com.movieadvisor.Model.Group;
import com.movieadvisor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdvisorEventFragment extends Fragment {

    private RequestQueue requestQueue;

    private ArrayList<Event> eventList = new ArrayList<>();

    private ProgressBar loadingProgressBar;

    private ListView eventListView;
    private EventListAdapter eventListAdapter;

    private Group group = null;
    private FirebaseUser user = null;

    private double latitude = Constants.MOVIE_ADVISOR_RECOMMENDATION_DEFAULT_LAT;
    private double longitude = Constants.MOVIE_ADVISOR_RECOMMENDATION_DEFAULT_LONG;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_advisor_event, container, false);

        requestQueue = Volley.newRequestQueue(getContext());

        user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getActivity().getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("group"))
                group = (Group) intent.getSerializableExtra("group");
        }

        loadingProgressBar = view.findViewById(R.id.loading_progress_bar);

        eventListAdapter = new EventListAdapter(getActivity(), eventList);
        eventListView = view.findViewById(R.id.event_list_view);
        eventListView.setAdapter(eventListAdapter);

        Dexter.withActivity(getActivity())
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        init();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();

        return view;
    }

    private void init() {
        try {
            Location location = getUserLocation();
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Constants.toast(getContext(), "Location: " + latitude + ", " + longitude + ".");
            } else {
                Constants.toast(getContext(), "Default location: " + latitude + ", " + longitude + ".");
            }
        } catch (Exception e) {
            Constants.toast(getContext(), "Default location: " + latitude + ", " + longitude + ".");
        }
        requestQueue.add(getEventRecommendationRequest());
    }

    private void show() {
        Constants.hide(loadingProgressBar);
        Constants.show(eventListView);
    }

    private String getRecommendationRequestURL() {
        String url = Constants.MOVIE_ADVISOR_API_URL;
        if (group != null)
            url += "group/recommendation/event/?group_id=" + group.getId();
        else
            url += "user/recommendation/event/?user_id=" + user.getEmail();
        return url + "&latitude=" + latitude + "&longitude=" + longitude;
    }

    private StringRequest getEventRecommendationRequest() {
        return new StringRequest(getRecommendationRequestURL(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            ArrayList<Event> genreEvents = new ArrayList<>();
                            ArrayList<Event> keywordsEvents = new ArrayList<>();

                            JSONObject keywords = jsonResponse.getJSONObject("keywords");

                            JSONArray keywordNames = keywords.names();
                            if (keywordNames != null) {
                                for (int i = 0; i < keywordNames.length(); i++) {
                                    String keywordName = keywordNames.getString(i);
                                    JSONArray keywordsArray = keywords.getJSONArray(keywordName);

                                    for (int j = 0; j < keywordsArray.length(); j++) {
                                        JSONObject keywordObject = keywordsArray.getJSONObject(j);

                                        String id = keywordObject.getString("event_id");
                                        String name = keywordObject.getString("name");

                                        String start = keywordObject.getString("start_ts");
                                        String end = keywordObject.getString("end_ts");

                                        String placeName = keywordObject.getString("place_name");
                                        String town = keywordObject.getString("town");
                                        String postal_code = keywordObject.getString("postal_code");
                                        String place = placeName + ", " + town + ", " + postal_code;

                                        ArrayList<String> tags = new ArrayList<>();
                                        JSONArray tagsArray = keywordObject.getJSONArray("tags");
                                        for (int m = 0; m < tagsArray.length(); m++) {
                                            tags.add(tagsArray.getString(m));
                                        }

                                        String source = "movies with the " + keywordName + " theme";
                                        Event event = new Event(id, name, start, end, place, tags, source);

                                        keywordsEvents.add(event);
                                    }
                                }
                            }

                            JSONObject genres = jsonResponse.getJSONObject("genres");
                            JSONArray genreNames = genres.names();
                            if (genreNames != null) {
                                for (int i = 0; i < genreNames.length(); i++) {
                                    String genreName = genreNames.getString(i);
                                    JSONArray genresArray = genres.getJSONArray(genreName);

                                    for (int j = 0; j < genresArray.length(); j++) {
                                        JSONObject genreObject = genresArray.getJSONObject(j);

                                        String id = genreObject.getString("event_id");
                                        String name = genreObject.getString("name");

                                        String start = genreObject.getString("start_ts");
                                        String end = genreObject.getString("end_ts");

                                        String placeName = genreObject.getString("place_name");
                                        String town = genreObject.getString("town");
                                        String postal_code = genreObject.getString("postal_code");
                                        String place = placeName + ", " + town + ", " + postal_code;

                                        ArrayList<String> tags = new ArrayList<>();
                                        JSONArray tagsArray = genreObject.getJSONArray("tags");
                                        for (int k = 0; k < tagsArray.length(); k++) {
                                            tags.add(tagsArray.getString(k));
                                        }

                                        String source = "the " + genreName + " genre";
                                        Event event = new Event(id, name, start, end, place, tags, source);

                                        genreEvents.add(event);
                                    }
                                }
                            }

                            eventList.addAll(keywordsEvents);
                            eventList.addAll(genreEvents);

                            eventListAdapter.notifyDataSetChanged();
                            show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Constants.toast(getContext(), Constants.ERROR_RESPONSE_MESSAGE);
                    }
                });
    }

    private Location getUserLocation() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkLocationEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (gpsEnabled) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return null;
        }

        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location networkLocation = null;

        if (networkLocationEnabled)
            networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (gpsLocation != null && networkLocation != null) {
            if (gpsLocation.getAccuracy() > networkLocation.getAccuracy())
                return networkLocation;
            else
                return gpsLocation;
        } else {
            if (gpsLocation != null)
                return gpsLocation;
            else
                return networkLocation;
        }
    }
}


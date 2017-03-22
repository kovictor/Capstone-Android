package com.capstonappdeveloper.capstone_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.capstonappdeveloper.capstone_android.Protocol.Map.Event;
import com.capstonappdeveloper.capstone_android.Protocol.Map.EventFetcher;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

/**
 * Created by james on 2016-12-01.
 */
public class EventMapFragment extends Fragment
        implements OnMapReadyCallback, OnMarkerClickListener {

    private HashMap<String, Event> events;
    private LatLng homeLocation;
    private LatLng currentPin;
    private GoogleMap googleMap;
    private SupportMapFragment fragment;
    private Event currentEvent;
    private ProgressBar dialog;
    private static boolean initalized;

    LinearLayout overheadBanner;
    ImageView overheadIcon;
    TextView overheadTitle;
    RelativeLayout darkenFilter;
    ImageView retryButton;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, container, false);

        retryButton = (ImageView) view.findViewById(R.id.retry_button);
        darkenFilter = (RelativeLayout) view.findViewById(R.id.darken_filter);
        dialog = (ProgressBar) view.findViewById(R.id.load_spinner);
        overheadBanner = (LinearLayout) view.findViewById(R.id.event_banner);
        overheadIcon = (ImageView) view.findViewById(R.id.event_icon);
        overheadTitle = (TextView) view.findViewById(R.id.event_title);
        //Todo: Get the actual gps location of the user
        homeLocation = new LatLng(43.761539, -79.411079);
        events = new HashMap<String, Event>();
        currentPin = null;
        showSpinner();

        return view;
    }

    protected void showSpinner() {
        dialog.setVisibility(View.VISIBLE);
        darkenFilter.setAlpha((float) 0.7);
    }

    public void hideSpinner() {
        dialog.setVisibility(View.GONE);
        darkenFilter.setAlpha((float) 0.0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }
        fragment.getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        googleMap.setOnMarkerClickListener(this);

        if (StaticResources.isNetworkAvailable(getActivity())) {
            Log.d("EVENT SYNC", "Starting event fetch from server");
            new EventFetcher(this).execute();
        }
        else {
            Log.d("Internet not connected", "Wifi connection unavailable");
            showRetry();
        }
    }

    protected double distance(LatLng a, LatLng b) {
        return Math.sqrt(
                Math.pow(a.latitude - b.latitude, 2) +
                Math.pow(a.longitude - b.longitude, 2)
        );
    }

    public String getCurrentEventID() {
        return currentEvent.id;
    }

    public void setOverhead(Event event) {
        overheadIcon.setImageBitmap(event.icon);
        overheadTitle.setText(event.eventName);
        currentPin = event.coordinates;
        this.currentEvent = event;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        setOverhead(events.get(marker.getSnippet()));
        return true;
    }

    public void showRetry() {
        hideSpinner();
        darkenFilter.setAlpha((float) 0.7);
        retryButton.setVisibility(View.VISIBLE);
    }

    public void fetchEvents() {
        new EventFetcher(this).execute();
        showSpinner();
        retryButton.setVisibility(View.GONE);
    }

    public void enterEvent(int numParticipants) {
        Intent intent = new Intent(getContext(), CameraActivity.class);
        intent.putExtra(CameraActivity.CURRENT_EVENT_ID, currentEvent.id);
        intent.putExtra(CameraActivity.CURRENT_EVENT_NAME, currentEvent.eventName);
        intent.putExtra(CameraActivity.NUM_PARTICIPANTS, numParticipants);
        startActivity(intent);
    }

    //some getters
    public HashMap<String, Event> getEvents() {
        return events;
    }

    public GoogleMap getMap() {
        return googleMap;
    }

    public LinearLayout getOverheadBanner() {
        return overheadBanner;
    }

    public LatLng getHomeLocation() {
        return homeLocation;
    }
}
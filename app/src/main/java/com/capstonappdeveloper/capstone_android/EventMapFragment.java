package com.capstonappdeveloper.capstone_android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.capstonappdeveloper.capstone_android.Protocol.Map.CreateEventUploader;
import com.capstonappdeveloper.capstone_android.Protocol.Map.Event;
import com.capstonappdeveloper.capstone_android.Protocol.Map.EventFetcher;
import com.capstonappdeveloper.capstone_android.Protocol.Video.BitmapLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by james on 2016-12-01.
 */
public class EventMapFragment extends Fragment
        implements OnMapReadyCallback, OnMarkerClickListener, OnMapClickListener {

    private HashMap<String, Event> events;
    private LatLng homeLocation;
    private LatLng currentPin;
    private Marker createPin;
    private GoogleMap googleMap;
    private SupportMapFragment fragment;
    private Event currentEvent;
    private ProgressBar dialog;
    private static boolean createMode = false;

    LinearLayout overheadBanner;
    ImageView overheadIcon;
    ImageView overheadButton;
    TextView overheadTitle;
    RelativeLayout darkenFilter;
    ImageView retryButton;
    EditText eventNameField;

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
        overheadButton = (ImageView) view.findViewById(R.id.camera_button);
        overheadTitle = (TextView) view.findViewById(R.id.event_title);
        eventNameField = (EditText) view.findViewById(R.id.create_event_field);
        //Todo: Get the actual gps location of the user
        homeLocation = new LatLng(43.761539, -79.411079);
        events = new HashMap<String, Event>();
        currentPin = null;
        createPin = null;
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
        googleMap.setOnMapClickListener(this);

        if (StaticResources.isNetworkAvailable(getActivity())) {
            Log.d("EVENT SYNC", "Starting event fetch from server");
            fetchEvents();
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
        overheadBanner.setVisibility(View.VISIBLE);
        this.currentEvent = event;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (!getCreateMode()) {
            setOverhead(events.get(marker.getSnippet()));
        }
        return true;
    }

    public void showRetry() {
        hideSpinner();
        darkenFilter.setAlpha((float) 0.7);
        retryButton.setVisibility(View.VISIBLE);
    }

    public void fetchEvents() {
        showSpinner();
        retryButton.setVisibility(View.GONE);
        new EventFetcher(this).execute();
    }

    public void enterEvent(int numParticipants) {
        Intent intent = new Intent(getContext(), CameraActivity.class);
        intent.putExtra(CameraActivity.CURRENT_EVENT_ID, currentEvent.id);
        intent.putExtra(CameraActivity.CURRENT_EVENT_NAME, currentEvent.eventName);
        intent.putExtra(CameraActivity.NUM_PARTICIPANTS, numParticipants);
        startActivity(intent);
    }

    public void setSearchMode() {
        this.createMode = false;
        if (createPin != null) {
            createPin.remove();
            createPin = null;
        }
        eventNameField.setVisibility(View.GONE);
        overheadIcon.setVisibility(View.VISIBLE);
        overheadBanner.setBackgroundColor(Color.WHITE);
        overheadTitle.setVisibility(View.VISIBLE);
        overheadTitle.setText(currentEvent.eventName);
        overheadButton.setVisibility(View.VISIBLE);
    }

    public boolean getCreateMode() {
        return createMode;
    }

    public void setCreateMode(boolean mode) {
        this.createMode = mode;
    }

    public void setPickLocationMode() {
        this.createMode = true;
        eventNameField.setText("");
        overheadBanner.setBackgroundColor(Color.WHITE);
        eventNameField.setVisibility(View.GONE);
        overheadIcon.setVisibility(View.GONE);
        overheadTitle.setVisibility(View.VISIBLE);
        overheadTitle.setText(R.string.pick_event_location);
        overheadButton.setVisibility(View.GONE);
    }

    public void setCreateEventMode() {
        overheadBanner.setBackgroundColor(Color.CYAN);
        eventNameField.setVisibility(View.VISIBLE);
        overheadTitle.setVisibility(View.GONE);
        overheadButton.setVisibility(View.VISIBLE);
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

    public void submitNewEvent() {
        if (createPin != null) {
            createPin.remove();
        }
        showSpinner();
        String eventName = eventNameField.getText().toString();
        // create a layoutInflater for marker bitmap creation
        View customMarkerView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.event_marker, null);
        int randomIcon = StaticResources.eventIcons[new Random().nextInt(StaticResources.numIcons)];
        Bitmap bmp = BitmapLoader.getMarkerBitmapFromView(randomIcon, customMarkerView);

        Event newEvent = new Event(Integer.toString(Math.abs(eventName.hashCode())), createPin.getPosition(), bmp, eventName);
        events.put(newEvent.id, newEvent);
        googleMap.addMarker(new MarkerOptions()
                .position(newEvent.coordinates)
                .title(newEvent.eventName)
                .icon(BitmapDescriptorFactory.fromBitmap(newEvent.icon)))
                .setSnippet(newEvent.id);
        if (getCreateMode()) {
            setCreateEventMode();
        }
        new CreateEventUploader(this, newEvent).execute();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (getCreateMode()) {
            if (createPin != null) {
                createPin.remove();
            }
            createPin = googleMap.addMarker(new MarkerOptions()
                    .position(latLng));
            if (getCreateMode()) {
                setCreateEventMode();
            }
        }
    }
}
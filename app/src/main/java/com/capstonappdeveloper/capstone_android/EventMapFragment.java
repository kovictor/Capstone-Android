package com.capstonappdeveloper.capstone_android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    LinearLayout overheadBanner;
    ImageView overheadIcon;
    TextView overheadTitle;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, container, false);

        overheadBanner = (LinearLayout) view.findViewById(R.id.event_banner);
        overheadIcon = (ImageView) view.findViewById(R.id.event_icon);
        overheadTitle = (TextView) view.findViewById(R.id.event_title);
        homeLocation = new LatLng(43.6532, -79.3832);
        events = new HashMap<String, Event>();
        currentPin = null;

        return view;
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
        //googleMap.setOnCameraIdleListener(this);
        new EventFetcher(map, homeLocation, events).execute();

    }

    protected double distance(LatLng a, LatLng b) {
        return Math.sqrt(
                Math.pow(a.latitude - b.latitude, 2) +
                Math.pow(a.longitude - b.longitude, 2)
        );
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Event event = events.get(marker.getSnippet());

        overheadIcon.setImageBitmap(event.icon);
        overheadTitle.setText(event.eventName);
        if (currentPin == null) {
            overheadBanner.setVisibility(View.VISIBLE);
        }
        currentPin = event.coordinates;
        return true;
    }
}
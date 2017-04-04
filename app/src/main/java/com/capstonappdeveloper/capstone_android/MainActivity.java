package com.capstonappdeveloper.capstone_android;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.capstonappdeveloper.capstone_android.Protocol.Map.EventJoiner;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

/**
 * For now, we're just swapping fragments into the framelayout "fragment_container"
 * but eventually we'll probably want to move to something like a viewPager
 * so that we can switch fragments while maintaining fragment state
 */
public class MainActivity extends FragmentActivity implements ConnectionCallbacks, OnConnectionFailedListener {
    EventMapFragment mapFragment;
    WebFragment webFragment;
    Fragment currentFragment;
    View menuSelector, mapSelector, videoSelector;
    GoogleApiClient mGoogleApiClient;
    LatLng mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuSelector = findViewById(R.id.menu_selector);
        mapSelector = findViewById(R.id.map_selector);
        videoSelector = findViewById(R.id.video_selector);

        mapFragment = new EventMapFragment();
        webFragment = new WebFragment();

        webFragment.init(StaticResources.HTTP_PREFIX + StaticResources.ProdServer);

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    0 );
        }

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void hideSelectors() {
        menuSelector.setVisibility(View.GONE);
        mapSelector.setVisibility(View.GONE);
        videoSelector.setVisibility(View.GONE);
    }

    public void switchToMap(View v) {
        hideSelectors();
        mapSelector.setVisibility(View.VISIBLE);

        if (mapFragment.getCreateMode()) {
            mapFragment.setSearchMode();
        }
        else {
            if (currentFragment == mapFragment) return;
            mapFragment.setHomeLocation(mLastLocation);
            currentFragment = mapFragment;

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, mapFragment)
                    .commit();
        }
    }

    public void switchToWebView(View v) {
        hideSelectors();
        videoSelector.setVisibility(View.VISIBLE);

        mapFragment.setCreateMode(false);

        if (currentFragment == webFragment) return;
        currentFragment = webFragment;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, webFragment)
                .commit();
    }

    public void handleArrowClick(View v) {
        if (mapFragment.getCreateMode()) {
            mapFragment.submitNewEvent();
        }
        else {
            mapFragment.showSpinner();
            new EventJoiner(mapFragment).execute();
        }
    }


    public void createEventTab(View v) {
        hideSelectors();
        mapFragment.setPickLocationMode();
        menuSelector.setVisibility(View.VISIBLE);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void fetchEvents(View v) {
        mapFragment.fetchEvents();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("woops", "we don't have the permissions soz");
            return;
        }
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        mLastLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLatitude());
        if (mLastLocation != null) {
            switchToMap(null);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

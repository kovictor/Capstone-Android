package com.capstonappdeveloper.capstone_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.capstonappdeveloper.capstone_android.Protocol.Video.EventFetcher;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * For now, we're just swapping fragments into the framelayout "fragment_container"
 * but eventually we'll probably want to move to something like a viewPager
 * so that we can switch fragments while maintaining fragment state
 */
public class MainActivity extends FragmentActivity
    implements OnMapReadyCallback{
    SupportMapFragment mapFragment;
    LatLng homeLocation;
    GoogleMap map;
    WebFragment webFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        homeLocation = new LatLng(43.6532, -79.3832);
        switchToMap();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(homeLocation, 10.0f));
        new EventFetcher(map, homeLocation).execute();

    }

    public void switchToMap() {
        mapFragment = new SupportMapFragment();
        mapFragment.getMapAsync(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .commit();
    }

    public void switchToWebView(View v) {
        //switch to webview for viewing 3D model
        webFragment = new WebFragment();
        webFragment.init("http://ec2-54-71-87-84.us-west-2.compute.amazonaws.com/");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, webFragment)
                .commit();
    }

    public void onCameraButtonClick(View v) {
        System.out.println("In onCameraButtonClick");
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);
    }

    public void onMenuButtonClick(View v) {
        System.out.println("In onMenuButtonClick");
        /*Intent intent = new Intent(this, DisplayEventsActivity.class);
          startActivity(intent);
        */
    }

    public void testVideoUpload(View v) {
        //VideoFileNavigator.getVideoFromInternalStorage(this, "");
        //new VideoUploader().execute(VideoFileNavigator.getVideoFromInternalStorage(this, ""));
    }
}

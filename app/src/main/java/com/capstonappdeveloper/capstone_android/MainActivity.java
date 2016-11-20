package com.capstonappdeveloper.capstone_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.capstonappdeveloper.capstone_android.Protocol.Video.VideoFileNavigator;
import com.capstonappdeveloper.capstone_android.Protocol.Video.VideoUploader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * For now, we're just swapping fragments into the framelayout "fragment_container"
 * but eventually we'll probably want to move to something like a viewPager
 * so that we can switch fragments while maintaining fragment state
 */
public class MainActivity extends FragmentActivity
    implements OnMapReadyCallback{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switchToMap();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }

    public void switchToMap() {
        SupportMapFragment mapFragment = new SupportMapFragment();
        mapFragment.getMapAsync(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .commit();
    }

    public void switchToWebView(View v) {
        //switch to webview for viewing 3D model
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
        Log.d("TESTING VIDEO UPLOAD", "TESTING THIS SHIT MOTHERFUCKA");
        //VideoFileNavigator.getVideoFromInternalStorage(this, "");
        new VideoUploader().execute(VideoFileNavigator.getVideoFromInternalStorage(this, ""));
    }
}

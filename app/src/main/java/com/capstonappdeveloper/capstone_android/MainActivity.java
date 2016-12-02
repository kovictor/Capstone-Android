package com.capstonappdeveloper.capstone_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.capstonappdeveloper.capstone_android.Protocol.Video.VideoFileNavigator;
import com.capstonappdeveloper.capstone_android.Protocol.Video.VideoUploader;

/**
 * For now, we're just swapping fragments into the framelayout "fragment_container"
 * but eventually we'll probably want to move to something like a viewPager
 * so that we can switch fragments while maintaining fragment state
 */
public class MainActivity extends FragmentActivity {
    EventMapFragment mapFragment;
    WebFragment webFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switchToMap();
    }

    public void switchToMap() {
        if (mapFragment == null) {
            mapFragment = new EventMapFragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .commit();
    }

    public void switchToWebView(View v) {
        //switch to webview for viewing 3D model
        if (webFragment == null) {
            webFragment = new WebFragment();
            webFragment.init("http://ec2-54-71-87-84.us-west-2.compute.amazonaws.com/");
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, webFragment)
                .commit();
    }

    public void onMenuButtonClick(View v) {
        System.out.println("In onMenuButtonClick");
        /*Intent intent = new Intent(this, DisplayEventsActivity.class);
          startActivity(intent);
        */
    }

    public void testVideoUpload(View v) {
        //VideoFileNavigator.getVideoFromInternalStorage(this, "");
        new VideoUploader().execute(VideoFileNavigator.getVideoFromInternalStorage(this, ""));
    }

    public void onCameraButtonClick(View v) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);
    }
}

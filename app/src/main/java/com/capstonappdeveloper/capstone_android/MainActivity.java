package com.capstonappdeveloper.capstone_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
    Fragment currentFragment;
    View menuSelector, mapSelector, videoSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        menuSelector = findViewById(R.id.menu_selector);
        mapSelector = findViewById(R.id.map_selector);
        videoSelector = findViewById(R.id.video_selector);
        switchToMap(null);
    }

    public void hideSelectors() {
        menuSelector.setVisibility(View.GONE);
        mapSelector.setVisibility(View.GONE);
        videoSelector.setVisibility(View.GONE);
    }

    public void switchToMap(View v) {
        if (mapFragment == null) {
            mapFragment = new EventMapFragment();
        }

        if (currentFragment == mapFragment) return;
        currentFragment = mapFragment;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .commit();

        hideSelectors();
        mapSelector.setVisibility(View.VISIBLE);
    }

    public void switchToWebView(View v) {
        //switch to webview for viewing 3D model
        if (webFragment == null) {
            webFragment = new WebFragment();
            webFragment.init(StaticResources.HTTP_PREFIX + StaticResources.JamesServer);
        }

        if (currentFragment == webFragment) return;
        currentFragment = webFragment;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, webFragment)
                .commit();

        hideSelectors();
        videoSelector.setVisibility(View.VISIBLE);
    }

    public void onMenuButtonClick(View v) {
        System.out.println("In onMenuButtonClick");
        /*Intent intent = new Intent(this, DisplayEventsActivity.class);
          startActivity(intent);
        */
    }

    public void testVideoUpload(View v) {
        hideSelectors();
        menuSelector.setVisibility(View.VISIBLE);
        //VideoFileNavigator.getVideoFromInternalStorage(this, "");
        new VideoUploader().execute(VideoFileNavigator.getVideoFromInternalStorage(this, ""));
    }

    public void onCameraButtonClick(View v) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);
    }
}

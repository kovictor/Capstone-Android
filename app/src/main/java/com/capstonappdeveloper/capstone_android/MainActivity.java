package com.capstonappdeveloper.capstone_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

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

        mapFragment = new EventMapFragment();
        webFragment = new WebFragment();

        webFragment.init(StaticResources.HTTP_PREFIX + StaticResources.JamesServer);

        switchToMap(null);
    }

    public void hideSelectors() {
        menuSelector.setVisibility(View.GONE);
        mapSelector.setVisibility(View.GONE);
        videoSelector.setVisibility(View.GONE);
    }

    public void switchToMap(View v) {
        hideSelectors();
        mapSelector.setVisibility(View.VISIBLE);

        if (currentFragment == mapFragment) return;
        currentFragment = mapFragment;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .commit();
    }

    public void switchToWebView(View v) {
        hideSelectors();
        videoSelector.setVisibility(View.VISIBLE);

        if (currentFragment == webFragment) return;
        currentFragment = webFragment;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, webFragment)
                .commit();
    }

    public void onCameraButtonClick(View v) {
        System.out.println("In onCameraButtonClick");
        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
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
}

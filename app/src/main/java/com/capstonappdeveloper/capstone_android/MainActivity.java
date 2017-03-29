package com.capstonappdeveloper.capstone_android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.capstonappdeveloper.capstone_android.Protocol.Map.EventJoiner;

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

        webFragment.init(StaticResources.HTTP_PREFIX + StaticResources.ProdServer);
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

        if (mapFragment.getCreateMode()) {
            mapFragment.setSearchMode();
        }
        else {
            if (currentFragment == mapFragment) return;
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

    public void fetchEvents(View v) {
        mapFragment.fetchEvents();
    }
}

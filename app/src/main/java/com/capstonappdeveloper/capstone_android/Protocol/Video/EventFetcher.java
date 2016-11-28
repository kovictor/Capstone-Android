package com.capstonappdeveloper.capstone_android.Protocol.Video;

import android.os.AsyncTask;
import android.util.Log;

import com.capstonappdeveloper.capstone_android.StaticResources;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by james on 2016-11-28.
 */
public class EventFetcher extends AsyncTask<String, String, String> {

    private LatLng location;

    public EventFetcher(LatLng currentLocation) {
        this.location = currentLocation;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Shows Progress Bar Dialog and then call doInBackground method
    }

    @Override
    protected String doInBackground(String... sourceFileUri) {
        fetchEvents(this.location);
        return null;
    }

    String formURL(LatLng currentLocation) {
        return StaticResources.HTTP_PREFIX +
                StaticResources.JamesServer +
                StaticResources.GET_LOCAL_EVENTS_SCRIPT +
                "?longitude=" + currentLocation.longitude +
                "&latitude=" + currentLocation.latitude;
    }

    private void fetchEvents(LatLng currentLocation) {
        HttpURLConnection conn = null;
        try {
            Log.d("blach", formURL(currentLocation));
            URL url = new URL(formURL(currentLocation));

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStream in = conn.getInputStream();
            InputStreamReader isw = new InputStreamReader(in);

            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                        .getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    Log.i("Huzza", "RES capstone Message: " + line);
                }
                rd.close();
            } catch (IOException ioex) {
                Log.e("Huzza", "error capstone: " + ioex.getMessage(), ioex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(String file_url) {

    }

    protected void onProgressUpdate(String... progress) {

    }
}
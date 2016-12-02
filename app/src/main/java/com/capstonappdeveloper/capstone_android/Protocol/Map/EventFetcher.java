package com.capstonappdeveloper.capstone_android.Protocol.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.capstonappdeveloper.capstone_android.StaticResources;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 2016-11-28.
 */
public class EventFetcher extends AsyncTask<String, String, String> {

    private HashMap<String, Event> events;
    private LatLng location;
    private GoogleMap map;

    public EventFetcher(GoogleMap map, LatLng currentLocation, HashMap<String, Event> events) {
        this.location = currentLocation;
        this.map = map;
        this.events = events;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Shows Progress Bar Dialog and then call doInBackground method
    }

    @Override
    protected String doInBackground(String... string) {
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
            URL url = new URL(formURL(currentLocation));

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            String response = "";

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                response = response + line;
            }

            //parse JSON
            JSONArray array = new JSONArray(response);
            for(int i=0;i<array.length();i++) {
                JSONObject e = array.getJSONObject(i);

                String id = e.getString("id");
                double longitude = e.getDouble("longitude");
                double latitude = e.getDouble("latitude");
                URL iconUrl = new URL(e.getString("icon"));
                String eventName = e.getString("event");

                Bitmap bmp =
                        Bitmap.createScaledBitmap(
                            BitmapFactory.decodeStream(iconUrl.openConnection().getInputStream()),
                            50,
                            50,
                            false
                    );

                events.put(id, new Event(id, new LatLng(latitude, longitude), bmp, eventName));
            }
            rd.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(String string) {
        //grab the events and pin them on the map
        for (Map.Entry<String, Event> event : events.entrySet()) {
            String key = event.getKey();
            Event value = event.getValue();
            map.addMarker(new MarkerOptions()
                    .position(value.coordinates)
                    .title(value.eventName)
                    .icon(BitmapDescriptorFactory.fromBitmap(value.icon)))
                    .setSnippet(value.id);
        }
    }

    protected void onProgressUpdate(String... progress) {

    }
}
package com.capstonappdeveloper.capstone_android.Protocol.Video;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by james on 2016-11-28.
 */
public class EventFetcher extends AsyncTask<String, String, String> {

    private class Event {
        int id;
        double longitude;
        double latitude;
        Bitmap icon;
        public Event(int id, double longitude, double latitude, Bitmap icon) {
            this.id = id;
            this.longitude = longitude;
            this.latitude = latitude;
            this.icon = icon;
        }
    }

    private ArrayList<Event> rows;
    private LatLng location;
    private GoogleMap map;

    public EventFetcher(GoogleMap map, LatLng currentLocation) {
        this.location = currentLocation;
        this.map = map;
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
            rows = new ArrayList<Event>();
            JSONArray array = new JSONArray(response);
            for(int i=0;i<array.length();i++) {
                JSONObject e = array.getJSONObject(i);

                int id = e.getInt("id");
                double longitude = e.getDouble("longitude");
                double latitude = e.getDouble("latitude");
                URL iconUrl = new URL(e.getString("icon"));

                Bitmap bmp = BitmapFactory.decodeStream(iconUrl.openConnection().getInputStream());

                rows.add(new Event(id, longitude, latitude, bmp));
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
        for (Event event : rows) {
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(event.latitude, event.longitude))
                    .title("EVENT")
                    .icon(BitmapDescriptorFactory.fromBitmap(event.icon)));
        }
    }

    protected void onProgressUpdate(String... progress) {

    }
}
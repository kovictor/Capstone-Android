package com.capstonappdeveloper.capstone_android.Protocol.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.capstonappdeveloper.capstone_android.EventMapFragment;
import com.capstonappdeveloper.capstone_android.StaticResources;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by james on 2016-11-28.
 */
public class EventFetcher extends AsyncTask<String, String, String> {

    private HashMap<String, Event> events;
    private LatLng location;
    private GoogleMap map;
    private static final double range = 10.0;
    EventMapFragment mapFragment;

    public EventFetcher(EventMapFragment mapFragment) {
        this.location = mapFragment.getHomeLocation();
        this.map = mapFragment.getMap();
        this.events = mapFragment.getEvents();
        this.mapFragment = mapFragment;
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
                StaticResources.ProdServer +
                StaticResources.GET_LOCAL_EVENTS_SCRIPT +
                "?longitude=" + currentLocation.longitude +
                "&latitude=" + currentLocation.latitude +
                "&range=" + this.range;
    }

    private Bitmap scaleBitmap(Bitmap bmp, int dim) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        width = (width > height) ? dim : (int) (((double) width)/height * dim);
        height = (height > width) ? dim : (int) (((double) height)/width * dim);
        return Bitmap.createScaledBitmap(bmp, width, height, false);
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
                URL iconUrl = new URL(e.getString("thumbnail"));
                String eventName = e.getString("name");

                Bitmap bmp = scaleBitmap(
                        BitmapFactory.decodeStream(iconUrl.openConnection().getInputStream()),
                        StaticResources.mapThumbnailSize
                );

                Log.d("FOUND EVENT", id + ":" + eventName);
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
        if (events.isEmpty()) {
            return;
        }

        //grab the events and pin them on the map
        for (Event event : events.values()) {
            FirebaseMessaging.getInstance().subscribeToTopic(event.id);
            map.addMarker(new MarkerOptions()
                    .position(event.coordinates)
                    .title(event.eventName)
                    .icon(BitmapDescriptorFactory.fromBitmap(event.icon)))
                    .setSnippet(event.id);
        }

        //for now let's just zoom in on and focus on the event with id "test"
        Event eventOfInterest = events.get("test");

        map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        eventOfInterest.coordinates,
                        StaticResources.mapZoom
                )
        );
        mapFragment.setOverhead(eventOfInterest);
    }

    protected void onProgressUpdate(String... progress) {

    }
}
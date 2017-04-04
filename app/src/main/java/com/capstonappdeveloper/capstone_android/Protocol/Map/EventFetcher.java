package com.capstonappdeveloper.capstone_android.Protocol.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.capstonappdeveloper.capstone_android.EventMapFragment;
import com.capstonappdeveloper.capstone_android.Protocol.Video.BitmapLoader;
import com.capstonappdeveloper.capstone_android.R;
import com.capstonappdeveloper.capstone_android.StaticResources;
import com.capstonappdeveloper.capstone_android.WebFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by james on 2016-11-28.
 */
public class EventFetcher extends AsyncTask<String, String, String> {

    private HashMap<String, Event> events;
    private HashMap<String, Event> newEvents;
    private LatLng location;
    private GoogleMap map;
    private String eventID;
    private static final double range = 10.0;
    EventMapFragment mapFragment;
    WebFragment webFragment;
    Context c;

    public EventFetcher(EventMapFragment mapFragment) {
        this.location = mapFragment.getHomeLocation();
        this.map = mapFragment.getMap();
        this.events = mapFragment.getEvents();
        this.newEvents = new HashMap<String, Event>();
        this.mapFragment = mapFragment;
        this.c = mapFragment.getContext();
    }

    public EventFetcher(WebFragment webFragment) {
        this.location = null;
        this.newEvents = new HashMap<String, Event>();
        this.webFragment = webFragment;
        this.c = webFragment.getContext();
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

    String formURL() {
        return StaticResources.HTTP_PREFIX +
                StaticResources.ProdServer +
                StaticResources.GET_ARCHIVED_EVENTS_SCRIPT;
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
            String urlString = currentLocation != null ? formURL(currentLocation) : formURL();
            URL url = new URL(urlString);

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
            Log.d("JSON RESPONSE", response);

            // create a layoutInflater for marker bitmap creation
            View customMarkerView = ((LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.event_marker, null);

            JSONArray array = new JSONArray(response);
            for(int i=0;i<array.length();i++) {
                JSONObject e = array.getJSONObject(i);

                String id = eventID = e.getString("id");
                double longitude = e.getDouble("longitude");
                double latitude = e.getDouble("latitude");
                URL iconUrl = new URL(e.getString("thumbnail"));
                String eventName = e.getString("name");
                String timeCreated = null;
                int numParticipants = 0;
                String status = null;
                try {
                    timeCreated = e.getString("time_created");
                    numParticipants = e.getInt("num_participants");
                    status = e.getString("status");
                } catch (JSONException je) {
                    // If the field doesn't exist, it isn't the end of the world
                }

                /*
                Bitmap bmp = scaleBitmap(
                        BitmapFactory.decodeStream(iconUrl.openConnection().getInputStream()),
                        StaticResources.mapThumbnailSize
                );
                */
                int icon;
                switch (status) {
                    case "PROCESSING":
                        icon = R.drawable.processing;
                        break;
                    default:
                        icon = StaticResources.eventIcons[new Random().nextInt(StaticResources.numIcons)];
                        break;
                }
                Bitmap bmp = BitmapLoader.getMarkerBitmapFromView(icon, customMarkerView);

                Log.d("FOUND EVENT", id + ":" + eventName);
                newEvents.put(id, new Event(id, new LatLng(latitude, longitude), bmp, eventName, timeCreated, numParticipants, status));
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

    protected void onPostExecuteMap() {
        if (newEvents.isEmpty()) {
            mapFragment.showRetry();
            return;
        }

        //grab the events and pin them on the map
        for (Event event : newEvents.values()) {
            if (!events.containsKey(event.id)) {
                FirebaseMessaging.getInstance().subscribeToTopic(event.id);
                map.addMarker(new MarkerOptions()
                        .position(event.coordinates)
                        .title(event.eventName)
                        .icon(BitmapDescriptorFactory.fromBitmap(event.icon)))
                        .setSnippet(event.id);
                events.put(event.id, event);
            }
        }

        //for now let's just zoom in on and focus on the event with id "test"
        Event eventOfInterest = events.values().iterator().next();

        map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        eventOfInterest.coordinates,
                        StaticResources.mapZoom
                )
        );
        mapFragment.setOverhead(eventOfInterest);
        mapFragment.hideSpinner();
    }

    protected void onPostExecuteWeb() {
        webFragment.setEvents(newEvents);
        webFragment.setListViewContents();
        webFragment.updateUrl(StaticResources.HTTP_PREFIX + StaticResources.ProdServer + StaticResources.GET_POINT_MODEL + eventID);
    }

    @Override
    protected void onPostExecute(String string) {
        if (mapFragment != null) {
            onPostExecuteMap();
        }
        else {
            onPostExecuteWeb();
        }
    }

    protected void onProgressUpdate(String... progress) {

    }
}
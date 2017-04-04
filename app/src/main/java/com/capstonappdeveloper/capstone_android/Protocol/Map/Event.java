package com.capstonappdeveloper.capstone_android.Protocol.Map;

import android.graphics.Bitmap;
import android.util.Log;

import com.capstonappdeveloper.capstone_android.PlaybackActivity;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

/**
 * Created by james on 2016-12-01.
 */
public class Event {
    public String id;
    public LatLng coordinates;
    public Bitmap icon;
    public String eventName;
    public String timeCreated;
    public int numParticipants;
    public String status;
    public Event(String id, LatLng coordinates, Bitmap icon, String eventName) {
        this.id = id;
        this.coordinates = coordinates;
        this.icon = icon;
        this.eventName = eventName;
        this.timeCreated = null;
        this.numParticipants = 0;
    }

    public Event(String id, LatLng coordinates, Bitmap icon, String eventName, String timeCreated, int numParticipants, String status) {
        this.id = id;
        this.coordinates = coordinates;
        this.icon = icon;
        this.eventName = eventName;
        this.timeCreated = timeCreated;
        this.numParticipants = numParticipants;
        this.status = status;
    }

    public String getJSON() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("name", eventName);
            obj.put("latitude", coordinates.latitude);
            obj.put("longitude", coordinates.longitude);
            obj.put("frames", PlaybackActivity.NUM_IMAGES_PER_SEQUENCE);
            obj.put("thumbnail", "https://portal.mytum.de/pressestelle/pressemitteilungen/NewsArticle_20110406_151235/jacobsen_hans_arno.jpg");
            String result = obj.toString();
            Log.d("JSONIZE EVENT", result);
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}

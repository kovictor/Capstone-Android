package com.capstonappdeveloper.capstone_android.Protocol.Map;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by james on 2016-12-01.
 */
public class Event {
    public String id;
    public LatLng coordinates;
    public Bitmap icon;
    public String eventName;
    public Event(String id, LatLng coordinates, Bitmap icon, String eventName) {
        this.id = id;
        this.coordinates = coordinates;
        this.icon = icon;
        this.eventName = eventName;
    }
}

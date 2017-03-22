package com.capstonappdeveloper.capstone_android.Protocol.Map;

import android.os.AsyncTask;
import android.util.Log;

import com.capstonappdeveloper.capstone_android.EventMapFragment;
import com.capstonappdeveloper.capstone_android.StaticResources;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by james on 2017-03-21.
 */
public class EventJoiner extends AsyncTask<String, String, String> {

    private int numParticipants = 0;
    EventMapFragment mapFragment;

    public EventJoiner(EventMapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Shows Progress Bar Dialog and then call doInBackground method
    }

    @Override
    protected String doInBackground(String... string) {
        joinEvent();
        return null;
    }

    String formURL() {
        return StaticResources.HTTP_PREFIX +
                StaticResources.ProdServer +
                StaticResources.JOIN_EVENT_SCRIPT +
                mapFragment.getCurrentEventID();
    }

    private void joinEvent() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(formURL());

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            String response = "";

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                response = response + line;
            }

            Log.d("JOIN EVENT WITH RESULT", response);
            //parse JSON
            JSONObject obj = new JSONObject(response);
            this.numParticipants = obj.getInt("num_participants");
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
        mapFragment.hideSpinner();
        mapFragment.enterEvent(this.numParticipants);
    }
}
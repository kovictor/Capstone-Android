package com.capstonappdeveloper.capstone_android.Protocol.Map;

import android.os.AsyncTask;
import android.util.Log;

import com.capstonappdeveloper.capstone_android.StaticResources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by james on 2017-03-20.
 */

public class SynchronizeCapture extends AsyncTask<String, String, String> {

    private String event;

    public SynchronizeCapture(String event) {
        this.event = event;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Shows Progress Bar Dialog and then call doInBackground method
    }

    @Override
    protected String doInBackground(String... string) {
        sendSynchronizeCaptureBroadcast();
        return null;
    }

    String formURL() {
        return StaticResources.HTTP_PREFIX +
                StaticResources.ProdServer +
                StaticResources.SYNCHRONIZE_CAPTURE_SCRIPT +
                "?topic=" + event;
    }

    private void sendSynchronizeCaptureBroadcast() {
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
            Log.d("BROADCAST MESSAGE", response);
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

    }

    protected void onProgressUpdate(String... progress) {

    }
}
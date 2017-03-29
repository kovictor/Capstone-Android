package com.capstonappdeveloper.capstone_android.Protocol.Map;

/**
 * Created by james on 2017-01-23.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.capstonappdeveloper.capstone_android.EventMapFragment;
import com.capstonappdeveloper.capstone_android.PlaybackActivity;
import com.capstonappdeveloper.capstone_android.StaticResources;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This is basically some sketchy copy-pasta'd code that I'm trying out
 * Uploads a video from Android to server
 * TODO:We'd ideally like to also perform a number of checks for correct file format/video length
 */
public class CreateEventUploader extends AsyncTask<String, String, String> {
    //this currently points to my test server, change accordingly
    private static String TWO_HYPHENS = "--";
    private static String BOUNDARY = "*****";
    private static String LINE_END = "\r\n";
    private static String AMPERSAND = "&";
    EventMapFragment mapFragment;
    private Event event;
    //set the max buffer size to 100MB, as specified on our apache server in /etc/php.ini
    private static int MAX_BUFFER_SIZE = 100 * ((2 << 19) - 1);

    public CreateEventUploader(EventMapFragment mapFragment, Event event) {
        this.mapFragment = mapFragment;
        this.event = event;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Shows Progress Bar Dialog and then call doInBackground method
    }

    @Override
    protected String doInBackground(String...s) {
        uploadEvent();
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        mapFragment.hideSpinner();

        mapFragment.setPickLocationMode();
    }

    protected void onProgressUpdate(String... progress) {

    }

    private URL formURL() {
        try {
            return new URL(StaticResources.HTTP_PREFIX +
                    StaticResources.ProdServer +
                    StaticResources.CREATE_EVENT_SCRIPT +
                    this.event.id);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private void uploadEvent() {
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        int bytesRead, bytesAvailable, bufferSize;
        int serverResponseCode = 400;
        byte[] buffer;
        String responseFromServer = "";

        try { // open a URL connection to the Servlet
            URL url = formURL();
            conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes("id=" + event.id + AMPERSAND);
            dos.writeBytes("name=" + event.eventName + AMPERSAND);
            dos.writeBytes("latitude=" + event.coordinates.latitude + AMPERSAND);
            dos.writeBytes("longitude=" + event.coordinates.longitude + AMPERSAND);
            dos.writeBytes("frames=" + PlaybackActivity.NUM_IMAGES_PER_SEQUENCE + AMPERSAND);
            dos.writeBytes("thumbnail=" + "http://vignette2.wikia.nocookie.net/shrek/images/c/cc/Shrek_smiling.jpg");


            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("Upload file to server", "HTTP capstone Response is : " + serverResponseMessage + ": " + serverResponseCode);

            dos.flush();
            dos.close();
            //this block will give the response of upload link
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                    .getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                Log.i("Huzza", "RES capstone Message: " + line);
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.capstonappdeveloper.capstone_android.Protocol.Video;

import android.os.AsyncTask;
import android.util.Log;

import com.capstonappdeveloper.capstone_android.StaticResources;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This is basically some sketchy copy-pasta'd code that I'm trying out
 * Uploads a video from Android to server
 * TODO:We'd ideally like to also perform a number of checks for correct file format/video length
 */
public class VideoUploader extends AsyncTask<String, String, String> {
    //this currently points to my test server, change accordingly
    private static String TWO_HYPHENS = "--";
    private static String BOUNDARY = "*****";
    private static String LINE_END = "\r\n";
    //set the max buffer size to 100MB, as specified on our apache server in /etc/php.ini
    private static int MAX_BUFFER_SIZE = 100 * ((2 << 19) - 1);

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Shows Progress Bar Dialog and then call doInBackground method
    }

    @Override
    protected String doInBackground(String... sourceFileUri) {
        uploadVideo(sourceFileUri[0]);
        return null;
    }

    @Override
    protected void onPostExecute(String file_url) {

    }

    protected void onProgressUpdate(String... progress) {

    }

    private void uploadVideo(String sourceFileUri) {
        // String [] string = sourceFileUri;
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        int bytesRead, bytesAvailable, bufferSize;
        int serverResponseCode = 400;
        byte[] buffer;
        String responseFromServer = "";

        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {
            Log.e("Huzza", "Source File Does not exist: " + sourceFileUri);
            return;
        }
        try { // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size
            Log.d("Huzza", "Initial capstone .available : " + bytesAvailable);

            bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
            URL url = new URL(StaticResources.HTTP_PREFIX +
                              StaticResources.JamesServer +
                              StaticResources.VIDEO_UPLOAD_SCRIPT);
            conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            conn.setRequestProperty("file", fileName);
            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""+ fileName + "\"" + LINE_END);
            dos.writeBytes(LINE_END);

            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(LINE_END);
            dos.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("Upload file to server", "HTTP capstone Response is : " + serverResponseMessage + ": " + serverResponseCode);
            // close streams
            Log.i("Upload file to server", fileName + " File capstone is written");
            fileInputStream.close();
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

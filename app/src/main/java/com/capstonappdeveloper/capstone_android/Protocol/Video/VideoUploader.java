package com.capstonappdeveloper.capstone_android.Protocol.Video;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This is basically some sketchy copy-pasta'd code that I'm trying out
 * Uploads a video from Android to server
 * TODO:We'd ideally like to also perform a number of checks for correct file format/video length
 */
public class VideoUploader {
    private static String SERVER_URI = "ec2-54-71-87-84.us-west-2.compute.amazonaws.com/src/php/uploadVideo.php";
    private static String TWO_HYPHENS = "--";
    private static String BOUNDARY = "*****";
    private static String LINE_END = "\r\n";
    private static int MAX_BUFFER_SIZE = 1 * 1024 * 1024;

    public static HttpURLConnection prepareConnectionForUpload(URL url, String fileName) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
        } catch(Exception e) {
            //damn something went wrong?
            return null;
        }
        conn.setDoInput(true); // Allow Inputs
        conn.setDoOutput(true); // Allow Outputs
        conn.setUseCaches(false); // Don't use a Cached Copy
        try {
            conn.setRequestMethod("POST");
        } catch(Exception e) {
            //this technically should never fail
            //just disregard this exception
        }
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
        conn.setRequestProperty("uploaded_file", fileName);
        return conn;
    }

    public static int uploadVideo(String sourceFileUri) {
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
            Log.e("Huzza", "Source File Does not exist");
            return 0;
        }
        try { // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(SERVER_URI);
            conn = prepareConnectionForUpload(url, fileName);
            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ fileName + "\"" + LINE_END);
            dos.writeBytes(LINE_END);

            bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size
            Log.i("Huzza", "Initial .available : " + bytesAvailable);

            bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
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

            Log.i("Upload file to server", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
            // close streams
            Log.i("Upload file to server", fileName + " File is written");
            fileInputStream.close();
            dos.flush();
            dos.close();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //this block will give the response of upload link
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                    .getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                Log.i("Huzza", "RES Message: " + line);
            }
            rd.close();
        } catch (IOException ioex) {
            Log.e("Huzza", "error: " + ioex.getMessage(), ioex);
        }
        return serverResponseCode;  // like 200 (Ok)
    }
}

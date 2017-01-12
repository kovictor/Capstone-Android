package com.capstonappdeveloper.capstone_android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by james on 2016-11-28.
 */
public class StaticResources {
    public static String HTTP_PREFIX = "http://";

    //server URIs
    public static String ProdServer = "ec2-35-163-6-36.us-west-2.compute.amazonaws.com";
    public static String JamesServer = "ec2-54-71-87-84.us-west-2.compute.amazonaws.com";
    public static String AlvinServer = "ec2-54-71-150-203.us-west-2.compute.amazonaws.com";
    public static String VictorServer = "ec2-54-70-80-134.us-west-2.compute.amazonaws.com";
    public static String TomServer = "ec2-54-190-4-165.us-west-2.compute.amazonaws.com";

    //scripts
    public static String VIDEO_UPLOAD_SCRIPT = "/src/php/uploadVideo.php";
    public static String GET_LOCAL_EVENTS_SCRIPT = "/src/php/db/get_events.php";

    //icon sizes
    public static int mapThumbnailSize = 250;

    public static int mapZoom = 14;

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

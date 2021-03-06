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

    //scripts
    public static String VIDEO_UPLOAD_SCRIPT = "/event/upload/";
    public static String GET_LOCAL_EVENTS_SCRIPT = "/events";
    public static String GET_ARCHIVED_EVENTS_SCRIPT = "/events/archive";
    public static String CREATE_EVENT_SCRIPT = "/event/";
    public static String JOIN_EVENT_SCRIPT = "/event/join/";
    public static String LEAVE_EVENT_SCRIPT = "/event/leave/";
    public static String SYNCHRONIZE_CAPTURE_SCRIPT = "/broadcast";
    public static String BEGIN_IMAGE_CAPTURE = "begin_image_capture";

    public static String GET_MESH_MODEL = "/model/mesh/";
    public static String GET_POINT_MODEL = "/model/point/";

    // Icon sizes
    public static int mapThumbnailSize = 250;

    // Resource IDs for random icons
    public static int eventIcons[] = {R.drawable.event_icon1, R.drawable.event_icon2, R.drawable.event_icon3, R.drawable.event_icon4, R.drawable.event_icon5};
    public static int numIcons = 5;

    public static int mapZoom = 14;

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

package com.capstonappdeveloper.capstone_android.Protocol.Video;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by james on 2016-11-08.
 */
public class VideoFileNavigator {
    public static String getVideoFromInternalStorage(Context c, String fileName) {
        Log.d("INSIDE DIRECTORY", "LOOKING AT FILES IN VIDEOS DIRECTORY");
        File rootsd = Environment.getExternalStorageDirectory();
        File dcim = new File(rootsd.getAbsolutePath() + "/DCIM");
        for (String file : dcim.list()) {
            Log.d("YO WTF", "PRINTING FILES IN VIDEOS DIRECTORY: " + file);
        }
        //fileWithinMyDir.setReadable(true, false);
        //return video.getPath();
        return "";
    }
}

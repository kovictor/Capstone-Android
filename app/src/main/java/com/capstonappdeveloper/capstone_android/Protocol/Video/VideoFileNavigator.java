package com.capstonappdeveloper.capstone_android.Protocol.Video;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by james on 2016-11-08.
 */
public class VideoFileNavigator {
    //right now all this does is find a random file to test uploading...
    //we still need to figure out where we're saving our videos to know where
    //we should be grabbing them from
    // right now looks in DCIM/Camera
    public static String getVideoFromInternalStorage(Context c, String fileName) {
        File rootsd = Environment.getExternalStorageDirectory();
        File dcim = new File(rootsd.getAbsolutePath() + "/DCIM/Camera");
        for (String file : dcim.list()) {
            //grab the first random image available
            if (file.contains(".jpg")) return rootsd.getAbsolutePath() + "/DCIM/Camera/" + file;
        }
        return "";
    }
}

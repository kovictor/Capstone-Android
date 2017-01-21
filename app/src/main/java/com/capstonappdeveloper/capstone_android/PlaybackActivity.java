package com.capstonappdeveloper.capstone_android;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.capstonappdeveloper.capstone_android.Protocol.Video.BitmapLoader;

import java.io.File;
import java.util.ArrayList;

public class PlaybackActivity extends AppCompatActivity {
    public static String FILE_PATH_EXTRA = "FILE_PATH";
    public static String FILE_NAME_BASE = "FILE_NAME";
    public static int NUM_IMAGES_PER_SEQUENCE = 10;
    private static int IMAGE_SWITCH_DELAY = 200; //miliseconds
    private static int IMAGE_RESOLUTION = 100; //scaling bitmaps to save memory
    ImageView playbackView;
    ArrayList<Bitmap> imageList;
    private ProgressBar dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playback);
        dialog = (ProgressBar) findViewById(R.id.load_spinner);
        showSpinner();
        imageList = new ArrayList<Bitmap>();
        playbackView = (ImageView) findViewById(R.id.playback_view);
        getImageList();

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                    playbackView.setImageBitmap(imageList.get(0));
                    Bitmap b = imageList.get(0);
                    imageList.remove(0);
                    imageList.add(b);
                    handler.postDelayed(this, IMAGE_SWITCH_DELAY);
            }
        };
        handler.post(runnable);
    }

    private void getImageList() {
        String filePath = getIntent().getStringExtra(FILE_PATH_EXTRA);
        String fileNameBase = getIntent().getStringExtra(FILE_NAME_BASE);
        for (int i = 0; i < NUM_IMAGES_PER_SEQUENCE; i++) {
            loadImageFromStorage(filePath, fileNameBase + "_" + i);
        }
        hideSpinner();
    }

    protected void showSpinner() {
        dialog.setVisibility(View.VISIBLE);
    }

    protected void hideSpinner() {
        dialog.setVisibility(View.GONE);
    }

    private void loadImageFromStorage(String filePath, String fileName)
    {
        File f = new File(filePath, fileName);
        Bitmap b = BitmapLoader.loadBitmap(filePath + "/" + fileName, IMAGE_RESOLUTION, IMAGE_RESOLUTION);
        imageList.add(b);
    }
}

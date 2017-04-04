package com.capstonappdeveloper.capstone_android.Protocol.Video;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.ImageView;

import com.capstonappdeveloper.capstone_android.R;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by james on 2017-01-19.
 */
public class BitmapLoader
{
    public static int getScale(int originalWidth,int originalHeight,
                               final int requiredWidth,final int requiredHeight)
    {
        //a scale of 1 means the original dimensions
        //of the image are maintained
        int scale=1;

        //calculate scale only if the height or width of
        //the image exceeds the required value.
        if((originalWidth>requiredWidth) || (originalHeight>requiredHeight))
        {
            //calculate scale with respect to
            //the smaller dimension
            if(originalWidth<originalHeight)
                scale=Math.round((float)originalWidth/requiredWidth);
            else
                scale=Math.round((float)originalHeight/requiredHeight);

        }

        return scale;
    }

    public static BitmapFactory.Options getOptions(String filePath,
                                                   int requiredWidth,int requiredHeight)
    {

        BitmapFactory.Options options=new BitmapFactory.Options();
        //setting inJustDecodeBounds to true
        //ensures that we are able to measure
        //the dimensions of the image,without
        //actually allocating it memory
        options.inJustDecodeBounds=true;

        //decode the file for measurement
        BitmapFactory.decodeFile(filePath,options);

        //obtain the inSampleSize for loading a
        //scaled down version of the image.
        //options.outWidth and options.outHeight
        //are the measured dimensions of the
        //original image
        options.inSampleSize=getScale(options.outWidth,
                options.outHeight, requiredWidth, requiredHeight);

        //set inJustDecodeBounds to false again
        //so that we can now actually allocate the
        //bitmap some memory
        options.inJustDecodeBounds=false;

        return options;

    }

    public static int[] convertYUV420_NV21toRGB8888(byte [] data, int width, int height) {
        int size = width*height;
        int offset = size;
        int[] pixels = new int[size];
        int u, v, y1, y2, y3, y4;

        // i percorre os Y and the final pixels
        // k percorre os pixles U e V
        for(int i=0, k=0; i < size; i+=2, k+=2) {
            y1 = data[i  ]&0xff;
            y2 = data[i+1]&0xff;
            y3 = data[width+i  ]&0xff;
            y4 = data[width+i+1]&0xff;

            u = data[offset+k  ]&0xff;
            v = data[offset+k+1]&0xff;
            u = u-128;
            v = v-128;

            pixels[i  ] = convertYUVtoRGB(y1, u, v);
            pixels[i+1] = convertYUVtoRGB(y2, u, v);
            pixels[width+i  ] = convertYUVtoRGB(y3, u, v);
            pixels[width+i+1] = convertYUVtoRGB(y4, u, v);

            if (i!=0 && (i+2)%width==0)
                i+=width;
        }

        return pixels;
    }

    private static int convertYUVtoRGB(int y, int u, int v) {
        int r,g,b;

        r = y + (int)1.402f*v;
        g = y - (int)(0.344f*u +0.714f*v);
        b = y + (int)1.772f*u;
        r = r>255? 255 : r<0 ? 0 : r;
        g = g>255? 255 : g<0 ? 0 : g;
        b = b>255? 255 : b<0 ? 0 : b;
        return 0xff000000 | (b<<16) | (g<<8) | r;
    }

    public static Bitmap loadBitmap(String filePath,
                                    int requiredWidth, int requiredHeight){
        BitmapFactory.Options options= getOptions(filePath,
                requiredWidth, requiredHeight);

        return BitmapFactory.decodeFile(filePath,options);
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    static public void decodeYUV420SP(int[] rgba, byte[] yuv420sp, int width,
                                      int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                // rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) &
                // 0xff00) | ((b >> 10) & 0xff);
                // rgba, divide 2^10 ( >> 10)
                rgba[yp] = ((r << 14) & 0xff000000) | ((g << 6) & 0xff0000)
                        | ((b >> 2) | 0xff00);
            }
        }
    }

    public static Bitmap loadBitmapFromYUV(String filePath,
                                           int requiredWidth, int requiredHeight) {
        int[] intArray = new int[requiredHeight * requiredWidth];

        byte[] data = null;
        try {
            data = readFile(new File(filePath));
        } catch (Exception e) {
            //k
            return null;
        }
        // Decode Yuv data to integer array
        //decodeYUV420SP(intArray, data, requiredWidth, requiredHeight);
        intArray = convertYUV420_NV21toRGB8888(data, requiredWidth, requiredHeight);

        //Initialize the bitmap, with the replaced color
        return Bitmap.createBitmap(intArray, requiredWidth, requiredHeight, Bitmap.Config.ARGB_8888);
    }


    public static Bitmap getMarkerBitmapFromView(@DrawableRes int resId, View customMarkerView) {
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.event_marker_image);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }
}
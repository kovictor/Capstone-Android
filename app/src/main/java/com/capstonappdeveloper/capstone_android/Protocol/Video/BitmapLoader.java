package com.capstonappdeveloper.capstone_android.Protocol.Video;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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



    public static Bitmap loadBitmap(String filePath,
                                    int requiredWidth, int requiredHeight){


        BitmapFactory.Options options= getOptions(filePath,
                requiredWidth, requiredHeight);

        return BitmapFactory.decodeFile(filePath,options);
    }
}
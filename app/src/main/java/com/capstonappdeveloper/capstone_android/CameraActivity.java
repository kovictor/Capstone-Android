package com.capstonappdeveloper.capstone_android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Victor on 11/29/2016.
 * Base Code from https://github.com/stereoboy/android_samples/tree/master/SimpleCamera
 */


public class CameraActivity extends Activity {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final int ACTIVITY_START_CAMERA_APP = 0;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CAMERA_RESULT = 2;
    private int mState;
    private ImageView mPhotoCapturedImageView;
    private String mImageFileLocation = "";
    private String GALLERY_LOCATION = "image_gallery";
    private File mGalleryFolder;
    private static LruCache<String, Bitmap> mMemoryCache;
    private RecyclerView mRecyclerView;
    private Size mPreviewSize;
    private String mCameraId;
    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    setupCamera(width, height);
                    openCamera();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            };

    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback
            = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            //Toast.makeText(getApplicationContext(), "Camera Opened", Toast.LENGTH_LONG).show();
            //System.out.println("IT WORKS THE CAMERA IS BEING OPENED!!");
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private CaptureRequest mPreviewCaptureRequest;
    private CaptureRequest.Builder mPreviewCaptureRequestBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result){
            switch (mState) {
                case STATE_PREVIEW:
                    //do nothing, no act
                    break;
                case STATE_WAIT_LOCK:
                    System.out.println("-----------in StateWaitLock---------");
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    //if(afState == CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED){
                        System.out.println("------------Camera Focus Locked Successful------------------");
                        captureStillImage();
                        unLockFocus();
                    //}
                    break;
            }
        }
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            process(result);
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);

            System.out.println("------------------Camera Focus UNsuccessful ---------------");
        }
    };

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for( String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                Size largestImageSize = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new Comparator<Size>() {
                            @Override
                            public int compare(Size o1, Size o2) {
                                return Long.signum(o1.getWidth() * o1.getHeight() - o2.getHeight() * o2.getWidth());
                            }
                        }
                );
                mImageReader = ImageReader.newInstance(largestImageSize.getWidth(),
                        largestImageSize.getHeight(),
                        ImageFormat.JPEG,
                        1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,
                        mBackgroundHandler);

                mPreviewSize = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size getPreferredPreviewSize(Size [] mapSizes, int width, int height) {
        List<Size> collectorSizes = new ArrayList<>();
        for(Size option : mapSizes){
            if(width > height) {
                if(option.getWidth() > width && option.getHeight() > height) {
                    collectorSizes.add(option);
                }
            } else {
                if(option.getWidth() > height && option.getHeight() > width){
                    collectorSizes.add(option);
                }
            }
        }
        if(collectorSizes.size() > 0) {
            return Collections.min(collectorSizes, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return mapSizes[0];
    }

    @Override
    public void onResume() {
        super.onResume();
        openBackgroundThread();

        if(mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause(){
        System.out.println("---------------- On Pause --------------");
        closeCamera();

        closeBackgroundThread();
        super.onPause();
    }

    @Override
    public void onDestroy(){
        //super.onDestroy();
        System.out.println("----------------On Destroy --------------");

        //closeCamera();
        //closeBackgroundThread();
        super.onDestroy();
        //closeBackgroundThread();

    }

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private static File mImageFile;
    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader){
                    mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage()));
                }
            };

    private static class ImageSaver implements Runnable {

        private final Image mImage;

        private ImageSaver(Image image){
            mImage = image;
        }
        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte [] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(mImageFile);
                fileOutputStream.write(bytes);
            } catch(IOException e){
                e.printStackTrace();
            } finally {
                mImage.close();
                if(fileOutputStream!=null){
                    try {
                        fileOutputStream.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara_intent);

        //askForPermission();

        createImageGallery();

        mRecyclerView = (RecyclerView) findViewById(R.id.galleryRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        //mRecyclerView.setLayoutManager(layoutManager);
        //RecyclerView.Adapter imageAdapter = new ImageAdapter(mGalleryFolder);
        //mRecyclerView.setAdapter(imageAdapter);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                if(!mGalleryFolder.exists()) {
                    mGalleryFolder.mkdirs();
                }
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this, "No Permission to use the Camera Service", Toast.LENGTH_LONG).show();
                }
                System.out.println("--------requesting permission of write external storage ---------");
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        else {
            if(!mGalleryFolder.exists()) {
                mGalleryFolder.mkdirs();
            }
        }

        final int maxMemorySize = (int) Runtime.getRuntime().maxMemory()/1024;
        final int cacheSize = maxMemorySize / 10;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };

        mTextureView = (TextureView) findViewById(R.id.textureView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera_intent, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void takePhoto(View view) {
        /*
        Intent callCameraApplicationIntent = new Intent();
        callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            photoFile = createImageFile();

        } catch (IOException e) {
            e.printStackTrace();
        }
        callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));

        startActivityForResult(callCameraApplicationIntent, ACTIVITY_START_CAMERA_APP);
        */

        try {
            mImageFile = createImageFile();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Button clicked, Locking Focus");
        lockFocus();
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if(requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
            // Toast.makeText(this, "Picture taken successfully", Toast.LENGTH_SHORT).show();
            // Bundle extras = data.getExtras();
            // Bitmap photoCapturedBitmap = (Bitmap) extras.get("data");
            // mPhotoCapturedImageView.setImageBitmap(photoCapturedBitmap);
            // Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(mImageFileLocation);
            // mPhotoCapturedImageView.setImageBitmap(photoCapturedBitmap);
            // setReducedImageSize();
            //RecyclerView.Adapter newImageAdapter = new ImageAdapter(mGalleryFolder);
            //mRecyclerView.swapAdapter(newImageAdapter, false);

        }
    }

    private void createImageGallery() {
        //TODO Need to ask for file permissions for new versions of android on runtime.
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mGalleryFolder = new File(storageDirectory, GALLERY_LOCATION);
        System.out.println(mGalleryFolder);
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                if(!mGalleryFolder.exists()) {
                    mGalleryFolder.mkdirs();
                }
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this, "No Permission to use the Camera Service", Toast.LENGTH_LONG).show();
                }
                System.out.println("--------requesting permission of write external storage ---------");
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        else {
            if(!mGalleryFolder.exists()) {
                mGalleryFolder.mkdirs();
            }
        }*/
        if(!mGalleryFolder.exists()) {
            mGalleryFolder.mkdirs();
        }

    }

    File createImageFile() throws IOException {

        System.out.println("---------------- In Create Image File---------------");
        createImageGallery();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";

        File image = new File(mGalleryFolder, imageFileName+".jpg");
        mImageFileLocation = image.getAbsolutePath();

        return image;

    }

    void setReducedImageSize() {
        int targetImageViewWidth = mPhotoCapturedImageView.getWidth();
        int targetImageViewHeight = mPhotoCapturedImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImageFileLocation, bmOptions);
        int cameraImageWidth = bmOptions.outWidth;
        int cameraImageHeight = bmOptions.outHeight;

        int scaleFactor = Math.min(cameraImageWidth/targetImageViewWidth, cameraImageHeight/targetImageViewHeight);
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inJustDecodeBounds = false;

        Bitmap photoReducedSizeBitmp = BitmapFactory.decodeFile(mImageFileLocation, bmOptions);
        mPhotoCapturedImageView.setImageBitmap(photoReducedSizeBitmp);
    }

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            Log.v("Camera", mCameraId + " " + mCameraDeviceStateCallback);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                } else {
                    if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                        Toast.makeText(this, "No Permission to use the Camera Service", Toast.LENGTH_LONG).show();
                    }
                    requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
                }
            }
            else {
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case REQUEST_CAMERA_RESULT:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("-----------Don't have permission to use the camera on the device----------");
                }
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //RecyclerView.Adapter imageAdapter = new ImageAdapter(sortFilesToLatest(mGalleryFolder), this);
                   //mRecyclerView.setAdapter(imageAdapter);
                } else {
                    System.out.println("--------------Don't have permission to start the gallery or save images-----------");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }*/

    private void createCameraPreviewSession(){
        try {
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            //capture request
            mPreviewCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewCaptureRequestBuilder.addTarget(previewSurface);
            //creating session
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            if(mCameraDevice == null){
                                return;
                            }
                            try {
                                mPreviewCaptureRequest = mPreviewCaptureRequestBuilder.build();
                                mCameraCaptureSession = session;
                                mCameraCaptureSession.setRepeatingRequest(
                                        mPreviewCaptureRequest,
                                        mSessionCaptureCallback,
                                        mBackgroundHandler
                                );
                            } catch(CameraAccessException e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Toast.makeText(getApplicationContext(), "create camera session failed", Toast.LENGTH_LONG).show();
                        }
                    }, null);
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera(){
        if(mCameraCaptureSession!=null){
            mCameraCaptureSession.close();
            mCameraCaptureSession=null;
        }
        if (mCameraDevice!=null){
            mCameraDevice.close();
            mCameraDevice=null;
        }
        if(mImageReader!= null){
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void openBackgroundThread(){
        System.out.println("----------------Opening Background Thread---------------");
        mBackgroundThread = new HandlerThread("Camera2 background thread");
        mBackgroundThread.start();
        mBackgroundHandler = new android.os.Handler(mBackgroundThread.getLooper());
    }

    private void closeBackgroundThread(){
        System.out.println("----------------Closing Background Thread---------------");
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    private void lockFocus(){
        try {
            mState = STATE_WAIT_LOCK;
            mPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CaptureRequest.CONTROL_AF_TRIGGER_START);
            mCameraCaptureSession.capture(mPreviewCaptureRequestBuilder.build(),
                    mSessionCaptureCallback, mBackgroundHandler);
        } catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void unLockFocus(){
        try {
            mState = STATE_PREVIEW;
            mPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
            mCameraCaptureSession.capture(mPreviewCaptureRequestBuilder.build(),
                    mSessionCaptureCallback, mBackgroundHandler);
        } catch(CameraAccessException e){
            e.printStackTrace();
        }
    }
    public static Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    public static void setBitmapToMemoryCache(String key, Bitmap bitmap) {
        if(getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private void captureStillImage() {
        try {
            System.out.println("--------------------------in Capture still image -------------------------");
            CaptureRequest.Builder captureStillBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureStillBuilder.addTarget(mImageReader.getSurface());

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureStillBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            CameraCaptureSession.CaptureCallback captureCallback =
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                            super.onCaptureCompleted(session, request, result);
                            System.out.println("--------------------------Image has been Taken -------------------------");
                            unLockFocus();
                        }
                    };
            mCameraCaptureSession.capture(
                    captureStillBuilder.build(), captureCallback, null
            );


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void askForPermission(){
        System.out.println("breaking in here");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                //do nothing, both already granted
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this, "No Permission to use the Camera Service", Toast.LENGTH_LONG).show();
                }
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 10);
            }
        }
        System.out.println("breaking in here");
    }
}
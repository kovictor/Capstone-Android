package com.capstonappdeveloper.capstone_android;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.TextView;
import android.widget.Toast;

import com.capstonappdeveloper.capstone_android.Protocol.Map.SynchronizeCapture;
import com.capstonappdeveloper.capstone_android.Protocol.Video.VideoUploader;

import java.io.File;
import java.io.FileNotFoundException;
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

    public static final String CURRENT_EVENT_ID = "current_event_id";
    public static final String CURRENT_EVENT_NAME = "current_event_name";
    public static final String NUM_PARTICIPANTS = "num_participants";
    public static final int IMAGE_FORMAT = ImageFormat.JPEG;
    private static final int ACTIVITY_START_CAMERA_APP = 0;
    private static enum CAPTURE_STATE {
        PREVIEW,
        WAIT_LOCK,
        CAPTURE
    }

    private int mPictureCount;
    private CAPTURE_STATE mState;
    private static String mImageFileLocation = "";
    private String GALLERY_LOCATION = "image_gallery";
    private static File mGalleryFolder;
    private static LruCache<String, Bitmap> mMemoryCache;
    private RecyclerView mRecyclerView;
    private String mImageHash;
    private Size mPreviewSize;
    private String mCameraId;
    private String eventID;
    private String eventName;
    private int numParticipants;
    private TextureView mTextureView;
    private TextView mTitle;
    private TextView mParticipantCount;
    private CaptureRequest mPreviewCaptureRequest;
    private CaptureRequest.Builder mPreviewCaptureRequestBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private ImageReader mImageReader;
    private Surface mPreviewSurface;
    private ImageCaptureReceiver mBroadcastReceiver;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader){
                    HandlerThread newThread = new HandlerThread("");
                    newThread.start();
                    Handler newHandler = new android.os.Handler(newThread.getLooper());
                    newHandler.post(new ImageSaver(reader.acquireNextImage(), mImageHash, mPictureCount));
                }
            };

    private class ImageCaptureReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(StaticResources.BEGIN_IMAGE_CAPTURE)) {
                takePhoto(intent.getStringExtra(CURRENT_EVENT_ID));
                Log.d("BROADCAST RECEIVED", "STARTING THE SYNCHRONIZED IMAGE CAPTURE NOW");
            }
        }
    }

    private static class ImageSaver implements Runnable {

        private final Image mImage;
        private final String mImageHash;
        private final int mSequenceNum;

        private ImageSaver(Image image, String imageHash, int sequenceNum){
            mImage = image;
            mImageHash = imageHash;
            mSequenceNum = sequenceNum;
        }
        @Override
        public void run() {
            byte[] bytes;
            File file = null;
            try {
                file = createImageFile(mImageHash, mSequenceNum);
            } catch (IOException e) {
                return;
            }
            switch (mImage.getFormat()){
                // YUV_420_888 images are saved in a format of our own devising. First write out the
                // information necessary to reconstruct the image, all as ints: width, height, U-,V-plane
                // pixel strides, and U-,V-plane row strides. (Y-plane will have pixel-stride 1 always.)
                // Then directly place the three planes of byte data, uncompressed.
                //
                // Note the YUV_420_888 format does not guarantee the last pixel makes it in these planes,
                // so some cases are necessary at the decoding end, based on the number of bytes present.
                // An alternative would be to also encode, prior to each plane of bytes, how many bytes are
                // in the following plane. Perhaps in the future.
                case ImageFormat.YUV_420_888:
                    FileOutputStream output = null;
                    ByteBuffer buffer;

                    boolean success = false;
                    // "prebuffer" simply contains the meta information about the following planes.
                    ByteBuffer prebuffer = ByteBuffer.allocate(16);
                    System.out.println("WIDTH:" + mImage.getWidth() + " HEIGHT:"+mImage.getHeight());
                    prebuffer.putInt(mImage.getWidth())
                            .putInt(mImage.getHeight())
                            .putInt(mImage.getPlanes()[1].getPixelStride())
                            .putInt(mImage.getPlanes()[1].getRowStride());

                    try {
                        output = new FileOutputStream(file);
                        output.write(prebuffer.array()); // write meta information to file
                        // Now write the actual planes.
                        for (int i = 0; i<3; i++){
                            buffer = mImage.getPlanes()[i].getBuffer();
                            bytes = new byte[buffer.remaining()]; // makes byte array large enough to hold image
                            buffer.get(bytes); // copies image from buffer to byte array
                            output.write(bytes);    // write the byte array to file
                        }
                        success = true;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mImage.close(); // close this to free up buffer for other images
                        if (null != output) {
                            try {
                                output.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;

                case ImageFormat.JPEG:
                    ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
                    bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);

                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(file);
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
                    break;
            }
        }
    };

    private TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    setupCamera(width, height);
                    openCamera();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
            };

    private CameraDevice.StateCallback mCameraDeviceStateCallback
            = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
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

    //do nothing special here, for now
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result){
            switch (mState) {
                case PREVIEW:
                    //do nothing, no act
                    break;
                case WAIT_LOCK:
                    unLockFocus();
                    captureStillImage();
                    break;
                case CAPTURE:
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
        }
    };

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                Size imageSize = Collections.min(
                        Arrays.asList(map.getOutputSizes(IMAGE_FORMAT)),
                        new Comparator<Size>() {
                            @Override
                            public int compare(Size o1, Size o2) {
                                return Long.signum(o1.getWidth() * o1.getHeight() - o2.getHeight() * o2.getWidth());
                            }
                        }
                );
                mImageReader = ImageReader.newInstance(imageSize.getWidth(),
                        imageSize.getHeight(),
                        IMAGE_FORMAT,
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara_intent);

        this.eventID = getIntent().getStringExtra(CURRENT_EVENT_ID);
        this.eventName = getIntent().getStringExtra(CURRENT_EVENT_NAME);
        this.numParticipants = getIntent().getIntExtra(NUM_PARTICIPANTS, 0);
        createImageGallery();
        unLockFocus();

        mTitle = (TextView) findViewById(R.id.title);
        mParticipantCount = (TextView) findViewById(R.id.participant_count);
        mRecyclerView = (RecyclerView) findViewById(R.id.galleryRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        mTitle.setText(eventName);
        mParticipantCount.setText(Integer.toString(this.numParticipants) + " scopers");

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

    public void sendTakePhotoBroadcast(View view) {
        new SynchronizeCapture("test").execute();
    }

    public void takePhoto(String event) {
        if (event.equals(eventID)) {
            mImageHash = generateImageHash();
            createImageGallery();
            lockFocus();
        }
    }

    void createImageGallery() {
        //TODO Need to ask for file permissions for new versions of android on runtime.
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mGalleryFolder = new File(storageDirectory, GALLERY_LOCATION);
        System.out.println(mGalleryFolder);

        if(!mGalleryFolder.exists()) {
            mGalleryFolder.mkdirs();
        }
    }

    private String generateImageHash() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        return Integer.toString(Math.abs(imageFileName.hashCode()));
    }

    static File createImageFile(String imageHash, int sequenceNum) throws IOException {
        File image = new File(mGalleryFolder, imageHash + "_" + sequenceNum);
        mImageFileLocation = image.getAbsolutePath();

        return image;
    }

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                }
                else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
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

    private void createCameraPreviewSession(){
        try {
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewSurface = new Surface(surfaceTexture);

            //capture request
            mPreviewCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewCaptureRequestBuilder.addTarget(mPreviewSurface);

            //creating session
            mCameraDevice.createCaptureSession(Arrays.asList(mPreviewSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            if (mCameraDevice == null) {
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

    private void openBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera2 background thread");
        mBackgroundThread.start();
        mBackgroundHandler = new android.os.Handler(mBackgroundThread.getLooper());
    }

    private void closeBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void lockFocus() {
        mState = CAPTURE_STATE.WAIT_LOCK;
        mPictureCount = 0;
    }

    private void unLockFocus() {
        mState = CAPTURE_STATE.PREVIEW;
    }

    private void setCaptureState() {
        mState = CAPTURE_STATE.CAPTURE;
    }

    public static Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    public static void setBitmapToMemoryCache(String key, Bitmap bitmap) {
        if(getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public CaptureRequest.Builder getFastRequestBuilder() {
        try {
            CaptureRequest.Builder captureStillBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureStillBuilder.addTarget(mImageReader.getSurface());
            captureStillBuilder.set(CaptureRequest.EDGE_MODE,
                    CaptureRequest.EDGE_MODE_OFF);
            captureStillBuilder.set(
                    CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                    CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
            captureStillBuilder.set(
                    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF);
            captureStillBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE,
                    CaptureRequest.NOISE_REDUCTION_MODE_OFF);
            captureStillBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);

            captureStillBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
            captureStillBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, true);
            return captureStillBuilder;
        } catch (Exception e) {
            return null;
        }
    }

    private void captureStillImage() {
        try {
            CaptureRequest.Builder captureStillBuilder = getFastRequestBuilder();
            captureStillBuilder.addTarget(mPreviewSurface);
            CameraCaptureSession.CaptureCallback captureCallback =
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                            super.onCaptureCompleted(session, request, result);
                            System.out.println("FINISHED TAKING NEW PICTURE");
                            mPictureCount++;
                            if (mPictureCount >= PlaybackActivity.NUM_IMAGES_PER_SEQUENCE - 1) {
                                startPlayback();
                            }
                        }
                    };

            ArrayList<CaptureRequest> requestList = new ArrayList<CaptureRequest>();
            for (int i = 0; i < PlaybackActivity.NUM_IMAGES_PER_SEQUENCE; i++) {
                requestList.add(captureStillBuilder.build());
            }

            mCameraCaptureSession.captureBurst(requestList, captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPause(){
        closeCamera();
        closeBackgroundThread();
        if (mBroadcastReceiver != null) unregisterReceiver(mBroadcastReceiver);

        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBroadcastReceiver == null) mBroadcastReceiver = new ImageCaptureReceiver();
        IntentFilter intentFilter = new IntentFilter(StaticResources.BEGIN_IMAGE_CAPTURE);
        registerReceiver(mBroadcastReceiver, intentFilter);
        openBackgroundThread();

        if(mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    public void startPlayback() {
        Intent intent = new Intent(this, PlaybackActivity.class);
        int index = mImageFileLocation.lastIndexOf("/");
        intent.putExtra(PlaybackActivity.FILE_PATH_EXTRA, mImageFileLocation.substring(0, index));
        intent.putExtra(PlaybackActivity.FILE_NAME_BASE, mImageHash);
        new VideoUploader(this.eventID, numParticipants).execute(mImageFileLocation.substring(0, index) + '/' + mImageHash + "_0");
        startActivity(intent);
    }
}
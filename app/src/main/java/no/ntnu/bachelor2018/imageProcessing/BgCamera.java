package no.ntnu.bachelor2018.imageProcessing;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The BgCamera class is used to take a picture in the background
 * when a frame has been found.
 */
public class BgCamera {

    private final String TAG = this.getClass().getSimpleName();

    Context context;
    CameraManager cameraManager;
    CameraDevice cam;
    CaptureRequest request;
    String backCamID;
    Size cSize;
    List<Surface> surfaces;
    ImageReader img;


    CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "Opened camera");
            cam = camera;

            try {
                img = ImageReader.newInstance(cSize.getWidth(), cSize.getHeight(), ImageFormat.RAW_SENSOR, 1);
                img.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Log.d(TAG, "New picture available");
                        Image image = reader.acquireLatestImage();
                        saveImage(image);
                    }
                }, null);

                Surface surface = img.getSurface();
                CaptureRequest.Builder requestBuilder = cam.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                requestBuilder.addTarget(surface);
                request = requestBuilder.build();
                surfaces = new ArrayList<>();
                surfaces.add(surface);

                cam.createCaptureSession(surfaces, cameraCaptureSessionStateCallback, null);
            } catch (CameraAccessException e) {
                // TODO better exception handling
                Log.e(TAG, e.getLocalizedMessage());
            }

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "Camera disconnected");
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "Could not open camera, error: " + String.valueOf(error));
            camera.close();
        }
    };

    CameraCaptureSession.StateCallback cameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            try {
                session.capture(request, cameraCaptureSessionCaptureCallback, null);
            } catch (CameraAccessException e){
                Log.e(TAG, e.getLocalizedMessage());
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "Configuration failed");
            cam.close();
        }

        @Override
        public void onActive(@NonNull CameraCaptureSession session){
            Log.d(TAG, "Starting to process capture requests");
        }
    };

    CameraCaptureSession.CaptureCallback cameraCaptureSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.d(TAG, "Capture completed");
            cam.close();
        }
    };


    // TODO move into functions
    public BgCamera(Context context){
        // Create a cameramanager object, this handles all the cameras on the device
        this.context = context;
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        try {
            // Get list of all camera IDs
            backCamID = null;
            String[] strings = cameraManager.getCameraIdList();

            // Go through all the IDs and find the back camera
            for(String id : strings){
                CameraCharacteristics chars = cameraManager.getCameraCharacteristics(id);
                if(chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK){
                    Log.d(TAG, "Found back camera, ID: " + chars.toString());
                    backCamID = id;
                    break;
                }
            }

            // If we could not find the back camera
            if(backCamID == null){
                Log.e(TAG, "Could not find back camera");
            } else {
                Log.d(TAG, "Camera ID: " + backCamID);
            }

        } catch(CameraAccessException e) {
            Log.e(TAG, "Camera access denied");
        }
    }

    public void takePicture(){
        try {
            // Set the camera size
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(backCamID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            cSize = map.getOutputSizes(ImageFormat.RAW_SENSOR)[0];

            cameraManager.openCamera(backCamID, cameraDeviceStateCallback, null);
        } catch(SecurityException e){
            // TODO improve camera permission handling
            Log.e(TAG, "Camera permission denied");
        } catch(CameraAccessException e){
            // TODO improve error handling
            Log.e(TAG, "Camera access denied");
        }
    }

    private boolean saveImage(Image image){
        ContextWrapper cw = new ContextWrapper(context);
        File dir = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File myPath = new File(dir, "im.jpg");

        Image.Plane[] planes = image.getPlanes();
        Log.d(TAG, String.valueOf(planes.length));
        ByteBuffer buffer = planes[0].getBuffer();

        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * image.getWidth();

        Bitmap bitmap = Bitmap.createBitmap(image.getWidth() + rowPadding / pixelStride, image.getHeight(), Bitmap.Config.RGB_565);
        bitmap.copyPixelsFromBuffer(buffer);

        image.close();

        try{
            FileOutputStream fos = new FileOutputStream(myPath);
            Log.d(TAG, "Compressing image...");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Log.d(TAG, "Done");
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e.getLocalizedMessage());
        } catch (IOException e){
            Log.e(TAG, "IOException: " + e.getLocalizedMessage());
        }

        return false;
    }

}

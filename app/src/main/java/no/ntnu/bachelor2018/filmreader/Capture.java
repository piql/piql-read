package no.ntnu.bachelor2018.filmreader;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
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
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

/**
 * The Capture class is used to take a picture in the background when a frame has been found.
 * It will attempt to take a high quality picture for reading and scanning data.
 */
public class Capture {

    // Tag for logging
    private final String TAG = this.getClass().getSimpleName();
    private Activity        activity;        // The context for the activity of creation
    private CameraManager   cameraManager;  // Camera manager to get information about all cameras
    private CameraDevice    cam;            // Object for one camera
    private CaptureRequest  request;        // A request object for a camera device
    private String          backCamID;      // The ID for the back camera
    private Size            cSize;          // The image resolution of the picture to be taken
    private List<Surface>   surfaces;       // The output surface to put the image
    private ImageReader     img;            // Object for reading images
    private ImageView       preview;
    private Mat             mat = null;
    private Bitmap          bitmap = null;

    // A callback object for receiving updates about the state of a camera device.
    CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "Opened camera");


            // Save the camera object for further use
            cam = camera;
            preview = activity.findViewById(R.id.imageView);


            try {
                // Create an ImageReader object where we can properly read images
                img = ImageReader.newInstance(cSize.getWidth(), cSize.getHeight(), ImageFormat.YUV_420_888, 2);

                // Whenever a new image is available
                img.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {

                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Log.d(TAG, "New picture available");
                        onNewImageCapture(reader);
                    }

                }, null);

                // We get a surface from the image which is the output
                Surface surface = img.getSurface();
                //Surface textureSurface = new Surface(texture);

                // Build a capture request for the camera
                CaptureRequest.Builder requestBuilder = cam.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                requestBuilder.addTarget(surface);

                //requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
                request = requestBuilder.build();

                // Create a list of the surfaces
                surfaces = new ArrayList<>();
                surfaces.add(surface);
                //surfaces.add(textureSurface);

                // Send the surface to another callback
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

    // A callback object for receiving updates about the state of a camera capture session.
    CameraCaptureSession.StateCallback cameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            try {
                // If the camera configured successfully we do a capture
                session.setRepeatingRequest(request, cameraCaptureSessionCaptureCallback, null);
            } catch (CameraAccessException e){
                Log.e(TAG, e.getLocalizedMessage());
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "Configuration failed");
            //cam.close();
        }

        @Override
        public void onActive(@NonNull CameraCaptureSession session){
            Log.d(TAG, "Starting to process capture requests");
        }

    };

    // A callback object for tracking the progress of a CaptureRequest submitted to
    // the camera device.
    CameraCaptureSession.CaptureCallback cameraCaptureSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.d(TAG, "Capture completed");

            //cam.close();
        }

    };


    // Constructor for the object, gets the camera ID for the backcam.
    public Capture(Activity activity){
        // Store away the context
        this.activity = activity;

        // Create a cameramanager object, this handles all the cameras on the device
        cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        try {
            // Get list of all camera IDs
            backCamID = null;
            String[] strings = cameraManager.getCameraIdList();

            // Go through all the IDs and store away the ID of the back camera
            for(String id : strings){
                CameraCharacteristics chars = cameraManager.getCameraCharacteristics(id);

                // TODO (Christian) devices with two back cameras
                if(chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK){
                    Log.d(TAG, "Found back camera, ID: " + chars.toString());
                    backCamID = id;
                    break;
                }
            }

            // If we could not find the back camera
            if(backCamID == null){
                // TODO (Christian) have to create a new object to reattempt, needs fix
                Log.e(TAG, "Could not find back camera");
            }

        } catch(CameraAccessException e) {
            // TODO (Christian) exit application and ask again next time
            Log.e(TAG, "Camera access denied");
        }
    }

    /**
     * Take a picture with the back camera that has been found.
     */
    public void takePicture(){
        try {
            // Set the camera size and settings
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(backCamID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            for(int i : map.getOutputFormats()){
                Log.d(TAG, String.valueOf(i));
            }

            cSize = map.getOutputSizes(ImageFormat.YUV_420_888)[0];

            // This operation is asynchronous and continues in the callback
            cameraManager.openCamera(backCamID, cameraDeviceStateCallback, null);
        } catch(SecurityException e){
            // TODO (Christian) improve camera permission handling
            Log.e(TAG, "Camera permission denied");
        } catch(CameraAccessException e){
            // TODO (Christian) improve error handling
            Log.e(TAG, "Camera access denied");
        }
    }

    /**
     * This function is called when an image is captured, transforms an image
     * to a {@link Mat}
     *
     * @param reader - Imagereader object containing the result(s)
     */
    public void onNewImageCapture(ImageReader reader){
        Image image = reader.acquireLatestImage();


        if(image != null){
            // Create a mat out of the image
            Bitmap bitmap = imageToBitmap(image);
            preview.setImageBitmap(bitmap);
            //hiresCapture = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC3);
            //Utils.bitmapToMat(bitmap, hiresCapture);
            image.close();
        }

    }

    /**
     * Takes an image object and converts it to a bitmap. Currently only works for images
     * with one plane (JPEG)
     *
     * @param image - The image object as input
     * @return A bitmap object of the image, null if the image has more than one plane
     */
    private Bitmap imageToBitmap(Image image){
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();

        mat = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1,buffer);
        if(bitmap == null) {
            bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        }
        Utils.matToBitmap(mat,bitmap);

        return bitmap;
    }

}

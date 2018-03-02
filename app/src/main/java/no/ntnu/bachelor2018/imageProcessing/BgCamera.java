package no.ntnu.bachelor2018.imageProcessing;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
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

import org.opencv.android.CameraBridgeViewBase;
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

import no.ntnu.bachelor2018.filmreader.MainActivity;
import no.ntnu.bachelor2018.filmreader.ViewImage;

/**
 * The BgCamera class is used to take a picture in the background when a frame has been found.
 * It will attempt to take a high quality picture for reading and scanning data.
 */
public class BgCamera {

    // Tag for logging
    private final String TAG = this.getClass().getSimpleName();

    private Context         context;        // The context for the activity of creation
    private CameraManager   cameraManager;  // Camera manager to get information about all cameras
    private CameraDevice    cam;            // Object for one camera
    private CaptureRequest  request;        // A request object for a camera device
    private String          backCamID;      // The ID for the back camera
    private Size            cSize;          // The image resolution of the picture to be taken

    public static Mat getHiresCapture() {
        return hiresCapture;
    }

    public static boolean isImageReady() {
        return imageReady;
    }

    private List<Surface>   surfaces;       // The output surface to put the image
    private ImageReader     img;            // Object for reading images

    private static Mat      hiresCapture;           // Captured image
    private static boolean  imageReady = false;     // Image ready check

    // A callback object for receiving updates about the state of a camera device.
    CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "Opened camera");

            // Save the camera object for further use
            cam = camera;

            try {
                // Create an ImageReader object where we can properly read images
                img = ImageReader.newInstance(cSize.getWidth(), cSize.getHeight(), ImageFormat.JPEG, 2);

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

                // Build a capture request for the camera
                CaptureRequest.Builder requestBuilder = cam.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                requestBuilder.addTarget(surface);

                //requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
                request = requestBuilder.build();

                // Create a list of the surfaces
                surfaces = new ArrayList<>();
                surfaces.add(surface);

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
                session.capture(request, cameraCaptureSessionCaptureCallback, null);
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
    public BgCamera(Context context){
        // Store away the context
        this.context = context;
        imageReady = false;

        // Create a cameramanager object, this handles all the cameras on the device
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

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

            cSize = map.getOutputSizes(ImageFormat.JPEG)[0];

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
            hiresCapture = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC3);
            Utils.bitmapToMat(bitmap, hiresCapture);

            // Start the viewimage activity
            imageReady = true;




        }

    }

    /**
     * Takes an image object and converts it to a bitmap. Currently only works for images
     * with one plane (JPEG)
     *
     * @param image - The image object as input
     * @return A bitmap object of the image, null if the image has more than one plane
     */
    public static Bitmap imageToBitmap(Image image){
        Image.Plane[] planes = image.getPlanes();

        if(planes.length > 1){
            return null;
        }

        ByteBuffer buffer = planes[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

        return bitmap;
    }

    /**
     * Saves an Image object to local files for the application. Since the application will
     * not store any images taken this function is deprecated.
     *
     * @param image - The {@link Image} object to save to file
     * @return True on success, False otherwise
     */
    @Deprecated
    private boolean saveImage(Image image){
        // Create an object with the path where the file should be saved, currently static
        ContextWrapper cw = new ContextWrapper(context);
        File dir = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File myPath = new File(dir, "im.jpg");

        // Create a ByteBuffer out of the image
        Image.Plane[] planes = image.getPlanes();

        Log.d(TAG, "Number of planes: " + String.valueOf(planes.length));
        Log.d(TAG, "Format ID: " + String.valueOf(image.getFormat()));

        // Decode/uncompress the jpg and get a bitmap
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        buffer.rewind();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

        // Close the image as we are done with it
        //image.close();

        try{
            // Save open a stream
            FileOutputStream fos = new FileOutputStream(myPath);
            Log.d(TAG, "Compressing image...");

            // Compress and save the file
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Log.d(TAG, "Done");

            // close the stream
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

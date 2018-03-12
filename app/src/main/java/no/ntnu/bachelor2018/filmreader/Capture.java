package no.ntnu.bachelor2018.filmreader;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
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
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

/**
 * The capture class controls the camera used for preview, it configures the camera on the
 * device and repeatedly captures frames to show using an imageView.
 */
public class Capture {

    // Tag for this class
    private final String TAG = getClass().getSimpleName();

    private Activity                activity;       // The context for the activity of creation
    private CameraManager           cameraManager;  // Camera manager to get information about all cameras
    private CameraDevice            cam;            // Object for one camera
    private CaptureRequest          request;        // A request object for a camera device
    private CameraCaptureSession    cSession;       // Globally storing the session to properly close it
    private String                  backCamID;      // The ID for the back camera
    private Size                    cSize;          // The image resolution of the picture to be taken
    private List<Surface>           surfaces;       // The output surface to put the image
    private ImageReader             img;            // Object for reading images
    private ImageView               preview;        // View for the preview images
    private Bitmap                  bitmap = null;  // bitmap for the image to process
    private Reader                  reader;         // Reader object for processing an image
    private Thread                  t1;             // Thread for updating the preview image

    // The imageformat to use on captures, changing this will most likely break something else.
    private int format = ImageFormat.YUV_420_888;

    // A callback object for receiving updates about the state of a camera device.
    CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "Opened camera");

            // Save the camera object for further use
            cam = camera;

            try {
                // Create an ImageReader object where we can properly read images
                img = ImageReader.newInstance(cSize.getWidth(), cSize.getHeight(), format, 2);

                // Whenever a new image is available
                img.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {

                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        onNewImageCapture(reader);
                    }

                }, null);

                // We get a surface from the image which is the output
                Surface surface = img.getSurface();
                //Surface textureSurface = new Surface(texture);

                // Build a capture request for the camera
                CaptureRequest.Builder requestBuilder = cam.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
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
                cSession = session;
                session.setRepeatingRequest(request, cameraCaptureSessionCaptureCallback, null);
            } catch (CameraAccessException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "Configuration failed");
            //cam.close();
        }

        @Override
        public void onActive(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "Starting to process capture requests");
        }

    };

    // A callback object for tracking the progress of a CaptureRequest submitted to
    // the camera device.
    CameraCaptureSession.CaptureCallback cameraCaptureSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

    };

    public class processingWorker implements Runnable {
        private ImageReader imReader;
        public processingWorker(ImageReader imReader) {
            this.imReader = imReader;
        }

        public void run() {
            final Image image = imReader.acquireLatestImage();
            if (image != null) {
                // Create a mat out of the image
                bitmap  = imageToBitmap(image);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        preview.setImageBitmap(bitmap);
                    }
                });

                image.close();
            }
        }
    }


    // Constructor for the object, gets the camera ID for the backcam.
    public Capture(Activity activity) {
        // Store away the context
        this.activity = activity;

        // Create a reader object for image processing
        reader = new Reader();

        // Get the imageView to get a preview of the captures
        preview = activity.findViewById(R.id.imageView);

        // Get the cameramanager object, this handles all the cameras on the device
        cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        try {
            // Get list of all camera IDs
            backCamID = null;
            String[] strings = cameraManager.getCameraIdList();

            // Go through all the IDs and store away the ID of the back camera
            for (String id : strings) {
                CameraCharacteristics chars = cameraManager.getCameraCharacteristics(id);

                // TODO devices with two back cameras
                if (chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    Log.d(TAG, "Found back camera, ID: " + chars.toString());
                    backCamID = id;
                    break;
                }
            }

            // If we could not find the back camera
            if (backCamID == null) {
                // TODO (Christian) have to create a new object to reattempt, needs fix
                Log.e(TAG, "Could not find back camera");
            }

        } catch (CameraAccessException e) {
            // TODO (Christian) exit application and ask again next time
            Log.e(TAG, "Camera access denied");
        }
    }

    /**
     * Take a picture with the back camera that has been found.
     */
    public void takePicture() {
        try {
            // Set the camera size and settings
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(backCamID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // Print out all the sizes
            for(Size size : map.getOutputSizes(format)){
                Log.d(TAG, String.valueOf(size.getWidth()) + "x" + String.valueOf(size.getHeight()));
            }

            // Choose the size of the capture, 0 is the largest resolution, differs from device
            cSize = map.getOutputSizes(format)[0];

            // This operation is asynchronous and continues in the callback
            cameraManager.openCamera(backCamID, cameraDeviceStateCallback, null);
        } catch (SecurityException e) {
            // TODO (Christian) improve camera permission handling
            Log.e(TAG, "Camera permission denied");
        } catch (CameraAccessException e) {
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
    public void onNewImageCapture(ImageReader reader) {
        if(t1 == null || !t1.isAlive()){
            t1 = new Thread(new processingWorker(reader));
            t1.start();
        }
    }

    /**
     * Takes an image object and converts it to a bitmap. Only takes the first plane
     *
     * @param image - The image object as input
     * @return A bitmap object of the image
     */
    private Bitmap imageToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();

        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1, buffer);

        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        }

        mat = reader.processFrame(mat);
        Utils.matToBitmap(mat, bitmap);

        return bitmap;
    }

    /**
     * Called whenever the camera should pause and stop capturing
     */
    public void pauseCamera(){
        closeCamera();
    }

    /**
     * Used to resume the camera after a pause
     */
    public void resumeCamera() {
        startCamera();
    }

    /**
     * Stops the camera captures
     */
    public void closeCamera(){
        Log.d(TAG, "closing camera");

        if(cam != null) {
            cam.close();
            cam = null;
        }

        if(img != null) {
            img.close();
            img = null;
        }
    }

    /**
     * Starts the camera capturing
     */
    public void startCamera(){
        takePicture();
    }


}



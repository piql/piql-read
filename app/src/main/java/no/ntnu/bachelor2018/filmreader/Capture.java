package no.ntnu.bachelor2018.filmreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
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
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

/**
 * The capture class controls the camera used for preview, it configures the camera on the
 * device and repeatedly captures frames to show using an imageView.
 */
public class Capture {

    // Tag for this class
    private static final String TAG = "Capture";
    private static final int THREADS = 2;
    private static List<ThreadWrapper> workers;    // List containing image processing workers
    // A callback object for tracking the progress of a CaptureRequest submitted to
    // the camera device.
    CameraCaptureSession.CaptureCallback cameraCaptureSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

    };
    private Activity activity;            // The context for the activity of creation
    private CameraManager cameraManager;    // Camera manager to get information about all cameras
    private CameraDevice cam;                // Object for one camera
    private CaptureRequest request;            // A request object for a camera device
    private CameraCaptureSession cSession;  // Globally storing the session to properly close it
    private SharedPreferences prefs;        // Get the preferences stored
    private String backCamID;                // The ID for the back camera
    private Size cSize;                    // The image resolution of the picture to be taken
    private List<Surface> surfaces;        // The output surface to put the image
    private ImageReader img;                // Object for reading images
    private TextView textView;
    private ProgressBar progressBar;

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
                stopCamera();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "Configuration failed");
            errorDialog();
        }

        @Override
        public void onActive(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "Starting to process capture requests");
        }

    };
    private ImageView preview;                // View for the preview images
    // The imageformat to use on captures, changing this will most likely break something else.
    private int format = ImageFormat.YUV_420_888;
    // A callback object for receiving updates about the state of a camera device.
    CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "Opened camera");

            // Save the camera object for further use
            cam = camera;

            textView = activity.findViewById(R.id.progressTextView);
            progressBar = activity.findViewById(R.id.progressBar);

            preview = activity.findViewById(R.id.imageView);
            try {
                // Create an ImageReader object where we can properly read images
                img = ImageReader.newInstance(cSize.getWidth(), cSize.getHeight(), format, THREADS + 1);

                // Whenever a new image is available
                img.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {

                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        onNewImageCapture(reader);
                    }

                }, null);

                // We get a surface from the image which is the output
                Surface surface = img.getSurface();

                // Build a capture request for the camera
                CaptureRequest.Builder requestBuilder = cam.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                requestBuilder.addTarget(surface);
                request = requestBuilder.build();

                // Create a list of the surfaces
                surfaces = new ArrayList<>();
                surfaces.add(surface);
                //surfaces.add(textureSurface);

                // Send the surface to another callback
                cam.createCaptureSession(surfaces, cameraCaptureSessionStateCallback, null);
            } catch (CameraAccessException e) {
                // Automatically goes to onDisconnected()
                Log.e(TAG, "onOpened error");
            }

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "Camera disconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "Could not open camera, error: " + String.valueOf(error));
            camera.close();

            // Print out the error, only for debugging
            switch (error) {
                case ERROR_CAMERA_DEVICE: // This is also when onDisconnect was run
                    Log.e(TAG, "ERROR_CAMERA_DEVICE");
                    break;
                case ERROR_CAMERA_DISABLED:
                    Log.e(TAG, "ERROR_CAMERA_DISABLED");
                    break;
                case ERROR_CAMERA_IN_USE:
                    Log.e(TAG, "ERROR_CAMERA_IN_USE");
                    break;
                case ERROR_CAMERA_SERVICE:
                    Log.e(TAG, "ERROR_CAMERA_SERVICE");
                    break;
                case ERROR_MAX_CAMERAS_IN_USE:
                    Log.e(TAG, "ERROR_MAX_CAMERAS_IN_USE");
                    break;
                default:
                    Log.e(TAG, "UNKNOWN ERROR");
                    break;
            }

            //errorDialog();
        }
    };

    // Constructor for the object, gets the camera ID for the backcam.
    public Capture(Activity activity) {
        // Store away the context
        this.activity = activity;

        // Get the preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);

        workers = new LinkedList<>();

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

                // Check if it is a camera facing back
                if (chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    Log.d(TAG, "Found back camera, ID: " + chars.toString());
                    backCamID = id;
                    break;
                }
            }

            // If we could not find the back camera
            if (backCamID == null) {
                Log.e(TAG, "Could not find back camera");
                errorDialog("No back camera found", "The app could not find a back camera on this device");
            }

            // Set the camera size and settings
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(backCamID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // Get the index chosen in preferences, default 0
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
            int index = Integer.valueOf(prefs.getString("resolution", "0"));

            // Save the sizes in preferences and set the resolution
            Size[] sizes = map.getOutputSizes(format);
            Preferences.SettingsFragment.addSizes(sizes);
            cSize = sizes[index];
        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access denied");
            errorDialog(activity.getResources().getString(R.string.camera_access_denied),
                    activity.getResources().getString(R.string.camera_access_denied_desc));
        }
    }

    /**
     * Takes an image object and converts it to a bitmap. Only takes the first plane
     *
     * @param image     - The image object as input
     * @param byteArray
     * @param bitmap
     * @return A bitmap object of the image
     */
    private static Bitmap processFrame(Image image, Reader reader, byte[] byteArray, Bitmap bitmap, int width, int height) {
        //Get buffer of captured image
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();

        //Initialize image variables once with correct sizes.

        Mat procImage = new Mat(height,width,CvType.CV_8UC1);

		if(byteArray.length != height*width){
		    byteArray = new byte[height*width];
        }
        buffer.get(byteArray);
        procImage.put(0, 0, byteArray);

        // This will process the image
        procImage = reader.processFrame(procImage);
        //procImage = rotateMat(procImage);
        //Log.d(TAG, String.valueOf(procImage.cols()) + " " + String.valueOf(procImage.rows()));


        //Resize if necessary(if processed frame is cropped)
        if (procImage.width() != bitmap.getWidth() || procImage.height() != bitmap.getHeight()) {
            bitmap.recycle();
            bitmap = Bitmap.createBitmap(procImage.width(), procImage.height(), Bitmap.Config.ARGB_8888);
        }

        //Convert processed image to bitmap that can be shown on screen
        procImage.get(0, 0, byteArray);
        Utils.matToBitmap(procImage, bitmap);
        procImage.release();
        procImage = null;
        return bitmap;
    }

    /**
     * Take a picture with the back camera that has been found.
     */
    public void startCamera() {
        try {
            // This operation is asynchronous and continues in the callback
            cameraManager.openCamera(backCamID, cameraDeviceStateCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access denied");
            errorDialog(activity.getResources().getString(R.string.camera_access_denied),
                    activity.getResources().getString(R.string.camera_access_denied_desc));
        } catch (SecurityException e) {
            Log.e(TAG, "Camera permission denied");
            // Dont do anything as this should not happen
        }
    }

    /**
     * Stops the camera captures
     */
    public void stopCamera() {
        Log.d(TAG, "closing camera");

        if (cam != null) {
            cam.close();
        }

        if (img != null) {
            img.close();
        }

        cam = null;
        img = null;
    }

    /**
     * This function is called when an image is captured, transforms an image
     * to a {@link Mat}
     *
     * @param reader - Imagereader object containing the result(s)
     */
    public void onNewImageCapture(ImageReader reader) {
        boolean started = false;
        for (int i = 0; i < workers.size() && !started; i++) {
            if (workers.get(i).startThread(reader)) {
                started = true;
            }
        }
        if (!started && workers.size() < THREADS) {
            /** Try catch */
            try {
                ThreadWrapper worker = new ThreadWrapper(reader);
                workers.add(worker);
                worker.startThread(reader);
            } catch (Exception e) {
                Log.e(TAG, "Failed to create new thread");
                e.printStackTrace();
            }
        }

    }

    /**
     * Creates an error dialog with custom title and message. Closes
     * the application on button press
     *
     * @param title   The title of the dialog
     * @param message The message of the dialog
     */
    private void errorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int i) {
                // When the user dismisses the dialog we close the application
                activity.finishAndRemoveTask();
            }
        });
        builder.create().show();
    }

    /**
     * Creates an error dialog with generic "error has occured" message
     */
    private void errorDialog() {
        errorDialog(activity.getString(R.string.error_title), activity.getString(R.string.error_message));
    }

    private Mat rotateMat(Mat input) {
        Mat output = new Mat();
        Mat rotationMat = Imgproc.getRotationMatrix2D(new Point(input.width() / 2, input.height() / 2), 0, 1);
        Imgproc.warpAffine(input, output, rotationMat, input.size());
        return output;
    }

    /**
     * Gets the size
     *
     * @return A {@link Size} object
     */
    public Size getSize() {
        return cSize;
    }
    public static synchronized void stopWorkers(){
        if(workers != null){
            workers.clear();
        }
    }

    /**
     * Contains a set of variables that can be used and reused with a single thread at the time.
     */
    public class ThreadWrapper {
        byte[] byteArray;
        RunnableWorker p1;
        private ImageReader imReader;
        private Bitmap bitmap;
        private Reader reader;
        private Thread thread;
        private int width, height;

        public ThreadWrapper(ImageReader imReader) {
            this.imReader = imReader;
            init();
        }

        private void init() {
            width = imReader.getWidth();
            height = imReader.getHeight();
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            byteArray = new byte[width * height];
            reader = new Reader();
            p1 = new RunnableWorker(imReader, byteArray, reader, bitmap);
        }

        public boolean startThread(ImageReader newReader) {
            if(imReader.getHeight() != newReader.getHeight()
                    || imReader.getWidth() != newReader.getWidth()){
                imReader = newReader;
                init();
            }
            if (thread == null || !thread.isAlive()) {
                thread = new Thread(p1, "Imgproc Thread");
                thread.start();
                return true;
            }
            return false;
        }
    }
    /**
     * Class for updating the view on another thread while the main thread can do image processing
     */
    public class RunnableWorker implements Runnable {
        byte[] byteArray;
        private ImageReader imReader;
        private Reader reader;
        private Bitmap bitmap;

        public RunnableWorker(ImageReader imReader, byte[] byteArray, Reader reader, Bitmap bitmap) {
            this.imReader = imReader;
            this.byteArray = byteArray;
            this.reader = reader;
            this.bitmap = bitmap;
        }

        public void run() {
            Image image = null;
            synchronized (imReader) {
                image = imReader.acquireLatestImage();
            }
            if (image != null && image.getHeight() > 0) {
                int width = image.getWidth();
                int height = image.getHeight();
                Log.d(TAG, "Picture size: " + width + "x" + height);
                bitmap = processFrame(image, reader, byteArray, bitmap, width, height);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (preview == null) {
                            Log.e(TAG, "preview is null");
                        } else if (bitmap == null) {
                            Log.e(TAG, "bitmap is null");
                        } else {
                            preview.setImageBitmap(bitmap);
                        }
                    }
                });

                if(image != null){
                    image.close();
                }
            }
        }
    }
}
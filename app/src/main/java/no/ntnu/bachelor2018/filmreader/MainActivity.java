package no.ntnu.bachelor2018.filmreader;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

/**
 * Main view
 */
public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";

    SharedPreferences prefs;
    JavaCameraView cameraView;
    Mat mRgba;
    private boolean processingFrame;
    Reader reader;
    MatOfPoint2f corners;
    BaseLoaderCallback loaderCB = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    cameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV loaded");
        } else {
            Log.d(TAG, "Could not load OpenCV");
        }
    }

    private void getCameraPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},1);
            }
        }
    }

    /**
     * Where the application is opened
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getCameraPermissions();
        setContentView(R.layout.activity_main);

        // Force portrait layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        corners = new MatOfPoint2f();

        cameraView = findViewById(R.id.camera_view);

        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCvCameraViewListener(this);


    }

    /**
     * When the application is paused, e.g. when the user minimizes this and
     * switches to another application
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (cameraView != null) {
            cameraView.disableView();
        }
    }

    /**
     * When the application is closed, NOT the same as pause
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraView != null) {
            cameraView.disableView();
        }
    }

    /**
     * When the user switches back this application after a pause
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV loaded");
            loaderCB.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "Could not load OpenCV");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, loaderCB);
        }
    }

    /**
     * Starts the information activity when the button is pressed
     *
     * @param view
     */
    public void infoButton(View view){
        Intent intent = new Intent(this, Information.class);
        startActivity(intent);
    }

    /**
     * Starts the preferences activity when the button is pressed
     *
     * @param view
     */
    public void preferencesButton(View view){
        Intent intent = new Intent(this, Preferences.class);
        startActivity(intent);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        reader = new Reader(width, height, this);
    }


    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    /**
     * Main loop of camera image access image output.
     * Method is called for each camera frame.
     *
     * @param inputFrame input frame from camera
     * @return image to draw on camera
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // Do NOT add new variables here

        return reader.processFrame(inputFrame.rgba());
    }

}

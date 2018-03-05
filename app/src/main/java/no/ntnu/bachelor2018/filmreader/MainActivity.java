package no.ntnu.bachelor2018.filmreader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

/**
 * Main view
 */
public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";
    private Capture capture;

    BaseLoaderCallback loaderCB = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
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
        capture = new Capture(this);
        capture.takePicture();
    }

    /**
     * When the application is paused, e.g. when the user minimizes this and
     * switches to another application
     */
    @Override
    protected void onPause() {
        //capture.pauseCamera();
        super.onPause();
    }

    /**
     * When the application is closed, NOT the same as pause
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * When the user switches back this application after a pause
     */
    @Override
    protected void onResume() {
        super.onResume();
        //capture.resumeCamera();

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

}

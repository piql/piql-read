package no.ntnu.bachelor2018.filmreader;

import android.Manifest;
import android.content.Context;
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

import java.io.File;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;
import no.ntnu.bachelor2018.previewImageProcessing.Calibration;

/**
 * Main view
 */
public class MainActivity extends AppCompatActivity{

    private final String TAG = getClass().getSimpleName();

    public static Context context;  // Context for other classes MainActivity uses
    private Capture capture;        // Capture class for capturing images

	// Callback for when OpenCV is loaded
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
            Log.d("MainActivity_init", "OpenCV loaded");
        } else {
            Log.d("MainActivity_init", "Could not load OpenCV");
        }
    }

	/**
	 * Gets the permissions required for the application (the popup dialogue on app start)
	 */
	private void getPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},1);
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
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

        Log.e(TAG, "RAN ONCREATE");
        context = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getPermissions();
        setContentView(R.layout.activity_main);

        // Force portrait layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        /*
	    if (OpenCVLoader.initDebug()) {
		    Log.d(TAG, "OpenCV loaded");
		    loaderCB.onManagerConnected(LoaderCallbackInterface.SUCCESS);
	    } else {
		    Log.d(TAG, "Could not load OpenCV");
		    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, loaderCB);
	    }
	    */
    }

    /**
     * When the application is paused, e.g. when the user minimizes this and
     * switches to another application
     */
    @Override
    protected void onStop() {
    	Log.e(TAG, "RAN ONSTOP");
        capture.stopCamera();
        super.onStop();
    }

    /**
     * When the application is closed
     */
    @Override
    protected void onDestroy() {
    	Log.e(TAG, "RAN ONDESTROY");
        capture.stopCamera();
        super.onDestroy();
    }

    /**
     * When the user switches back this application after a pause
     */
    @Override
    protected void onStart() {
    	Log.e(TAG, "RAN ONSTART");

    	Log.d(TAG, "creating camera");
	    capture = new Capture(this);
	    Log.d(TAG, "starting camera");
	    capture.startCamera();
	    Log.d(TAG, "started camera");
        super.onStart();
    }


    /**
     * Starts the information activity when the button is pressed
     *
     * @param view not used
     */
    @Deprecated
    public void infoButton(View view){
        /*Intent intent = new Intent(this, Information.class);
        startActivity(intent);*/
    }

    public void openPreferences(View view){
	    Intent intent = new Intent(this, Preferences.class);
	    startActivity(intent);
    }

    /**
     * Deletes the current calibration configuration stored on the file system
     *
     * @param view not used
     */
    public void deleteConfig(View view){
        File configLoc = Calibration.configFile();
        boolean deleted = configLoc.delete();

        // If the file got deleted we restart the whole capture class which trails all the
        // way to calibration and resets the isCalibrated boolean.
        if(deleted) {
            Log.d(TAG, "config deleted");
            capture.stopCamera();
            capture = null;
            capture = new Capture(this);
            capture.startCamera();
        }
    }

}

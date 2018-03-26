package no.ntnu.bachelor2018.filmreader;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;

import java.io.File;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;
import no.ntnu.bachelor2018.previewImageProcessing.Calibration;

import static android.content.ContentValues.TAG;
//import no.ntnu.bachelor2018.filmreader.PiqlLib.Wrapper;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    public static Context context;  // Context for other classes MainActivity uses
    private Capture capture;        // Capture class for capturing images

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("MainActivity_init", "OpenCV loaded");
        } else {
            Log.d("MainActivity_init", "Could not load OpenCV");
        }
        Log.d("MainActivity_init", "Loading library");
        System.loadLibrary("unboxingdata");
        Log.d("MainActivity_init", "Library loaded");
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

        Log.d(TAG, "RAN ONCREATE");
        context = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getPermissions();

        // Force landscape layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    }

    /**
     * When the application is paused, e.g. when the user minimizes this and
     * switches to another application
     */
    @Override
    protected void onStop() {
    	Log.d(TAG, "RAN ONSTOP");
        capture.stopCamera();
        super.onStop();
    }


    /**
     * When the application is closed
     */
    @Override
    protected void onDestroy() {
    	Log.d(TAG, "RAN ONDESTROY");
        capture.stopCamera();
        super.onDestroy();
    }

    /**
     * When the user switches back this application after a pause
     */
    @Override
    protected void onStart() {
    	Log.d(TAG, "RAN ONSTART");

    	if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
		    setContentView(R.layout.activity_main);
	    } else {
	        setContentView(R.layout.activity_main_land);
	    }

	    capture = new Capture(this);

    	if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

		    ImageView preview = findViewById(R.id.imageView);
		    Size cSize = capture.getSize();

		    if(cSize == null){
			    Log.d(TAG, "cSize is null");
		    }
		    if(preview == null){
			    Log.d(TAG, "preview is null");
		    }

		    float scaleX, scaleY;

		    Point screenSize = new Point();
		    getWindowManager().getDefaultDisplay().getSize(screenSize);
		    scaleY = (float) cSize.getHeight() / (float) screenSize.x;
		    scaleX = (float) cSize.getWidth() / (float) screenSize.y;

		    preview.setScaleX(scaleX);
		    preview.setScaleY(scaleY);

		    /*
		    preview.setScaleX(2);
		    preview.setScaleY(2);
		    preview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		    preview.setCropToPadding(true);
		    */
	    }

	    capture.startCamera();
        super.onStart();
    }

	/**
	 * Opens up the preference activity
	 *
	 * @param view not used
	 */
    public void openPreferences(View view){
	    Intent intent = new Intent(this, FileDisplay.class);
	    startActivity(intent);
    }

    /**
     * Deletes the current calibration configuration stored on the file system
     *
     * @param view not used
     */
    public void deleteConfig(View view){
        File configLoc = Calibration.configFile();

        // If the file got deleted we restart the whole capture class which trails all the
        // way to calibration and resets the isCalibrated boolean.
        if(configLoc.delete()) {
            Log.d(TAG, "config deleted");
            capture.stopCamera();
            capture = null;
            capture = new Capture(this);
            capture.startCamera();
        }
    }

}

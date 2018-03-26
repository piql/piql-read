package no.ntnu.bachelor2018.filmreader;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.ArrayList;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;
import no.ntnu.bachelor2018.previewImageProcessing.Calibration;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity {

	private final String TAG = getClass().getSimpleName();

	public static Context context;          // Context for other classes MainActivity uses
	private Capture capture;                // Capture class for capturing images
	ArrayList<String> requiredPermissions;  // List of the missing permissions

	static {
		if (OpenCVLoader.initDebug()) {
			Log.d("MainActivity_init", "OpenCV loaded");
		} else {
			Log.d("MainActivity_init", "Could not load OpenCV");
		}
	}

	/**
	 * Checks the missing permissions for the app and adds them to a list
	 * @return true if no permissions are missing, false otherwise
	 */
	private boolean missingPermissions(){
		// List of all required permissions
		final String[] permissions = {Manifest.permission.CAMERA,
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE};

		requiredPermissions = new ArrayList<>();

		// Loop through all permissions and check if some of them are missing
		for (String permission : permissions) {
			if (ContextCompat.checkSelfPermission(this, permission)
					== PackageManager.PERMISSION_DENIED) {
				// Add missing permissions to the list
				requiredPermissions.add(permission);
			}
		}

		// If at least one permission is missing we return true
		return (requiredPermissions.size() > 0);
	}

	/**
	 * Gets the permissions required for the application (the popup dialogue on app start).
	 * Continues at the callback: onRequestPermissionsResult
	 */
	private void getPermissions() {
		// Transform the ArrayList<String> into a String[]
		String[] finalRequiredPermissions = new String[requiredPermissions.size()];
		requiredPermissions.toArray(finalRequiredPermissions);

		// Request all the missing permissions all at once. (Android supports this)
		Log.d(TAG, "Permissions missing, requesting permissions");
		ActivityCompat.requestPermissions(this, finalRequiredPermissions, 1);
	}

	/**
	 * Where the application is opened
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

		// Force landscape layout
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// If permissions are missing we ask for those, the application then starts at the callback
		if(missingPermissions()){
			getPermissions();
		} else {
			// If there are no missing permissions we start the application normally
			startCapture();
		}
	}

	/**
	 * When the user switches back this application after a stop
	 */
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "RAN ONSTART");

		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onResume(){
		super.onResume();
		Log.d(TAG, "RAN ONPAUSE");

		startCapture();
	}

	/**
	 * When the application is paused, e.g. when the user minimizes this and
	 * switches to another application
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "RAN ONSTOP");

		if (capture != null) {
			capture.stopCamera();
		}
	}

	/**
	 * When the application is closed
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "RAN ONDESTROY");

		if (capture != null) {
			capture.stopCamera();
		}
	}

	/**
	 * Callback for when then user has given and/or denied permissions
	 * @param requestCode not used
	 * @param permissions not used
	 * @param grantResults The results of the permissions
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		Log.d(TAG, "callback");

		// Loop through all the permission results and check if every single permission has been given
		for (int i = 0; i < grantResults.length; i++) {
			if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
				// If at least one permission has been denied we tell the user and close the app
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.permissions_denied));
				builder.setMessage(getResources().getString(R.string.permissions_denied_desc));
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						dialog.dismiss();
						finish();
					}
				});
				builder.create().show();
				return;
			}
		}

		// If all permissions has successfully been granted we start the application normally
		startCapture();
	}

	/**
	 * For the start of the application after the permissions have been set
	 */
	public void startCapture(){
		capture = new Capture(this);
		capture.startCamera();
	}

	/**
	 * Opens up the preference activity
	 *
	 * @param view not used
	 */
	public void openPreferences(View view) {
		Intent intent = new Intent(this, Preferences.class);
		startActivity(intent);
	}

	/**
	 * Deletes the current calibration configuration stored on the file system
	 *
	 * @param view not used
	 */
	public void deleteConfig(View view) {
		File configLoc = Calibration.configFile();

		// If the file got deleted we restart the whole capture class which trails all the
		// way to calibration and resets the isCalibrated boolean.
		if (configLoc.delete()) {
			Log.d(TAG, "config deleted");
			capture.stopCamera();
			capture = null;
			capture = new Capture(this);
			capture.startCamera();
		}
	}

}

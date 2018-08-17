package no.ntnu.bachelor2018.filmreader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.opencv.android.OpenCVLoader;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;
import no.ntnu.bachelor2018.previewImageProcessing.Calibration;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity {
    public static Context context;          // Context for other classes MainActivity uses
    public static Boolean isActive;           // If the main view with the camera is active.

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

    private final String TAG = getClass().getSimpleName();
    ArrayList<String> requiredPermissions;  // List of the missing permissions
    private static Capture capture;         // Capture class for capturing images

    /**
     * Checks the missing permissions for the app and adds them to a list
     *
     * @return true if no permissions are missing, false otherwise
     */
    private boolean missingPermissions() {
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
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "RAN ONCREATE");
        context = this;
        isActive = true;

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // If permissions are missing we ask for those, the application then starts at the callback
        if (missingPermissions()) {
            getPermissions();
        }
    }

    /**
     * When the user switches back this application after a stop
     */
    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
        Log.d(TAG, "RAN ONSTART");

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
        Log.d(TAG, "RAN ONRESUME");

        /* start capture on launch */
        startCapture();
    }

    /**
     * When the application is paused, e.g. when the user minimizes this and
     * switches to another application
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "RAN ONPAUSE");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (capture != null) {
                    capture.stopCamera();
                }
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
        Log.d(TAG, "RAN ONSTOP");
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
     * Toggle capture
     */
    public void onToggleClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            startCapture();
        } else {
            MyAsyncTask myAsyncTask = new MyAsyncTask();
            myAsyncTask.execute();
        }
    }

    /**
     * Callback for when then user has given and/or denied permissions
     *
     * @param requestCode  not used
     * @param permissions  not used
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
    }

    /**
     * For the start of the application after the permissions have been set
     *
     */
    public void startCapture() {
        setupDir();
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

        Calibration.deleteCalibration();

    }

    /**
     * Make sure required directories exist.
     */
    private void setupDir() {
        File file = new File("/data/data/filmreader.bacheloroppg.ntnu.no.filmreader/app_tardir/");
        file.mkdirs();
    }

    public static void progress() {
        Toast toast = Toast.makeText(context, "Status" , Toast.LENGTH_LONG);
        toast.show();
    }

    //Async TODO
    private class MyAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        // Not for UI
        protected Bitmap doInBackground(Void... params) {
            capture.stopCamera();

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView preview = (ImageView) findViewById(R.id.imageView);
            preview.setImageResource(android.R.color.transparent);

            TextView processingText = (TextView) findViewById(R.id.processingText);
            processingText.setText(""); // FPS: --.--

            TextView progressText = (TextView) findViewById(R.id.progressTextView);
            progressText.setVisibility(View.INVISIBLE);

            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
package no.ntnu.bachelor2018.filmreader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.EditText;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static org.opencv.core.CvType.CV_8UC3;

/**
 * Main view
 */
public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    JavaCameraView cameraView;
    Mat mRgba;
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
                        new String[]{Manifest.permission.CAMERA},
                        1);
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
        reader = new Reader(width, height);

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
        return findQuadsTest(inputFrame);
    }

    /**
     * Finds contour-lines in the given inputframe
     *
     * @param inputFrame
     * @return Mat object with contour lines drawn
     */
    /*
    private Mat contourTest(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        grayImg = inputFrame.gray();
        mRgba = inputFrame.rgba();
        Imgproc.threshold(grayImg,grayImg,0,255,Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        /// Detect edges using canny
        /// Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(grayImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE );
        /// Draw contours
        Scalar color = new Scalar(210, 210, 50);
        Imgproc.drawContours(mRgba, contours, -1, color, 4, 8, hierarchy, 1, new Point());
        return mRgba;
    }*/
/*
    private Mat houghLineTest(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        grayImg = inputFrame.gray();
        mRgba = inputFrame.rgba();
        Imgproc.Canny(grayImg, cannyImg, 100, 200);
        Imgproc.HoughLinesP(grayImg,hierarchy,1.0,Math.PI/180, 100, 10, 10);
        for (int i = 0; i < hierarchy.cols(); i++) {
            double data[] = hierarchy.get(0, i);
            double rho1 = data[0];
            double theta1 = data[1];
            double cosTheta = Math.cos(theta1);
            double sinTheta = Math.sin(theta1);
            double x0 = cosTheta * rho1;
            double y0 = sinTheta * rho1;
            Point pt1 = new Point(x0 + 10000 * (-sinTheta), y0 + 10000 * cosTheta);
            Point pt2 = new Point(x0 - 10000 * (-sinTheta), y0 - 10000 * cosTheta);
            Imgproc.line(mRgba, pt1, pt2, new Scalar(0, 0, 255), 2);
        }
        return mRgba;
    }
    */

private Mat findQuadsTest(CameraBridgeViewBase.CvCameraViewFrame inputFrame){

    grayImg = inputFrame.gray();
    //Imgproc.threshold(grayImg,cannyImg,0,255,Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
    Imgproc.adaptiveThreshold(grayImg,cannyImg,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,301,8);
    List<MatOfPoint> contours = new ArrayList<>();
    List<MatOfPoint> toDraw = new ArrayList<>();
    Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
    /// Draw contours
    Scalar color = new Scalar(210, 210, 50);
    for(MatOfPoint conto: contours){
        if(Imgproc.contourArea(conto)>200000){
            toDraw.add(conto);
        }
    }
    Imgproc.drawContours(grayImg, toDraw, -1, color, 2);
    return grayImg;
}
}

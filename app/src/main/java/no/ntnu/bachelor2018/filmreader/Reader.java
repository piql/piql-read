package no.ntnu.bachelor2018.filmreader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CaptureResult;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.concurrent.TimeUnit;

import no.ntnu.bachelor2018.imageProcessing.BgCamera;
import no.ntnu.bachelor2018.imageProcessing.Calibration;
import no.ntnu.bachelor2018.imageProcessing.FrameFinder;
import no.ntnu.bachelor2018.imageProcessing.MarkerDetection;

/**
 * Created by hcon on 13.02.18.
 */

public class Reader {

    private final String TAG = this.getClass().getSimpleName();
    private final int CAPTURETIMEOUT = 10000;

    private FrameFinder finder;
    private MarkerDetection markDetect;
    private Calibration calib;
    private Rect newROI;
    private SharedPreferences prefs;
    private BgCamera camera;
    private Mat hiresImage;
    private Context context;
    private Handler handler;
    private int width, height, blocksize;
    private Runnable captureRequest, showPicture;

    //Grayscale image, thresholded image, mask image for frame processing
    private Mat grayImg, threshImg, roiImage;

    //Outer frame corners and inner corners for marker finding mask
    private List<Point> corners, cornerInner;

    public Reader(int width, int height, final Context context){
        //TODO: Håkon add camera config parameter constructor

        this.context = context;
        finder = new FrameFinder(width, height, prefs);
        markDetect = new MarkerDetection(width,height);
        calib = new Calibration(width,height);
        newROI = null;
        this.width = width;
        this.height = height;
        roiImage = new Mat(height,width, CvType.CV_8UC1);
        roiImage.setTo(new Scalar(0,0,0));
        grayImg = new Mat(height, width, CvType.CV_8UC1);
        this.width = width;
        this.height = height;

        captureRequest  =new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "In ui thread");//test
                camera.takePicture();
            }
        };
        showPicture  =new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, ViewImage.class);
                context.startActivity(intent);
            }
        };
        handler = new Handler(Looper.getMainLooper());

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //camera = new BgCamera(context);
        //camera.takePicture();

        finder = new FrameFinder(width, height, prefs);
        markDetect = new MarkerDetection(width,height);
        threshImg = new Mat();

        //300 was a good size for 1080p image. 1080/300 = 3.6
        blocksize = (int)(height/5.6);
        blocksize += (blocksize + 1)%2;//Round up to closest odd number
    }

     /**
     * Main loop process that processes the image
     *
     * @param inputImage camera image frame
     */
    public Mat processFrame(Mat inputImage){

        //If calibration succeeded and we have an undistorted image
        if(calib.calibration(inputImage)){

            //Adjust ROI
            if(newROI == null){
                newROI = calib.getNewROI();
                newROI.width/= 2;
                newROI.x += newROI.width/2;
            }
            //Copy region of interest to image with white background.

            Imgproc.adaptiveThreshold(inputImage,threshImg,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,blocksize,0);
            //Imgproc.threshold(inputImage,threshImg,150,255,Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY_INV);

            threshImg.submat(newROI).copyTo(roiImage.submat(newROI));
            roiImage.copyTo(threshImg);
            //Find and draw corners
            corners = finder.cornerFinder(threshImg);

            for (int i = 0; i<corners.size(); i++){
                Imgproc.line(inputImage,corners.get(i),corners.get((i + 1) %corners.size()),new Scalar(255,255,255));
            }
            markDetect.findMarkers(threshImg,inputImage,corners);
            captureHiRes();
            //hiresImage.submat(0,inputImage.rows(),0,inputImage.cols()).copyTo(inputImage);
        }
        //hiresImage.submat(0,inputImage.rows(),0,inputImage.cols()).copyTo(inputImage);

        //Apply adaptive threshold


        //Draw lines between found quad

        //inputImage = markDetect.findMarkers(threshImg,inputImage,corners);


        //markDetect.findMarkers(threshImg);
        return inputImage;
    }

    private void showProcessedCapture(){
        handler.post(showPicture);
    }

    private void captureHiRes(){


        handler.post(captureRequest);

        long startTime = System.currentTimeMillis();
        while (!BgCamera.isImageReady() && System.currentTimeMillis() - startTime < CAPTURETIMEOUT){
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Log.d(TAG, "Reader was interrupted");
            }
        }

    }

}

package no.ntnu.bachelor2018.filmreader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import no.ntnu.bachelor2018.imageProcessing.Calibration;
import no.ntnu.bachelor2018.imageProcessing.FrameFinder;
import no.ntnu.bachelor2018.imageProcessing.MarkerDetection;

/**
 * Created by hcon on 13.02.18.
 */

public class Reader {

    private final String TAG = this.getClass().getSimpleName();

    private FrameFinder finder;
    private MarkerDetection markDetect;
    private Calibration calib;
    private Rect newROI;
    private int width, height, blocksize;

    //Grayscale image, thresholded image, mask image for frame processing
    private Mat threshImg, roiImage;

    //Outer frame corners and inner corners for marker finding mask
    private List<Point> corners, cornerInner;

    public Reader(){
        //TODO: Håkon add camera config parameter constructor

        finder = new FrameFinder();
        markDetect = new MarkerDetection();
        calib = new Calibration();
        newROI = null;
        threshImg = new Mat();


    }

    private void calibSize(Mat image){
        if(image.width() != this.width || image.height() != this.height){
            this.width = image.width();
            this.height = image.height();
            roiImage = new Mat(height,width, CvType.CV_8UC1);
            roiImage.setTo(new Scalar(0,0,0));
            //300 was a good size for 1080p image. 1080/300 = 3.6
            blocksize = (int)(height/5.6);
            blocksize += (blocksize + 1)%2;//Round up to closest odd number
        }
    }

     /**
     * Main loop process that processes the image
     *
     * @param inputImage camera image frame
     */
    public Mat processFrame(Mat inputImage){
        calibSize(inputImage);

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
            return threshImg;
        }
        return inputImage;
    }

}

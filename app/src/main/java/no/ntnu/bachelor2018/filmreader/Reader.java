package no.ntnu.bachelor2018.filmreader;

import android.hardware.camera2.params.StreamConfigurationMap;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


import java.util.List;
import java.util.function.DoubleToIntFunction;

import no.ntnu.bachelor2018.imageProcessing.FrameFinder;
import no.ntnu.bachelor2018.imageProcessing.MarkerDetection;

/**
 * Created by hcon on 13.02.18.
 */

public class Reader {

    private final String TAG = this.getClass().getSimpleName();

    private FrameFinder finder;
    private MarkerDetection markDetect;
    private int width, height, blocksize;

    //Grayscale image, thresholded image, mask image for frame processing
    private Mat grayImg, threshImg, kernel;

    //Outer frame corners and inner corners for marker finding mask
    private List<Point> corners, cornerInner;

    public Reader(int width, int height){
        //TODO: Håkon add camera config parameter constructor
        finder = new FrameFinder(width,height);
        markDetect = new MarkerDetection(width,height);
        this.width = width;
        this.height = height;
        grayImg = new Mat(height, width, CvType.CV_8UC1);
        threshImg = new Mat();
        //300 was a good size for 1080p image. 1080/300 = 3.6
        blocksize = (int)(height/3.6);

        blocksize += (blocksize + 1)%2;//Round up to closest odd number
    }

     /**
     * Main loop process that processes the image
     *
     * @param inputImage camera image frame
     */
    public Mat processFrame(Mat inputImage){

        //Convert to grayscale
        Imgproc.cvtColor(inputImage, grayImg, Imgproc.COLOR_BGR2GRAY);

        //Apply adaptive threshold
        Imgproc.adaptiveThreshold(grayImg,threshImg,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,blocksize,8);
        //Find and draw corners
        corners = finder.cornerFinder(threshImg);

        //Draw lines between found quad
        for (int i = 0; i<corners.size(); i++){
            Imgproc.line(inputImage,corners.get(i),corners.get((i + 1) %corners.size()),new Scalar(255,255,255));
        }
        inputImage = markDetect.findMarkers(threshImg,inputImage,corners);
        //markDetect.findMarkers(threshImg);
        return inputImage;

    }
}

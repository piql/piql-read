package no.ntnu.bachelor2018.filmreader;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


import java.util.Vector;

import no.ntnu.bachelor2018.imageProcessing.FrameFinder;
import no.ntnu.bachelor2018.imageProcessing.MarkerDetection;

/**
 * Created by hcon on 13.02.18.
 */

public class Reader {

    private final String TAG = this.getClass().getSimpleName();

    private FrameFinder finder;
    private MarkerDetection markDetect;
    private int width, height;
    private Mat grayImg, threshImg;
    private Vector<Point> corners;

    public Reader(int width, int height){
        //TODO: Håkon add camera config parameter constructor
        finder = new FrameFinder(width,height);
        markDetect = new MarkerDetection(width,height);
        this.width = width;
        this.height = height;
        grayImg = new Mat(height, width, CvType.CV_8UC1);
        threshImg = new Mat();
    }

     /**
     * Main loop process that processes the image
     *
     * @param inputImage camera image frame
     */
    public Mat processFrame(Mat inputImage){
        // Saves a grayscale image to the variable 'grayImg'
        Imgproc.cvtColor(inputImage, grayImg, Imgproc.COLOR_BGR2GRAY);
        //Apply adaptive threshold
        Imgproc.adaptiveThreshold(grayImg,threshImg,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,301,8);
        corners = finder.cornerFinder(threshImg);
        for (int i = 0; i<corners.size(); i++){
            Imgproc.line(inputImage,corners.get(i),corners.get((i + 1) %corners.size()),new Scalar(255,255,255));
            Imgproc.putText(inputImage,"" +i,corners.get(i), Core.FONT_HERSHEY_PLAIN,10,new Scalar(0,0,0),10);
        }
        //markDetect.findMarkers(threshImg);
        return inputImage;
    }
}

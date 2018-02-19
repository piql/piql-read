package no.ntnu.bachelor2018.imageProcessing;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by bruker on 13.02.2018.
 */

public class MarkerDetection {

    private final String TAG = this.getClass().getSimpleName();

    private int width, height;
    private Mat contourImage, emptyMask;
    private MatOfPoint corners;
    private MatOfPoint2f corners2f;

    private final Size windowSize = new Size(21,21);
    private final Size zeroZone = new Size(1,1);

    private final TermCriteria criteria = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 40, 0.001 );

    public MarkerDetection(int width, int height){
        this.width = width;
        this.height = height;
        contourImage = new Mat(height, width, CvType.CV_8UC1);
        corners = new MatOfPoint();
        corners2f = new MatOfPoint2f();
        emptyMask = new Mat();

    }

    /**
     * Finds corner markers.
     * @param image
     * @return
     */
    public Mat findMarkers(Mat image){
        //Apply adaptive threshold
        Point[] pointArr;
        //Imgproc.adaptiveThreshold(image,contourImage,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY,301,8);

        //Imgproc.cornerHarris(contourImage,corners2f,50,31,0.04);
        Imgproc.goodFeaturesToTrack(image,corners,20,0.5,10);
        corners.convertTo(corners2f,CvType.CV_32FC(2));

        Imgproc.cornerSubPix(image,corners2f,windowSize,zeroZone,criteria);
        pointArr = corners2f.toArray();
        for(Point pt: pointArr){
            Imgproc.circle(image,pt,10,new Scalar(255,255,255));
        }
        return image;

    }

}

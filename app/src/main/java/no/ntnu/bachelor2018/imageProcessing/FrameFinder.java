package no.ntnu.bachelor2018.imageProcessing;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Håkon Heggholmen on 13.02.2018.
 * Class is used to find the outer frame of the image
 */

public class FrameFinder {

    private final String TAG = this.getClass().getSimpleName();

    private int width, height;
    private Mat contourImage, hierarchy;
    private List<MatOfPoint> contours;
    private MatOfPoint2f contour2f;
    private MatOfInt hull;

    public FrameFinder(int width, int height){
        this.width = width;
        this.height = height;
        contourImage = new Mat(height, width, CvType.CV_8UC1);
        hierarchy = new Mat(height, width, CvType.CV_8UC1);
        contours = new ArrayList<>();
        contour2f = new MatOfPoint2f();
        hull = new MatOfInt();

    }

    /**
     * Finds corners of film frame(black boarder edge corners)
     * @param image
     * @return
     */
    public Vector<Point> cornerFinder(Mat image){
        //Init variables
        Point points[];
        Vector<Point> retPoints = new Vector<Point>();
        contours.clear();

        //Apply adaptive threshold
        Imgproc.adaptiveThreshold(image,contourImage,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,301,8);

        //Find outer contour
        Imgproc.findContours(contourImage, contours,hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //Loop through all contours
        for(MatOfPoint conto: contours){
            //Filter out small contour with area less then
            if(Imgproc.contourArea(conto)>(width/8)*(height/8)){

                //Approximate polygon line to contour
                conto.convertTo(contour2f,CvType.CV_32FC2);
                Imgproc.approxPolyDP(contour2f,contour2f,10,true);
                contour2f.convertTo(conto,CvType.CV_32S);

                points = conto.toArray();

                //Find corner points
                Imgproc.convexHull(conto,hull,true);
                //add corner points to return vector
                for(int hullId:hull.toArray()){
                    retPoints.add(points[hullId]);
                }
            }
        }
        return retPoints;
    }
}

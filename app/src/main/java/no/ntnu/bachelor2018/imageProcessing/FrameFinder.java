package no.ntnu.bachelor2018.imageProcessing;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import no.ntnu.bachelor2018.filmreader.MainActivity;
import no.ntnu.bachelor2018.filmreader.Preferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Håkon Heggholmen on 13.02.2018.
 * Class is used to find the outer frame of the image
 */

public class FrameFinder {

    private final String TAG = this.getClass().getSimpleName();

    private String mode;

    private int width, height;
    private Mat contourImage, hierarchy;
    private List<MatOfPoint> contours;
    private MatOfPoint2f contour2f;
    private MatOfInt hull;
    private List<Point> retPoints;
    private SharedPreferences prefs;


    public FrameFinder(int width, int height, SharedPreferences prefs){
        this.width = width;
        this.height = height;
        this.prefs = prefs;

        hierarchy = new Mat();
        contours = new ArrayList<>();
        contour2f = new MatOfPoint2f();
        hull = new MatOfInt();
        retPoints = new ArrayList<>();
    }

    public void setSize(int width, int height){
        this.width = width;
        this.height = height;
    }
/*
    public Mat drawEdges(Mat img, Scalar color){
        mode = prefs.getString("cont_mode", null);

        Mat grayImg = new Mat();
        Mat binImg = new Mat();
        Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(grayImg, binImg, 100, 200);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(binImg, contours, grayImg, Integer.parseInt(mode), Imgproc.CHAIN_APPROX_SIMPLE);
        int edges = contours.size();
        if(edges > 0) Log.d(TAG, "Edge points: " + edges);
            Imgproc.drawContours(img, contours, -1, color);
        return img;
    }
*/
    /**
     * Finds corners of film frame(black boarder edge corners)
     * @param image
     * @return
     */
    public List<Point> cornerFinder(Mat image){
        //Init variables
        Point points[];
        boolean done = false;
        contours.clear();
        retPoints.clear();

        //Find outer contour
        Imgproc.findContours(image, contours,hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //Loop through all contours
        for(MatOfPoint conto: contours){
            //Filter out small contour with area less then
            if(Imgproc.contourArea(conto)>Math.pow(height/3,2) && !done){

                //Approximate polygon line to contour
                conto.convertTo(contour2f,CvType.CV_32FC2);
                Imgproc.approxPolyDP(contour2f,contour2f,10,true);
                contour2f.convertTo(conto,CvType.CV_32S);

                points = conto.toArray();

                //Find corner points
                Imgproc.convexHull(conto,hull,true);
                //add corner points to return vector, only quads are accepted
                if(hull.size().area() == 4){
                    for(int hullId:hull.toArray()){
                        retPoints.add(points[hullId]);

                    }
                    done = true;
                }

            }
        }
        return retPoints;
    }
}

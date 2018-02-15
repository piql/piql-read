package no.ntnu.bachelor2018.imageProcessing;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruker on 13.02.2018.
 */

public class FrameFinder {

    private final String TAG = this.getClass().getSimpleName();

    private int width, height;
    private Mat contourImage, hierarchy;
    private List<MatOfPoint> contours;
    private List<MatOfPoint> keep;

    public FrameFinder(int width, int height){
        //TODO: Håkon add camera config parameter constructor
        this.width = width;
        this.height = height;
        contourImage = new Mat(height, width, CvType.CV_8UC1);
        hierarchy = new Mat(height, width, CvType.CV_8UC1);
        contours = new ArrayList<>();
        keep = new ArrayList<>();

    }

    /**
     * Draws the edges on a given color image.
     *
     * @param img - A {@link Mat} containing the color image to draw edges on
     * @param color - A {@link Scalar} with the edge color
     * @return A {@link Mat} with the edges drawn.
     */
    public Mat drawEdges(Mat img, Scalar color){
        Mat grayImg = new Mat();
        Mat binImg = new Mat();

        // Creates a grayscale image from the input image
        Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);

        // Creates a binary matrix out of the grayscale image
        Imgproc.Canny(grayImg, binImg, 100, 200);

        // Finds the edges and puts it in a list
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(binImg, contours, grayImg, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        int edges = contours.size();
        if(edges > 0) Log.d(TAG, "Edge points: " + edges);

        // Draws the edges on the input image
        Imgproc.drawContours(img, contours, -1, color);

        return img;
    }

    /**
     * Finds all edges on a picture and return a list containing all
     *
     * @param img - The image to find edges on
     * @return A {@link List} with all the points
     */
    public List<MatOfPoint> getEdgePoints(Mat img){
        Mat binImg = new Mat();
        Imgproc.Canny(img, binImg, 100, 200);

        List<MatOfPoint> edges = new ArrayList<>();
        Imgproc.findContours(binImg, edges, img, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        return edges;
    }


    /**
     *Finds corners of image
     * @param image
     * @return
     */
    public Mat cornerFinder(Mat image){
        //Imgproc.threshold(grayImg,cannyImg,0,255,Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Point points[];

        keep.clear();
        contours.clear();
        int sumX = 0, sumY = 0, count = 0;
        Imgproc.adaptiveThreshold(image,contourImage,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,301,8);
        Imgproc.findContours(contourImage, contours,hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        /// Draw contours
        Scalar color = new Scalar(210, 210, 50);
        for(MatOfPoint conto: contours){
            if(Imgproc.contourArea(conto)>200000){
                keep.add(conto);
                points = conto.toArray();
                for(int i = 1; i<points.length - 1; i++){
                    //Math.sqrt((points[i].x - ))
                    double res = Math.atan2(points[i+1].y - points[i-1].y, points[i+1].x - points[i-1].x) -
                            Math.atan2(points[i].y - points[i-1].y, points[i].x - points[i-1].x);
                    Math.toDegrees(res);
                    if(Math.abs(Math.toDegrees(res)) > 60){
                        Imgproc.drawMarker(image, new Point(points[i].x,points[i].y), color);
                    }

                }
            }
        }
        return image;
    }
}

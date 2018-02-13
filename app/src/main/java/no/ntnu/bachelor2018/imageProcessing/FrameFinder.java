package no.ntnu.bachelor2018.imageProcessing;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruker on 13.02.2018.
 */

public class FrameFinder {

    private final String TAG = this.getClass().getSimpleName();

    public FrameFinder(){

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

}

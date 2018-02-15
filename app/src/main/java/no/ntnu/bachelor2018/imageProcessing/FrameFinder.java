package no.ntnu.bachelor2018.imageProcessing;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by bruker on 13.02.2018.
 */

public class FrameFinder {


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
     * Returns a list of the coordinates at the edge
     *
     * @param mat
     * @return
     */
    public List<MatOfPoint> findFrame(Mat mat){
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mat, contours, null, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        contourImage = new Mat(height, width, CvType.CV_8UC4);
        return contours;
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

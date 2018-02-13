package no.ntnu.bachelor2018.imageProcessing;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruker on 13.02.2018.
 */

public class FrameFinder {


    public FrameFinder(){

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
        return contours;
    }

}

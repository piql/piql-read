package no.ntnu.bachelor2018.previewImageProcessing;

/**
 * Created by hcon on 21.03.18.
 */

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

/**
 * Class for general static image processing functions
 */
public class GeneralImgproc {

    /**
     * Calculates distance between two points
     * @param pt1
     * @param pt2
     * @return double distance
     */
    public static double distance(Point pt1, Point pt2){
        return Math.sqrt(Math.pow(pt1.x- pt2.x,2) + Math.pow(pt1.y- pt2.y,2));
    }

    /**
     * Rotates the Mat
     * @param input
     * @return
     */
    public static Mat rotateMat(Mat input, int degrees){
        Mat output = new Mat();
        Mat rotationMat = Imgproc.getRotationMatrix2D(new Point(input.width()/2, input.height()/2), degrees, 1);
        Imgproc.warpAffine(input, output, rotationMat, input.size());
        return output;
    }
}

package no.ntnu.bachelor2018.previewImageProcessing;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by Håkon on 12.03.18.
 * Prepares the image for processing in the Piql library.
 */

public class FinalProcessing {
    private int width, height;
    private Mat perspectiveImage;


    public FinalProcessing(){

    }

    private void calibSize(Mat image){
        if(image.width() != this.width || image.height() != this.height){
            this.width = image.width();
            this.height = image.height();
            perspectiveImage = new Mat(height,width, CvType.CV_8UC1);
        }
    }

    /**
     * Distance between two points
     * @param p1
     * @param p2
     * @return
     */
    private static double distance(Point p1, Point p2){
        return Math.sqrt(Math.pow(p1.x - p2.x,2) + Math.pow(p1.y - p2.y,2));
    }


    /**
     * Finalizes image for processing by:
     *  -Performing image perspective transform
     *  -Inverting the image
     * @param image
     * @param pts
     */
    public void finalizeImage(Mat image, List<Point> pts){
        calibSize(image);
        double maxWidth, maxHeight;
        if(pts.size() == 4){
            //Find new image width and height using maximum edge lengths for minimal loss
            maxWidth = Math.max(distance(pts.get(0),pts.get(1)),distance(pts.get(3),pts.get(2)));
            maxHeight = Math.max(distance(pts.get(3),pts.get(0)),distance(pts.get(1),pts.get(2)));
            
        }
    }
}

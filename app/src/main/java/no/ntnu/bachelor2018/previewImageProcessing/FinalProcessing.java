package no.ntnu.bachelor2018.previewImageProcessing;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import no.ntnu.bachelor2018.filmreader.PiqlLib.Wrapper;

//mport no.ntnu.bachelor2018.filmreader.PiqlLib.Wrapper;

/**
 * Created by Håkon on 12.03.18.
 * Prepares the image for processing in the Piql library.
 */

public class FinalProcessing {
    //Width and height of image
    private int width, height;

    //Calculated margin distance from image example
    //~64 distance between frames
    //~716 width of single frame
    //0,089385474860335 64/716 constant to find margin width
    //Rounded down to 0.08 to ensure that the neighboring frames are not included
    //in the cropped image.
    private static final double marginCoefficient = 0.08;

    //private Mat perspectiveImage;
    //Transformation matrix used to perform perspective transformation
    private Mat perspectiveMatrix;

    //Image with same size as input image and set to white. Used to invert image value.
    private Mat invertSubtract;

    private Wrapper wrapper;

    private Mat croppedImage;


    public FinalProcessing(){
        //initialize matrix
        perspectiveMatrix = new Mat(3,3,CvType.CV_32FC1);
        croppedImage = new Mat();
        wrapper = new Wrapper();
    }

    /**
     * Used to adjust image size dependent variables.
     * @param image
     */
    private void calibSize(Mat image){
        if(image.width() != this.width || image.height() != this.height){
            this.width = image.width();
            this.height = image.height();
            //perspectiveImage = new Mat(height,width, CvType.CV_8UC1);
            invertSubtract = new Mat(height,width, CvType.CV_8UC1);
            invertSubtract.setTo(new Scalar(255,255,255));

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
    public Mat finalizeImage(Mat image, List<Point> pts){
        MatOfPoint2f inputPts, targetPts;
        calibSize(image);
        double maxWidth, maxHeight;
        if(pts.size() == 4){
            inputPts = new MatOfPoint2f(pts.get(3), pts.get(2), pts.get(1), pts.get(0));

            //Find new image width and height using maximum edge lengths for minimal loss
            maxWidth = Math.max(distance(pts.get(0),pts.get(1)),distance(pts.get(3),pts.get(2)));
            maxHeight = Math.max(distance(pts.get(3),pts.get(0)),distance(pts.get(1),pts.get(2)));

            //left and right margin size
            double cropMarginWidth = maxWidth*marginCoefficient;
            double cropMarginHeight = maxHeight*marginCoefficient;
            //Calculate destination points for the corner points(inputPts)
            //Marigin sizes are added as an offset to the target points.
            targetPts = new MatOfPoint2f(
                    new Point(cropMarginWidth,cropMarginHeight),                            //Top left target point
                    new Point(maxWidth + cropMarginWidth, cropMarginHeight),            //Top right
                    new Point(maxWidth+cropMarginWidth,maxHeight+cropMarginHeight),  //Bottom right
                    new Point(cropMarginWidth,  maxHeight + cropMarginHeight));       //Bottom left

            //Get transformation matrix
            perspectiveMatrix = Imgproc.getPerspectiveTransform(inputPts,targetPts);
            //Warp image
            //New image will have a margin on all 4 sides
            Imgproc.warpPerspective(image,croppedImage,perspectiveMatrix,new Size(maxWidth + 2*cropMarginWidth,maxHeight + 2*cropMarginHeight));

            //If the found image crop is bigger then the image(happens with false positives)
            if(croppedImage.width()>image.width() || croppedImage.height()>image.height()){
                return null;
            }
            //Subtracts the image from mat filled with white(255) inverting the image.
            //Since the image is cropped in the above step(warpPerspective) a fitting size must be chosen from
            //invertSubtract
            Core.subtract(invertSubtract.submat(new Rect(0,0,croppedImage.width(),croppedImage.height())),croppedImage,croppedImage);
            processMat(croppedImage);
            return croppedImage;
        }
        return null;
    }

    public void processMat(Mat input){
        byte image[] = new byte[input.width()*input.height()];
        input.get(0,0,image);
        wrapper.getFileFromImage(input.width(), input.height(), image);
    }
}

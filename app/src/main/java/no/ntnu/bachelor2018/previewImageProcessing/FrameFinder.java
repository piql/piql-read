package no.ntnu.bachelor2018.previewImageProcessing;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import no.ntnu.bachelor2018.filmreader.Preferences;

/**
 * Created by Håkon Heggholmen on 13.02.2018.
 * Class is used to find the outer frame of the image
 */

public class FrameFinder {

    private int width, height, blocksize;           // Width and height of the image. Blocksize for thresholding.
    private Mat hierarchy, roiImage, threshImg;     // hierarchy(not used but required by findcontours)
                                                    // roiImage(cropped thresholded image).
    private List<MatOfPoint> contours;              // List of found contours
    private MatOfPoint2f contour2f;                 // More precise points required by approxpolyDP
    private MatOfInt hull;                          // List of non-convex points in the contour
    private List<Point> retPoints;                  // Final list of 4 corner points to return.
    private Rect roi;                               // Region of interest used to limit processing area.

    //Parameter scale constant values. Scaling linearly for now.
    //Can be extended using newtons method or other approximation functions.
    //Used for scaling threshold 1 and to for canny edge detection and epsilon for approxPolyDP
    private static final double
        EPSILONB = -3.2,
        EPSILONA = 0.01764705882,
        THRESH1B = 20,
        THRESH1A = 0.07352941176,
        THRESH2B = 140,
        THRESH2A = 0.1470588235;
    private double epsilon, thresh1, thresh2;


    public FrameFinder(){
        //Region of interest(area to process)
        roi = new Rect(0, 0, width, height);
        //Required for findcontours, but not used
        hierarchy = new Mat();
        //Initial contours
        contours = new ArrayList<>();
        contour2f = new MatOfPoint2f();
        hull = new MatOfInt();
        retPoints = new ArrayList<>();

    }

    /**
     * Used to adjust image size dependent variables.
     * @param image
     */
    private void calibSize(Mat image){
        if(image.width() != this.width || image.height() != this.height){
            this.width = image.width();
            this.height = image.height();
            roiImage = new Mat(height,width, CvType.CV_8UC1);
            threshImg= new Mat(height,width, CvType.CV_8UC1);
            roiImage.setTo(new Scalar(0,0,0));
            //300 was a good size for 1080p image. 1080/300 = 3.6
            //blocksize = (int)(height/30);
            //blocksize += (blocksize + 1)%2;//Round up to closest odd number required by adaptive thresholding

            //Calculate thresh1, thresh2 and epsilon for this resolution
            thresh1 = height * THRESH1A + THRESH1B;
            thresh2 = height * THRESH2A + THRESH2B;
            epsilon = height * EPSILONA + EPSILONB;

            //Not likely to happen, but setting to 0 just in case to not have opencv crash
            //from invalid negative parameter
            if(epsilon < 0) epsilon = 0;
            if(thresh2 < 0) thresh2 = 0;
            if(thresh1 < 0) thresh1 = 0;
        }
    }

    /**
     * Finds corners of film frame(black boarder edge corners)
     * @param image
     * @param overlay
     * @return List of found corner points
     */
    public List<Point> cornerFinder(Mat image, Overlay overlay){
        //Init variables
        calibSize(image);
        Point points[];
        boolean done = false;
        contours.clear();
        retPoints.clear();
        threshROI(image,overlay);

        //Find outer contour
        Imgproc.findContours(threshImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //Loop through all contours
        for(MatOfPoint conto: contours){
            //Filter out small contour with area less then
            if(conto.toArray().length > 3 && Imgproc.contourArea(conto)>roi.area()/6 && !done){

                conto.convertTo(contour2f,CvType.CV_32FC2);

                //Approximate polygon line to contour
                Imgproc.approxPolyDP(contour2f,contour2f,epsilon,true);
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

        //Draw overlay if cornerFinder is selected
        if(Preferences.isPreviewType(GeneralImgproc.PreviewType.MARKERDETECT)){
            overlay.overrideDisplayImage(image);
            if(done){
                overlay.addPolyLine(retPoints);
            }
        }

        return retPoints;
    }

    public void setROI(Rect roi,Mat image){
        //Set/reset the ROI
        calibSize(image);
        this.roi = roi;
        //Clear roi image(in case the new ROI size is smaller)
        roiImage.setTo(new Scalar(0,0,0));
    }


    //Constant dilation kernel
    private static final Mat DILATEKERNEL = Mat.ones(5,5,CvType.CV_8U);

    /**
     * Thresholds and copys the image into a cropped format within the region of interest(ROI)
     * @param inputImage
     */
    private void threshROI(Mat inputImage, Overlay overlay){
        //Copy region of interest to image with white background.
        //Imgproc.adaptiveThreshold(inputImage.submat(roi),threshImg.submat(roi),255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY_INV,blocksize,3);

        Imgproc.Canny(inputImage.submat(roi),threshImg.submat(roi),thresh1 ,thresh2,5,true);
        Imgproc.dilate(threshImg.submat(roi),threshImg.submat(roi),DILATEKERNEL);

        //Display thesholded image if the preference is set.
        if(Preferences.isPreviewType(GeneralImgproc.PreviewType.THRESHOLDED)){
            overlay.overrideDisplayImage(threshImg);
        }
        threshImg.submat(roi).copyTo(roiImage.submat(roi));
        roiImage.copyTo(threshImg);

        //Draw threshold overlay if selected

    }

}

package no.ntnu.bachelor2018.previewImageProcessing;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Håkon Heggholmen on 13.02.2018.
 * Class is used to find the outer frame of the image
 */

public class FrameFinder {

    private final String TAG = this.getClass().getSimpleName();

    private int width, height, blocksize;
    private Mat hierarchy, roiImage, threshImg;
    private List<MatOfPoint> contours;
    private MatOfPoint2f contour2f;
    private MatOfInt hull;
    private List<Point> retPoints;
    private Rect roi;


    public FrameFinder(){
        //Region of interest(area to process)
        roi = null;
        //Required for findcontours, but not used
        hierarchy = new Mat();
        //Initial contours
        contours = new ArrayList<>();

        contour2f = new MatOfPoint2f();
        hull = new MatOfInt();
        retPoints = new ArrayList<>();
    }

    private void calibSize(Mat image){
        if(image.width() != this.width || image.height() != this.height){
            this.width = image.width();
            this.height = image.height();
            roiImage = new Mat(height,width, CvType.CV_8UC1);
            threshImg= new Mat(height,width, CvType.CV_8UC1);
            roiImage.setTo(new Scalar(0,0,0));
            //300 was a good size for 1080p image. 1080/300 = 3.6
            blocksize = (int)(height/3.6);
            blocksize += (blocksize + 1)%2;//Round up to closest odd number
        }
    }

    public Mat drawEdges(Mat img, Scalar color){
        //mode = prefs.getString("cont_mode", null);

        Mat grayImg = new Mat();
        Mat binImg = new Mat();
        //Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(img, binImg, 100, 200);
        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(binImg, contours, grayImg, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        int edges = contours.size();
        Log.d(TAG, "Edge points: " + edges);
        if(edges > 0) {
            Imgproc.drawContours(img, contours, -1, color);
        }
        return img;
    }

    /**
     * Finds corners of film frame(black boarder edge corners)
     * @param image
     * @return
     */
    public List<Point> cornerFinder(Mat image){
        //Init variables
        calibSize(image);
        Point points[];
        boolean done = false;
        contours.clear();
        retPoints.clear();
        copyToROI(image);

        //Find outer contour
        Imgproc.findContours(threshImg, contours,hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

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

    public void setROI(Rect roi,Mat image){
        //Set/reset the ROI
        calibSize(image);
        this.roi = roi;
        //Clear roi image(in case the new ROI size is smaller)
        roiImage.setTo(new Scalar(0,0,0));
    }

    /**
     * Thresholds and copys the image into a cropped format within the region of interest(ROI)
     * @param inputImage
     */
    private void copyToROI(Mat inputImage){
        //Copy region of interest to image with white background.

        Imgproc.adaptiveThreshold(inputImage,threshImg,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,blocksize,0);

        threshImg.submat(roi).copyTo(roiImage.submat(roi));
        roiImage.copyTo(threshImg);
    }
}
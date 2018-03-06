package no.ntnu.bachelor2018.imageProcessing;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruker on 13.02.2018.
 */

public class MarkerDetection {

    private final String TAG = this.getClass().getSimpleName();

    //Width,height of image. Template resolution(even number)
    private int width, height, templateRes;
    private Mat  mask,maskedImage,template, hierarchy;
    private List<MatOfPoint> contours;
    private MatOfPoint2f contour2f;
    private double maskSize;

    private final Scalar black = new Scalar(0,0,0);
    private final Scalar white = new Scalar(255,255,255);
    private final TermCriteria criteria = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 40, 0.001 );

    public MarkerDetection(){

        hierarchy = new Mat();
        contours = new ArrayList<>();
        contour2f = new MatOfPoint2f();
        template = new Mat();
        maskSize = 0.15;
        template = new Mat(templateRes,templateRes,CvType.CV_8UC1);
        template.setTo(black);
        Imgproc.rectangle(template,new Point(0,0),new Point(templateRes/2 -1,templateRes/2-1),white);
        Imgproc.rectangle(template,new Point(templateRes/2 ,templateRes/2-1),new Point(templateRes -1,templateRes-1),white);
    }

    private void calibSize(Mat image){
        if(image.width() != this.width || image.height() != this.height){
            this.width = image.width();
            this.height = image.height();
            mask = new Mat(height,width,CvType.CV_8UC1);
            maskedImage = new Mat(width,height,CvType.CV_8UC1);

        }
    }

    /**
     * Finds corner markers.
     * @param image
     * @return
     */
    public Mat findMarkers(Mat image, Mat overlayTest, List<Point> frameCorners){
        //TODO(håkon) return the marker points.
        calibSize(image);
        Point centerPoint = new Point(0,0);
        Point bestContours[][] = new Point[4][8];
        int foundContours  =0;
        Point ptArray[];

        if(frameCorners.size() == 4) {

            //Clear masked image for next iteration
            maskedImage.setTo(black);
            contours.clear();

            //Find mask for the image corners
            maskFinder(frameCorners);

            //Copy part of the thresholded image to the masked image using the mask
            if(!maskedImage.empty()){
                image.copyTo(maskedImage, mask);
            }



            //Imgproc.erode(maskedImage,maskedImage,new Mat(),new Point(0,0),2);

            //Find contours in the masked image

            Imgproc.findContours(maskedImage,contours,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
            //Imgproc.cornerHarris(maskedImage,maskedImage,5,11,0.1);
            //Core.normalize(maskedImage,maskedImage,0,255,Core.NORM_MINMAX,CvType.CV_32FC1,mask);
            //Core.convertScaleAbs(maskedImage,maskedImage);

            //Imgproc.Canny(maskedImage,maskedImage,20,220,5,true);
            //Imgproc.HoughLines(maskedImage,hough,1,Math.PI/180,100);

            /*Log.d(TAG,hough.size() + "");
            if(!hough.empty()){
                for(int i = 0; i < hough.rows(); i++ )
                {
                    lineData = hough.get(i,0);
                    double rho = lineData[0], theta = lineData[1];
                    Point pt1 = new Point(), pt2 = new Point();
                    double a = Math.cos(theta), b = Math.sin(theta);
                    double x0 = a*rho, y0 = b*rho;
                    pt1.x = (x0 + 1000*(-b));
                    pt1.y = (y0 + 1000*(a));
                    pt2.x = (x0 - 1000*(-b));
                    pt2.y = (y0 - 1000*(a));
                    Imgproc.line( overlayTest, pt1, pt2, new Scalar(0,0,255), 1);
                }
            }*/

            /*
            for(int i = 0; i<houghP.cols();i++){
                for(int a = 0; a<houghP.rows();a++){
                    lineData = houghP.get(a,i);

                }
            }*/

            for(MatOfPoint pts:contours){

                //Convert to matofpoint2f required by approxPoylDp and approximate
                pts.convertTo(contour2f,CvType.CV_32FC2);

                //Approximate polygon line with only strong corners
                Imgproc.approxPolyDP(contour2f,contour2f,5,true );

                //Select contours with 8 points
                ptArray  = contour2f.toArray();
                if(ptArray.length == 8){
                    foundContours++;

                    //Get average point
                    for(Point pt: ptArray) {
                        //Imgproc.drawMarker(overlayTest,pt,new Scalar(255,0,0));
                        centerPoint.x += pt.x;
                        centerPoint.y += pt.y;
                    }
                    centerPoint.x /= 8;centerPoint.y /= 8;
                    Imgproc.circle(overlayTest,centerPoint,10,new Scalar(255,0,0),10);
                    centerPoint.x = 0; centerPoint.y = 0;
                }
            }

            //maskedImage.copyTo(overlayTest,mask);//TEST code for debugging
            //Find area of biggest corner marker
            /*
            for (MatOfPoint pts:contours){
                area = Imgproc.contourArea(pts);
                if(area > bestArea){
                    bestArea = area;
                }

            }*/

            /*
            //Draw all acceptable contours (at least half the area of the biggest contour
            for (MatOfPoint pts:contours){
                area = Imgproc.contourArea(pts);

                //If the contour area is within accepted percentage and found less than four max distance lines
                if(area > bestArea*acceptedAreaSize && linesFound < 4){
                    bestLength = 0;
                    ptArray = pts.toArray();

                    //Loop through all combinations of points in the contour
                    for(int i = 0; i<ptArray.length; i++){
                        for(int a = i + 1; a<ptArray.length; a++){

                            //Calculate distance between points
                            ptDistance = Math.sqrt(Math.pow(ptArray[i].x - ptArray[a].x,2)+Math.pow(ptArray[i].y - ptArray[a].y,2));

                            //Save the two points with the most distance so far
                            if(ptDistance>bestLength){
                                //Set new best length¶
                                bestLength = ptDistance;
                                bestPts[linesFound][0] = ptArray[i];
                                bestPts[linesFound][1] = ptArray[a];
                            }
                        }
                    }

                    linesFound++;
                }

            }
            //Draw the points for testing;//TODO(håkon) make debug/test mode
            for(int i = 0; i<linesFound; i++){
                Imgproc.drawMarker(overlayTest,bestPts[i][0],white);
                Imgproc.drawMarker(overlayTest,bestPts[i][1],white);
            }*/

        }

        return overlayTest;
    }


    //Calculates distance between two points
    private double distance(Point pt1, Point pt2){
        return Math.sqrt(Math.pow(pt1.x- pt2.x,2) + Math.pow(pt1.y- pt2.y,2));
    }


    /**
     * Adjusts mask size until marker is found.
     * Is used to adjust for different frame resolutions as the markers vary in size.
     */

    //Fills in the mask
    private void maskFinder(List<Point> pts){
        Point topVec, midPoint, botVec,ptOver,ptUnder,current, center = new Point(0,0);
        mask.setTo(black);

        //Find center point
        for(Point pt : pts){
            center.x += pt.x;center.y += pt.y;
        }
        center.x/= pts.size();center.y/= pts.size();

        for(Point pt: pts){
            pt.x += 0.03*(center.x - pt.x);
            pt.y += 0.03*(center.y - pt.y);
        }


        //Find distance between corners for mask
        for(int i = 0; i<pts.size();i++){
            current = pts.get(i);
            //Get the points connected to this point in the quad
            ptOver= pts.get((i+3)%4);
            ptUnder= pts.get((i+1)%4);
            //Create vectors for rotated square construction
            topVec = new Point(maskSize*(ptOver.x - current.x),maskSize*(ptOver.y - current.y));
            botVec = new Point(maskSize*(ptUnder.x - current.x),maskSize*(ptUnder.y - current.y));

            //Find middle corner point from vectors

            midPoint = new Point(current.x + topVec.x + botVec.x,current.y + topVec.y + botVec.y);

            //Set vectors to top and bottom corners for this square in the mask
            topVec.x += current.x;topVec.y += current.y;
            botVec.x += current.x;botVec.y += current.y;
            Imgproc.fillConvexPoly(mask, new MatOfPoint(current,botVec,midPoint,topVec),white);
        }
    }
}

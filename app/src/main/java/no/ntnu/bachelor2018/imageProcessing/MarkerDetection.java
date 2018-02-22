package no.ntnu.bachelor2018.imageProcessing;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruker on 13.02.2018.
 */

public class MarkerDetection {

    private final String TAG = this.getClass().getSimpleName();

    private int width, height;
    private Mat  mask,maskedImage,kernel, hierarchy;
    private List<MatOfPoint> contours;

    private final Scalar black = new Scalar(0,0,0);
    private final Scalar white = new Scalar(255,255,255);

    private final TermCriteria criteria = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 40, 0.001 );

    public MarkerDetection(int width, int height){
        this.width = width;
        this.height = height;
        mask = new Mat(height,width,CvType.CV_8UC1);
        maskedImage = new Mat(height,width,CvType.CV_8UC1);
        kernel = new Mat(height,width,CvType.CV_8UC1);
        hierarchy = new Mat();
        contours = new ArrayList<>();



    }

    /**
     * Finds corner markers.
     * @param image
     * @return
     */
    public Mat findMarkers(Mat image, Mat overlayTest, List<Point> frameCorners){
        //TODO(håkon) return the marker points.
        double bestArea = 0;
        double bestLength = 0;
        double acceptedAreaSize = 0.5;//TODO(håkon):parameterize this
        double area,ptDistance;
        int linesFound = 0;
        Point bestPts[][] = new Point[4][2];
        Point ptArray[];

        if(frameCorners.size()>0) {
            contours.clear();
            maskedImage.setTo(black);
            maskFinder(frameCorners);
            image.copyTo(maskedImage, mask);
            Imgproc.findContours(maskedImage,contours,hierarchy,Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            //Find area of biggest corner marker
            for (MatOfPoint pts:contours){
                area = Imgproc.contourArea(pts);
                if(area > bestArea){
                    bestArea = area;
                }

            }

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
                                //Set new best length
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
            }

        }

        return overlayTest;
    }


    //Fills in the mask
    private void maskFinder(List<Point> pts){
        Point topVec, midPoint, botVec,ptOver,ptUnder,current;
        mask.setTo(black);


        //Find distance between corners for mask
        for(int i = 0; i<pts.size();i++){
            current = pts.get(i);
            //Get the points connected to this point in the quad
            ptOver= pts.get((i+3)%4);
            ptUnder= pts.get((i+1)%4);
            //Create vectors for rotated square construction
            topVec = new Point(0.15*(ptOver.x - current.x),0.15*(ptOver.y - current.y));//TODO(håkon)Parameterize this.
            botVec = new Point(0.15*(ptUnder.x - current.x),0.15*(ptUnder.y - current.y));

            //Find middle corner point from vectors

            midPoint = new Point(current.x + topVec.x + botVec.x,current.y + topVec.y + botVec.y);

            //Set vectors to top and bottom corners for this square in the mask
            topVec.x += current.x;topVec.y += current.y;
            botVec.x += current.x;botVec.y += current.y;
            Imgproc.fillConvexPoly(mask, new MatOfPoint(current,botVec,midPoint,topVec),white);
        }
    }
}

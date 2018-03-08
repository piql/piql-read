package no.ntnu.bachelor2018.previewImageProcessing;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Created by bruker on 13.02.2018.
 */

public class MarkerDetection {

    private final String TAG = this.getClass().getSimpleName();

    //Width,height of image. Template resolution(even number)
    private int width, height;
    private Mat  mask,maskedImage,template, hierarchy, saddlePoints;
    private List<MatOfPoint> contours;
    private MatOfPoint2f contour2f;
    private double maskSize;

    private final Scalar black = new Scalar(0,0,0);
    private final Scalar white = new Scalar(255,255,255);
    private final int templateRes  = 40;
    private final TermCriteria criteria = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 40, 0.001 );

    public MarkerDetection(){

        hierarchy = new Mat();
        contours = new ArrayList<>();
        contour2f = new MatOfPoint2f();
        template = new Mat();
	    maskSize = 0.15;
        template = new Mat(templateRes,templateRes,CvType.CV_8UC1);
        template.setTo(white);
        Imgproc.rectangle(template,new Point(0,0),new Point(templateRes/2 -1,templateRes/2-1),black);
        Imgproc.rectangle(template,new Point(templateRes/2 ,templateRes/2-1),new Point(templateRes -1,templateRes-1),black);
    }

    private void calibSize(Mat image){
        if(image.width() != this.width || image.height() != this.height){
            this.width = image.width();
            this.height = image.height();
            mask = new Mat(height,width,CvType.CV_8UC1);
            maskedImage = new Mat(width,height,CvType.CV_8UC1);
            saddlePoints = new Mat(height,width,CvType.CV_8UC1);
        }
    }

    /**
     * Finds corner markers.
     * @param image
     * @return
     */
    public boolean findMarkers(Mat image, Mat overlayTest, List<Point> frameCorners){
        //TODO(håkon) return the marker points.
        calibSize(image);

        if(frameCorners.size() == 4) {
            //Find mask for the image corners
            corner_detect5(overlayTest,saddlePoints,maskFinder(frameCorners));
            saddlePoints.copyTo(overlayTest);
            return true;
        }

        return false;
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
    private List<Rect> maskFinder(List<Point> pts){
        Point topVec, midPoint, botVec,ptOver,ptUnder,current, center = new Point(0,0);
        mask.setTo(black);
        List<Rect> result = new ArrayList<>();

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

            //Create bounding rectangles for marker detection mask.
            result.add(Imgproc.boundingRect(new MatOfPoint(topVec,botVec,current,midPoint)));
        }
        return result;
    }


    /**
     * The ChESS corner detection algorithm
     *
     * Copyright 2010-2012 Stuart Bennett
     *
     * Permission is hereby granted, free of charge, to any person obtaining a copy
     * of this software and associated documentation files (the "Software"), to
     * deal in the Software without restriction, including without limitation the
     * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
     * sell copies of the Software, and to permit persons to whom the Software is
     * furnished to do so, subject to the following conditions:
     *
     * The above copyright notice and this permission notice shall be included in
     * all copies or substantial portions of the Software.
     *
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
     * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
     * IN THE SOFTWARE.

     /**
     * Perform the ChESS corner detection algorithm with a 5 px sampling radius
     *  @param    image    input image
     * @param    output    output response image
     * @param mask
     */
    private void corner_detect5(final Mat image, Mat output, List<Rect> mask) {
        int x, y;
        Mat currentROI, outputROI;
        double circular_sample[] = new double[16];
        output.setTo(black);

        for (Rect roi : mask) {

            //Set current region of interest(Current corner mask)
            currentROI = image.submat(roi);
            outputROI = output.submat(roi);
            for (y = 7; y < currentROI.height() - 7; y++) {
                for (x = 7; x < currentROI.width() - 7; x++) {
                    //Could not find a way to acsess image as an array. the used get method is not efficient.
                    //TODO(håkon) optimize code if efficient mat to array is found.
                    //circular_sample[2] =    image.get(x - 2, y - 5)[0];
                    circular_sample[2] = currentROI.get(y - 5, x - 2)[0];
                    circular_sample[1] = currentROI.get(y, x - 2)[0];
                    circular_sample[0] = currentROI.get(y - 5, x + 2)[0];
                    circular_sample[8] = currentROI.get(y + 5, x - 2)[0];
                    circular_sample[9] = currentROI.get(y + 5, x)[0];
                    circular_sample[10] = currentROI.get(y + 5, x + 2)[0];
                    circular_sample[3] = currentROI.get(y - 4, x - 4)[0];
                    circular_sample[15] = currentROI.get(y - 4, x + 4)[0];
                    circular_sample[7] = currentROI.get(y + 4, x - 4)[0];
                    circular_sample[11] = currentROI.get(y + 4, x + 4)[0];
                    circular_sample[4] = currentROI.get(y - 2, x - 5)[0];
                    circular_sample[14] = currentROI.get(y - 2, x + 5)[0];
                    circular_sample[6] = currentROI.get(y + 2, x - 5)[0];
                    circular_sample[12] = currentROI.get(y + 2, x + 5)[0];
                    circular_sample[5] = currentROI.get(y, x - 5)[0];
                    circular_sample[13] = currentROI.get(y, x + 5)[0];

                    // purely horizontal local_mean samples
                    double local_mean = (currentROI.get(y, x - 1)[0] + currentROI.get(y, x)[0] + currentROI.get(y, x + 1)[0]) * 16 / 3;

                    long sum_response = 0;
                    long diff_response = 0;
                    long mean = 0;

                    int sub_idx;
                    for (sub_idx = 0; sub_idx < 4; ++sub_idx) {
                        double a = circular_sample[sub_idx];
                        double b = circular_sample[sub_idx + 4];
                        double c = circular_sample[sub_idx + 8];
                        double d = circular_sample[sub_idx + 12];

                        sum_response += abs(a - b + c - d);
                        diff_response += abs(a - c) + abs(b - d);
                        mean += a + b + c + d;
                    }
                    outputROI.put(y, x, (double) (sum_response - diff_response - abs(mean - local_mean)));
                }
            }
        }
    }
}



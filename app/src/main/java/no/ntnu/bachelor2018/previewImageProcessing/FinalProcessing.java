package no.ntnu.bachelor2018.previewImageProcessing;

import android.content.Intent;
import android.util.Log;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;
import no.ntnu.bachelor2018.filmreader.FileDisplay;
import no.ntnu.bachelor2018.filmreader.MainActivity;
import no.ntnu.bachelor2018.filmreader.PiqlLib.Wrapper;

import static android.content.ContentValues.TAG;

//mport no.ntnu.bachelor2018.filmreader.PiqlLib.Wrapper;

/**
 * Created by Håkon on 12.03.18.
 * Prepares the image for processing in the Piql library.
 */

public class FinalProcessing {
    //Width and height of image
    private int width, height;
    private static final double maskSize = 0.15;

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


    private Mat croppedImage;

    private Mat rotatedImage;


    public FinalProcessing(){
        //initialize matrix
        perspectiveMatrix = new Mat(3,3,CvType.CV_32FC1);
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
            croppedImage = new Mat(height,width, CvType.CV_8UC1);
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
    public Mat finalizeImage(Mat image, List<Point> pts, Overlay overlay){
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
                    new Point(cropMarginWidth,cropMarginHeight),                           //Top left target point
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

            rotatedImage = rotateImage(targetPts,croppedImage,overlay);

            //Failed to rotate
            if(rotatedImage == null){
                return null;
            }
            if(processMat(rotatedImage)){
                // Start activity for showing the tar file
                Intent intent = new Intent(MainActivity.context, FileDisplay.class);
                MainActivity.context.startActivity(intent);
            }
            return rotatedImage;
        }
        return null;

    }

    public boolean processMat(Mat input){
        if(input != null && !input.empty()){
            byte image[] = new byte[input.width()*input.height()];
            input.get(0,0,image);
            return Wrapper.getFileFromImage(input.width(), input.height(), image);
        }
        return false;
    }

    /**
     * Gets lines along the edges to check for rotation
     * Looks for the grayscale information edge where half of the
     * line will be black and half white.
     * @param pts initial corner points
     * @return four bounding rectangles around corner positions
     */
    private Point[][] getRotateChecklines(List<Point> pts, Overlay overlay){
        Point topVec, botVec,ptOver,ptUnder,current;
        Point retPts[][] = new Point[4][2];

        //Find distance between corners for mask
        if(pts.size() == 4){
            for(int i = 0; i<4;i++){
                current = pts.get(i);
                //Get the points connected to this point in the rectangle
                ptOver= pts.get((i+3)%4);
                ptUnder= pts.get((i+1)%4);
                //Create vectors for sample line construction
                topVec = new Point(maskSize*(ptOver.x - current.x),maskSize*(ptOver.y - current.y));
                botVec = new Point(maskSize*(ptUnder.x - current.x),maskSize*(ptUnder.y - current.y));

                //Set the left neighbour point's end line
                retPts[(i+3)%4][1] = new Point(
                        current.x + topVec.x + (botVec.x/4),
                        current.y + topVec.y + (botVec.y/4)
                );
                //Set current point's neighbour point's end line
                retPts[i][0] = new Point(
                        current.x + botVec.x + (topVec.x/4),
                        current.y + botVec.y + (topVec.y/4)
                );
            }
        }


        //Add sample lines to overlay
        for(int i = 0; i<4; i++){
            List<Point> line = new ArrayList<>();
            line.add(retPts[i][0]);
            line.add(retPts[i][1]);
            overlay.addPolyLine(line);
        }
        return retPts;
    }

    /**
     * Rotates the image
     * @param corners
     * @param image
     */
    private Mat rotateImage(MatOfPoint2f corners, Mat image, Overlay overlay){
        List<Point> pts = corners.toList();
        Point pts2[] = corners.toArray();

        Point[][] alignCheck  = getRotateChecklines(pts, overlay);
        double bestScore = 0;
        int bestScoreIndex = -1;
        for(int i = 0; i<4; i++){
            //Get the image score
            overlay.addText("P" + i, pts.get(i));
            overlay.addText("P" + i, pts2[i]);
            double score = sampleAndScore(alignCheck[i][0], alignCheck[i][1],image);
            if(score>bestScore){
                bestScoreIndex = i;
                bestScore = score;
            }
        }

        //If sampleAndScore did not fail.
        if(bestScoreIndex != -1){
            /*Rotate image.
            * The best scoring line should be at the top
            * best = 0, rotation = 180
            * best = 1, rotation = 270
            * best = 2, rotation = 0
            * best = 3, rotation = 90
            * => RP_n = ((n+2)%4)*90
            */
            return GeneralImgproc.rotateMat(image,bestScoreIndex*90);
            //overlay.addText("Best:" + bestScoreIndex,new Point(image.width()/2, image.height()/2));
            //return image;
        }
        return null;
    }

    /**
     * Samples the pixels in the line between p1 and p2
     * and returns a score
     * @param p1
     * @param p2
     * @return
     */
    private double sampleAndScore(Point p1, Point p2, Mat image){
        double avg1 = 0, avg2 = 0,ptDistance;
        Point ptMin, ptMax;
        //Vertical line
        if((p1.x - p2.x) == 0 && (p1.y - p2.y != 0)){
            //Find top and bottom point
            if(p1.y > p2.y){
                ptMin = p2;
                ptMax = p1;
            }else {
                ptMin = p1;
                ptMax = p2;
            }
            ptDistance = ptMax.y - ptMin.y;

            //Get submat of first half
            Mat sampleArea = image.submat((int)ptMin.y, (int)(ptMin.y + ptDistance/2) -1,(int)p1.x-1, (int)p1.x+1);
            avg1 = Core.mean(sampleArea).val[0];

            //Get submat of second half
            sampleArea = image.submat((int)(ptMin.y + ptDistance/2), (int)ptMax.y,(int)p1.x-1, (int)p1.x+1);
            avg2 = Core.mean(sampleArea).val[0];

            return Math.abs(avg1 - avg2);
        }
        //generalising this function would mean swapping point coordinates,
        //which could lead to faults while getting the image's submat
        //Horizontal line:
        else if((p1.y - p2.y) == 0 && (p1.x - p2.x != 0)){
            //Find top and bottom point
            if(p1.x > p2.x){
                ptMin = p2;
                ptMax = p1;
            }else {
                ptMin = p1;
                ptMax = p2;
            }
            ptDistance = ptMax.x - ptMin.x;

            //Get submat of first half
            Mat sampleArea = image.submat((int)p1.y -1, (int)p1.y+1,(int)ptMin.x, (int)(ptMin.x + ptDistance/2) -1);
            avg1 =Core.mean(sampleArea).val[0];

            //Get submat of second half
            sampleArea = image.submat((int)p1.y-1, (int)p1.y+1,(int)(ptMin.x + ptDistance/2),(int)ptMax.x);
            avg2 = Core.mean(sampleArea).val[0];

            return Math.abs(avg1 - avg2);
        }else{
            //Line as not vertical or horizontal! input was wrong
            Log.d(TAG,"INVALID INPUT: points are not on horizontal or vertical line"
            + p1 + "\n" + p2 + "\n");
        }
        return -1;
    }

}

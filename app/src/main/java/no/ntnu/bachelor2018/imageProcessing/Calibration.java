package no.ntnu.bachelor2018.imageProcessing;

import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by hcon on 26.02.18.
 */

public class Calibration {
    private MatOfPoint2f corners;
    private int height, width;
    private List<Mat> imagePoints;
    private List<Mat> objectPoints;
    private MatOfPoint3f obj;
    private MatOfPoint2f imageCorners;
    private int boardsNumber;
    private int numCornersHor;
    private int numCornersVer;
    private int successes;
    private long pictureTakenTime;
    private Rect newROI;
    private Mat intrinsic;
    private Mat newCameraMatrix;
    private Mat undistorted;
    private Mat distCoeffs;
    private Mat grayImage;
    private boolean isCalibrated;
    private final int pictureDelayMS = 100;

    /**
     * Calibrates camera using the input image or undistorts the input image if calibrated.
     * @param width of the input image
     * @param height of the input image
     */
    public Calibration(int width, int height){
        this.height = height;
        this.width = width;

        //Target points for the checkerboard corners used in calibration
        obj = new MatOfPoint3f();

        //Refined corner points from all the calibration images.
        imagePoints = new ArrayList<>();

        //Corners refined by cornersubpix
        imageCorners = new MatOfPoint2f();

        //List of target object points for all the frames(required by configuration)
        objectPoints = new ArrayList<>();

        //Camera matrix with initial values
        intrinsic = new Mat();
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(intrinsic);
        intrinsic.put(0, 0, 1.0);

        newCameraMatrix = new Mat();

        //Distance coefficients
        distCoeffs = new Mat();

        //Sucsessfully captured images for configuration
        successes = 0;

        //Region of interest after undistortion
        newROI = new Rect();

        //Counter for taking a new image
        pictureTakenTime = 0;

        //Undistorted grayscale image
        undistorted = new Mat(width,height,CvType.CV_8UC1);

        isCalibrated = false;

        //Amount of internal corners in the checkerboard pattern TODO(håkon) parameterize this.
        numCornersHor = numCornersVer = 15;

        //Number of pictures required to configure the camera. More pictures = better calibration
        // TODO(håkon) parameterize this.
        boardsNumber = 20;

        //Grayscale converted image
        grayImage = new Mat(width,height,CvType.CV_8UC1);

        int numSquares = numCornersHor * numCornersVer;

        //Initialise target grid points for calibration
        for (int i = 0; i < numSquares; i++)
            obj.push_back(new MatOfPoint3f(new Point3(i / numCornersHor, i % numCornersVer, 0.0f)));

    }


    /**
     * Returns rect where pixel deformation is not occurring
     * @return Rect region of interest after distortion correction
     */
    public Rect getNewROI(){
        if(isCalibrated){
            return newROI;
        }else{
            return null;
        }
    }

    /**
     * Calibrates camera or undistorts image using input frame.
     * @param inputFrame
     * @return
     */
    public boolean calibration(Mat inputFrame)
    {
        Imgproc.cvtColor(inputFrame, grayImage, Imgproc.COLOR_BGR2GRAY);

        //Undistort image if the camera is already calibrated
        if(isCalibrated){
            Point tmpPoint = new Point();
            double cords[] = new double[4];
            Imgproc.undistort(grayImage,undistorted, newCameraMatrix, distCoeffs);
            undistorted.copyTo(inputFrame);
            return true;
        }
        //Take picture for calibration if timer has passed and not done.
        else if (successes < this.boardsNumber && (System.currentTimeMillis() - pictureTakenTime)>pictureDelayMS)
        {
            Size boardSize = new Size(this.numCornersHor, this.numCornersVer);
            boolean found = Calib3d.findChessboardCorners(grayImage, boardSize, imageCorners,
                    Calib3d.CALIB_CB_FAST_CHECK);

            pictureTakenTime = System.currentTimeMillis();

            if (found)
            {
                //Refine the corners with sub-pixel accuracy for better calibration.
                TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);
                Imgproc.cornerSubPix(grayImage, imageCorners, new Size(11, 11), new Size(-1, -1), term);

                //Draw chessboard corners
                Calib3d.drawChessboardCorners(inputFrame, boardSize, imageCorners, found);

                //Add image corners and target grid to list
                imagePoints.add(imageCorners);
                objectPoints.add(obj);

                imageCorners = new MatOfPoint2f();
                successes++;
            }
            //Calibrate camera.
        }else if(!isCalibrated && successes == boardsNumber){
            //TODO(håkon) save configuration, use (r/t)vecs?
            List<Mat> rvecs = new ArrayList<>();
            List<Mat> tvecs = new ArrayList<>();
            //TODO(håkon) Save configuration.
            Calib3d.calibrateCamera(objectPoints, imagePoints, grayImage.size(), intrinsic, distCoeffs, rvecs, tvecs);

            //Get new camera matrix
            newCameraMatrix = Calib3d.getOptimalNewCameraMatrix(intrinsic,distCoeffs,grayImage.size(),1,grayImage.size(),newROI,false);
            this.isCalibrated = true;
        }else if(!isCalibrated){
            Imgproc.putText(inputFrame,"Not calibrated: Image " + successes + "/" + boardsNumber, new Point(100,100), Core.FONT_HERSHEY_PLAIN,5,new Scalar(255,0,0),10);
        }
        return false;
    }
}

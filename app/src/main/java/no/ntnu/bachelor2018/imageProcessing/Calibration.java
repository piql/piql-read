package no.ntnu.bachelor2018.imageProcessing;

import android.app.Activity;
import android.content.ContextWrapper;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import no.ntnu.bachelor2018.filmreader.MainActivity;

import static android.content.ContentValues.TAG;

/**
 * Created by hcon on 26.02.18.
 */

public class Calibration{
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
    private boolean isCalibrated;
    private final int pictureDelayMS = 1000;

    /**
     * Calibrates camera using the input image or undistorts the input image if calibrated.
     */
    public Calibration(){
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

        newCameraMatrix = null;

        //Distance coefficients
        distCoeffs = new Mat();

        //Sucsessfully captured images for configuration
        successes = 0;

        //Region of interest after undistortion
        newROI = new Rect();

        //Counter for taking a new image
        pictureTakenTime = 0;

        isCalibrated = false;

        //Amount of internal corners in the checkerboard pattern TODO(håkon) parameterize this.
        numCornersHor = numCornersVer = 15;

        //Number of pictures required to configure the camera. More pictures = better calibration
        // TODO(håkon) parameterize this.
        boardsNumber = 20;

        int numSquares = numCornersHor * numCornersVer;

        //Initialise target grid points for calibration
        for (int i = 0; i < numSquares; i++)
            obj.push_back(new MatOfPoint3f(new Point3(i / numCornersHor, i % numCornersVer, 0.0f)));
        isCalibrated = loadConfig();

    }

    private void calibSize(Mat image){
        if(image.width() != this.width || image.height() != this.height){
            this.width = image.width();
            this.height = image.height();

            //Undistorted grayscale image
            undistorted = new Mat(width,height,CvType.CV_8UC1);
        }
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
        calibSize(inputFrame);
        //Undistort image if the camera is already calibrated
        if(isCalibrated){
            if(newCameraMatrix == null){
                newCameraMatrix = Calib3d.getOptimalNewCameraMatrix(intrinsic,distCoeffs,inputFrame.size(),1,inputFrame.size(),newROI,false);
            }
            Imgproc.undistort(inputFrame,undistorted, newCameraMatrix, distCoeffs);
            undistorted.copyTo(inputFrame);
            return true;
        }
        //Take picture for calibration if timer has passed and not done.
        else if (successes < this.boardsNumber && (System.currentTimeMillis() - pictureTakenTime)>pictureDelayMS)
        {
            Size boardSize = new Size(this.numCornersHor, this.numCornersVer);
            boolean found = Calib3d.findChessboardCorners(inputFrame, boardSize, imageCorners,
                    Calib3d.CALIB_CB_FAST_CHECK);

            pictureTakenTime = System.currentTimeMillis();

            if (found)
            {
                //Refine the corners with sub-pixel accuracy for better calibration.
                TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);
                Imgproc.cornerSubPix(inputFrame, imageCorners, new Size(11, 11), new Size(-1, -1), term);

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
            Calib3d.calibrateCamera(objectPoints, imagePoints, inputFrame.size(), intrinsic, distCoeffs, rvecs, tvecs);
            saveConfig();

            //Get new camera matrix
            newCameraMatrix = Calib3d.getOptimalNewCameraMatrix(intrinsic,distCoeffs,inputFrame.size(),1,inputFrame.size(),newROI,false);

            this.isCalibrated = true;
        }else if(!isCalibrated){
            Imgproc.putText(inputFrame,"Not calibrated: Image " + successes + "/" + boardsNumber, new Point(100,100), Core.FONT_HERSHEY_PLAIN,5,new Scalar(255,0,0),10);
        }
        return false;
    }

    private boolean loadConfig(){
        try {
            FileInputStream fstream = new FileInputStream(configFile());
            ObjectInputStream ostream = new ObjectInputStream(fstream);
            this.intrinsic = arrayToMat((double[][])ostream.readObject());
            this.distCoeffs = arrayToMat((double[][])ostream.readObject());
            ostream.close();
            fstream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private void saveConfig(){
        try {
            FileOutputStream fstream = new FileOutputStream(configFile());
            ObjectOutputStream ostream = new ObjectOutputStream(fstream);

            ostream.writeObject(matToArray(this.intrinsic));
            ostream.writeObject(matToArray(this.distCoeffs));
            ostream.close();
            fstream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private double[][] matToArray(Mat input){
        int cols = input.cols();
        int rows = input.rows();
        double matArray[][] = new double[rows][cols];
        for(int i = 0; i<rows; i++){
            for(int a = 0; a<cols; a++){

                matArray[i][a] = input.get(i,a)[0];
            }
        }
        return matArray;
    }

    private Mat arrayToMat(double[][] input){
        int rows = input.length;
        int cols = input[0].length;
        Mat mat = new Mat(rows,cols,CvType.CV_64F);

        for(int i = 0; i<rows; i++){
            for(int a = 0; a<cols; a++){
                mat.put(i,a,input[i][a]);
            }
        }
        return mat;

    }

    private File configFile(){
        ContextWrapper cw = new ContextWrapper(MainActivity.context);
        File dir =  cw.getDir("config", MainActivity.context.MODE_PRIVATE);

        return new File(dir, "config.save");

    }
}

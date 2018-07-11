package no.ntnu.bachelor2018.previewImageProcessing;

import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
import org.opencv.imgcodecs.Imgcodecs;
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

import no.ntnu.bachelor2018.filmreader.ImageBufferManager;
import no.ntnu.bachelor2018.filmreader.MainActivity;

/**
 * Calibration class calibrates an image to correct for distortion that appears
 * when using an extra lens. The configuration is saved locally and is loaded upon start.
 */
public class Calibration {

    private static final String TAG = "Calibration";
    private static final int pictureDelayMS = 1000;
    private static int height, width;
    private static List<Mat> imagePoints;
    private static List<Mat> objectPoints;
    private static MatOfPoint3f obj;
    private static MatOfPoint2f imageCorners;
    private static int boardsNumber;
    private static int numCornersHor;
    private static int numCornersVer;
    private static int successes;
    private static long pictureTakenTime;
    private static Rect newROI;
    private static Mat intrinsic;
    private static Mat newCameraMatrix;
    private static Mat distCoeffs;
    private static Mat rectMap1, rectMap2;
    private static SharedPreferences prefs;
    private static boolean isCalibrated;

    /**
     * Calibrates camera using the input image or undistorts the input image if calibrated.
     */
    public Calibration() {

    }

    private static synchronized void init() {
        prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
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
        rectMap1 = null;
        rectMap2 = null;

        //Distance coefficients
        distCoeffs = new Mat();

        //Sucsessfully captured images for configuration
        successes = 0;

        //Region of interest after undistortion
        newROI = new Rect();

        //Counter for taking a new image
        pictureTakenTime = 0;

        isCalibrated = false;

        //Amount of internal corners in the checkerboard pattern
        int defCornerValue = 15;
        String tempNumCorners = prefs.getString("calib_size", String.valueOf(defCornerValue));
        if (tempNumCorners.equals("") || tempNumCorners.equals("0")) {
            numCornersHor = numCornersVer = defCornerValue;
        } else {
            numCornersHor = numCornersVer = Integer.valueOf(tempNumCorners);
        }

        //Number of pictures required to configure the camera. More pictures = better calibration
        int defNumValue = 20;
        String tempBoardsNumber = prefs.getString("calib_num", String.valueOf(defNumValue));
        if (tempBoardsNumber.equals("") || tempBoardsNumber.equals("0")) {
            boardsNumber = defNumValue;
        } else {
            boardsNumber = Integer.parseInt(tempBoardsNumber);
        }

        int numSquares = numCornersHor * numCornersVer;

        //Initialise target grid points for calibration
        for (int i = 0; i < numSquares; i++) {
            obj.push_back(new MatOfPoint3f(new Point3(i / numCornersHor, i % numCornersVer, 0.0f)));
        }

        try {
            isCalibrated = loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "init: Failed to load config");
        }
    }

    /**
     * Used to adjust image size dependent variables.
     *
     * @param image The image to recalibrate the size of
     */
    private static void calibSize(Mat image) {
        if (image.width() != width || image.height() != height) {
            // If the resolution was zero, do not delete the saved calibration.
            boolean firstFrame = (width == 0 && height == 0);
            width = image.width();
            height = image.height();
            if(!firstFrame){
                deleteCalibration();
            }
        }
    }


    /**
     * Calibrates camera or undistorts image using input frame. If the calibration is not
     * set up, the method will use the frame to calibrate. If the calibration is set up, the
     * method will apply the calibraiton to the frame.
     * NB:Function inspired by http://answers.opencv.org/question/179214/using-stereobm-in-java-on-android-some-results/
     *
     * @param inputFrame The input {@link Mat} to calibrate
     * @return True if the calibration is set, false otherwise
     */
    public static synchronized boolean calibration(Mat inputFrame) {
        calibSize(inputFrame);

        // Initialize variables if not already done.
        if (objectPoints == null || imagePoints == null) {
            init();
        }

        // Undistort image if the camera is already calibrated
        if (isCalibrated) {
            //Get buffer
            Mat undistorted = new Mat(width,height,CvType.CV_8UC1);

            if (newCameraMatrix == null) {
                //Rectification maps for saving undistortion transformation
                newCameraMatrix = Calib3d.getOptimalNewCameraMatrix(intrinsic, distCoeffs, inputFrame.size(), 0, inputFrame.size(), newROI, false);

                //Initialize undistortion mapping. Better then undistort as it only maps once.
                Log.d(TAG, "Camera matrix configured");
            }
            if (rectMap1 == null || rectMap2 == null) {
                rectMap1 = new Mat(width, height, CvType.CV_32FC2);
                rectMap2 = new Mat(width, height, CvType.CV_32FC1);
                Imgproc.initUndistortRectifyMap(intrinsic, distCoeffs, new Mat(), newCameraMatrix, new Size(width, height), CvType.CV_32FC(1), rectMap1, rectMap2);
            }
            //undistorter.undistort(inputFrame);
            Imgproc.remap(inputFrame, undistorted, rectMap1, rectMap2, Imgproc.INTER_LINEAR);

            //Copy undistorted image to return and set unused.
            undistorted.copyTo(inputFrame);
            undistorted.release();
            undistorted = null;


            return true;
        }

        // Take picture for calibration if timer has passed and not done.
        else if (successes < boardsNumber &&
                (System.currentTimeMillis() - pictureTakenTime) > pictureDelayMS) {
            Size boardSize = new Size(numCornersHor, numCornersVer);
            boolean found = Calib3d.findChessboardCorners(inputFrame, boardSize, imageCorners,
                    Calib3d.CALIB_CB_FAST_CHECK);

            pictureTakenTime = System.currentTimeMillis();

            if (found) {
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
        }

        // If we have enough pictures to calibrate with, we apply the config and save it
        else if (!isCalibrated && successes >= boardsNumber) {
            //Not used. Contains radial and tangential deviation in each sample.
            //Required by calibrateCamera.
            List<Mat> rvecs = new ArrayList<>();
            List<Mat> tvecs = new ArrayList<>();

            Calib3d.calibrateCamera(objectPoints, imagePoints, inputFrame.size(), intrinsic, distCoeffs, rvecs, tvecs);
            try {
                saveConfig();
            } catch (IOException e) {
                Log.d(TAG, "calibration: WARNING. Failed to save calibration.");
                e.printStackTrace();
            }

            //Get new camera matrix
            newCameraMatrix = Calib3d.getOptimalNewCameraMatrix(intrinsic, distCoeffs, inputFrame.size(), 0, inputFrame.size(), newROI, false);
            isCalibrated = true;
        } else if (!isCalibrated) {
            Imgproc.putText(inputFrame, "Not calibrated: " + successes + "/" + boardsNumber, new Point(100, 100), Core.FONT_HERSHEY_PLAIN, 5, new Scalar(255, 0, 0), 10);
        }

        return false;
    }

    /**
     * Loads the configuration files if exists
     *
     * @return True on success, false otherwise
     */
    private static synchronized boolean loadConfig() throws IOException {
        FileInputStream fstream = null;
        ObjectInputStream ostream = null;
        try {
            // Open the streams
            fstream = new FileInputStream(configFile());
            ostream = new ObjectInputStream(fstream);

            // Read the streams, convert the arrays to Mat object and set the config variables
            intrinsic = arrayToMat((double[][]) ostream.readObject());
            distCoeffs = arrayToMat((double[][]) ostream.readObject());
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Could not find config");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } finally {
            if(fstream != null)
                fstream.close();
            if(ostream != null)
                ostream.close();
        }

        return true;
    }

    /**
     * Saves the two {@link Mat} containing the distortion configuration locally to the device
     */
    private static synchronized void saveConfig() throws IOException {
        FileOutputStream fstream = null;
        ObjectOutputStream ostream = null;
        try {
            // Open up an objectstream
            fstream = new FileOutputStream(configFile());
            ostream = new ObjectOutputStream(fstream);

            // Convert the Mat objects to arrays
            ostream.writeObject(matToArray(intrinsic));
            ostream.writeObject(matToArray(distCoeffs));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fstream != null)
                fstream.close();
            if(ostream != null)
                ostream.close();
        }

    }

    /**
     * Converts a {@link Mat} to a double array since {@link Mat} is not serializable
     *
     * @param input The input {@link Mat} to convert
     * @return A 2 dimensional double array containing the {@link Mat} data
     */
    private static double[][] matToArray(Mat input) {
        int cols = input.cols();
        int rows = input.rows();
        double matArray[][] = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int a = 0; a < cols; a++) {

                matArray[i][a] = input.get(i, a)[0];
            }
        }
        return matArray;
    }

    /**
     * Converts a 2 dimentional double array to {@link Mat}
     *
     * @param input The 2D double array to convert
     * @return A {@link Mat} containing the data stored in the array
     */
    private static Mat arrayToMat(double[][] input) {
        int rows = input.length;
        int cols = input[0].length;
        Mat mat = new Mat(rows, cols, CvType.CV_64F);

        for (int i = 0; i < rows; i++) {
            for (int a = 0; a < cols; a++) {
                mat.put(i, a, input[i][a]);
            }
        }
        return mat;
    }

    /**
     * Gets a {@link File} object with the path to the config
     *
     * @return A {@link File} with the path to the config
     */
    public static File configFile() {
        ContextWrapper cw = new ContextWrapper(MainActivity.context);
        File dir = cw.getDir("config", MainActivity.context.MODE_PRIVATE);

        return new File(dir, "config.save");
    }

    /**
     * Deletes calibration file.
     */
    public static synchronized void deleteCalibration() {
        File configLoc = Calibration.configFile();


        if (configLoc.exists()) {
            configLoc.delete();
        }
        init();

        Log.d(TAG, "config deleted");

    }
}
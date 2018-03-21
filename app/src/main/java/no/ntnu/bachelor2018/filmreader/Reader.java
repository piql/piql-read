package no.ntnu.bachelor2018.filmreader;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.List;

import no.ntnu.bachelor2018.previewImageProcessing.Calibration;
import no.ntnu.bachelor2018.previewImageProcessing.FinalProcessing;
import no.ntnu.bachelor2018.previewImageProcessing.FrameFinder;
import no.ntnu.bachelor2018.previewImageProcessing.MarkerDetection;
import no.ntnu.bachelor2018.previewImageProcessing.Overlay;

/**
 * The Reader class is for the main processing on an image, it includes calibrating,
 * finding corners and preparing it for decoding.
 */
public class Reader {

    private final String TAG = this.getClass().getSimpleName();

    private FrameFinder finder;
    private MarkerDetection markDetect;
    private FinalProcessing finalProc;
    private Calibration calib;
    private Mat processedImage;
    private SharedPreferences prefs;
    private Rect newROI, defROI;
    private Overlay overlay;
    private int width, height;
    private boolean toCalibrate;

    //Outer frame corners and inner corners for marker finding mask
    private List<Point> corners;

    public Reader(){
        //TODO: Håkon add camera config parameter constructor

        prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
        toCalibrate = prefs.getBoolean("pref_cal", true);
        finder = new FrameFinder();
        markDetect = new MarkerDetection();
        calib = new Calibration();
        newROI = null;
        overlay = new Overlay();
        finalProc = new FinalProcessing();
    }

    /**
     * Used to adjust image size dependent variables.
     * @param image the image to calibrate
     */
    private void calibSize(Mat image){
        if(image.width() != this.width || image.height() != this.height){
            this.width = image.width();
            this.height = image.height();

        }
    }

     /**
     * Main loop process that processes one frame
     *
     * @param inputImage camera image frame
     */
    public Mat processFrame(Mat inputImage){
        calibSize(inputImage);

        // If the calibration preference is set to true (default)
        if(toCalibrate) {

	        //If calibration succeeded and we have an undistorted image
	        if (calib.calibration(inputImage)) {

		        //Adjust ROI TODO consider finding image corners
		        if (newROI == null) {
			        newROI = calib.getNewROI();
			        finder.setROI(newROI, inputImage);
		        }

		        //Find and draw corners
		        corners = finder.cornerFinder(inputImage);

		        overlay.addPolyLine(corners);
		        //markDetect.findMarkers(threshImg,inputImage,corners);

		        processedImage = finalProc.finalizeImage(inputImage, corners);
		        if (processedImage != null) {
			        return processedImage;
		        }

		        //Draw overlay as the last thing(to not interfere with detection and other processing
		        overlay.drawAndClear(inputImage);
	        }
	        // If the camera is not calibrated the calibration funciton will calibrate

        } else {
        	// If the calibration is set to false
	        // TODO: Check what happens when you calibrate and turn it off without deleting the config

	        // Create an ROI over the whole screen
        	defROI = new Rect(0, 0, inputImage.width(), inputImage.height());
	        finder.setROI(defROI, inputImage);

	        //Find and draw corners
	        corners = finder.cornerFinder(inputImage);

	        overlay.addPolyLine(corners);
	        //markDetect.findMarkers(threshImg,inputImage,corners);

	        processedImage = finalProc.finalizeImage(inputImage, corners);
	        if (processedImage != null) {
		        return processedImage;
	        }

	        //Draw overlay as the last thing(to not interfere with detection and other processing
	        overlay.drawAndClear(inputImage);
        }

	    return inputImage;
    }

}

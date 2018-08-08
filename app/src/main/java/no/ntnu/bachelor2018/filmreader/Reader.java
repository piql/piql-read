package no.ntnu.bachelor2018.filmreader;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.List;

import no.ntnu.bachelor2018.previewImageProcessing.Calibration;
import no.ntnu.bachelor2018.previewImageProcessing.FinalProcessing;
import no.ntnu.bachelor2018.previewImageProcessing.FrameFinder;
import no.ntnu.bachelor2018.previewImageProcessing.GeneralImgproc;
import no.ntnu.bachelor2018.previewImageProcessing.Overlay;

/**
 * The Reader class is for the main processing on an image, it includes calibrating,
 * finding corners and preparing it for decoding.
 */
public class Reader {

    private static SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
    private int width, height;
    private boolean toCalibrate;
    private FrameFinder finder;
    private FinalProcessing finalProc;
    private Mat processedImage;
    private Overlay overlay;
    //Outer frame corners and inner corners for marker finding mask
    private List<Point> corners;

    public Reader() {
        toCalibrate = prefs.getBoolean("pref_cal", true);
        finder = new FrameFinder();
        //newROI = null;
        overlay = new Overlay();
        finalProc = new FinalProcessing();
    }

    /**
     * Used to adjust image size dependent variables.
     *
     * @param image the image to calibrate
     */
    private synchronized void calibSize(Mat image) {
        if (image.width() != width || image.height() != height) {
            width = image.width();
            height = image.height();
        }
    }

    /**
     * Main loop process that processes one frame
     *
     * @param inputImage camera image frame
     */
    public synchronized Mat processFrame(Mat inputImage) {
        calibSize(inputImage);

        // If the calibration preference is set to true (default)

        //If calibration succeeded and we have an undistorted image
        if (!toCalibrate || Calibration.calibration(inputImage)) {
            //Reset overlay
            overlay = new Overlay();
            //setROI(inputImage);

            //Find corners
            corners = finder.cornerFinder(inputImage, overlay);

            //Final processing
            processedImage = finalProc.finalizeImage(inputImage, corners, overlay);

            corners.clear();

            //Draw and return for viewing if selected and successful
            if (processedImage != null && Preferences.isPreviewType(GeneralImgproc.PreviewType.PROCESSED)) {
                overlay.drawAndClear(processedImage);
                return processedImage;
            }

            //Draw overlay as the last thing(to not interfere with detection and other processing)
            return overlay.drawAndClear(inputImage);
        }



        return inputImage;
    }

}

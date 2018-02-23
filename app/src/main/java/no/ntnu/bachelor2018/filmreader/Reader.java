package no.ntnu.bachelor2018.filmreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


import java.util.List;

import no.ntnu.bachelor2018.imageProcessing.BgCamera;
import no.ntnu.bachelor2018.imageProcessing.FrameFinder;
import no.ntnu.bachelor2018.imageProcessing.MarkerDetection;

/**
 * Created by hcon on 13.02.18.
 */

public class Reader {

    private final String TAG = this.getClass().getSimpleName();

    private FrameFinder finder;
    private MarkerDetection markDetect;
    private int width, height;
    private MatOfPoint pointMat;
    private Mat grayImg, threshImg, mask;
    private List<Point> corners;
    private SharedPreferences prefs;
    private BgCamera camera;

    public Reader(int width, int height, Context context){
        //TODO: Håkon add camera config parameter constructor
        this.width = width;
        this.height = height;

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        camera = new BgCamera(context);
        camera.takePicture();
        finder = new FrameFinder(width, height, prefs);
        markDetect = new MarkerDetection(width,height);
        grayImg = new Mat(height, width, CvType.CV_8UC1);
        threshImg = new Mat();
        mask = new Mat();
    }

     /**
     * Main loop process that processes the image
     *
     * @param inputImage camera image frame
     */
    public Mat processFrame(Mat inputImage){
        Imgproc.cvtColor(inputImage, grayImg, Imgproc.COLOR_BGR2GRAY);
        //Apply adaptive threshold
        Imgproc.adaptiveThreshold(grayImg,threshImg,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,301,8);

        //Find and draw corners
        corners = finder.cornerFinder(threshImg);
        for (int i = 0; i<corners.size(); i++){
            Imgproc.line(inputImage,corners.get(i),corners.get((i + 1) %corners.size()),new Scalar(255,255,255));
        }

        //TEST: try to fill poly
        pointMat = new MatOfPoint();
        pointMat.fromList(corners);
        if(corners.size() > 2) {
            Imgproc.fillConvexPoly(inputImage, pointMat, new Scalar(150, 0, 255));
        }

        //Find distance between corners for mask

        //markDetect.findMarkers(threshImg);
        return inputImage;
    }
}

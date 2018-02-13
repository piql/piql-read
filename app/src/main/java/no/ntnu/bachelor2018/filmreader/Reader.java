package no.ntnu.bachelor2018.filmreader;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import no.ntnu.bachelor2018.imageProcessing.FrameFinder;

/**
 * Created by hcon on 13.02.18.
 */

public class Reader {

    private FrameFinder finder;
    private int width, height;
    private Mat grayImg;

    public Reader(int width, int height){
        //TODO: Håkon add camera config parameter constructor
        finder = new FrameFinder();
        this.width = width;
        this.height = height;
        grayImg = new Mat(height, width, CvType.CV_8UC1);

    }

    /**
     * Main loop process that processes the image
     * @param inputImage camera image frame
     */
    public Mat processFrame(Mat inputImage){
        Imgproc.cvtColor(inputImage,grayImg,Imgproc.COLOR_BGR2GRAY);

        return inputImage;
    }
}

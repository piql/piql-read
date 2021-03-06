package no.ntnu.bachelor2018.previewImageProcessing;

/**
 * Created by hcon on 12.03.18.
 */

import android.app.Activity;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Vector;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;
import no.ntnu.bachelor2018.filmreader.MainActivity;
import static android.content.ContentValues.TAG;


/**
 * Class is used to draw overlay onto an image.
 * Used to draw overlay after the image is processed.
 */
public class Overlay {
    // used to show an image that is not done processing.
    private static final int FRAMESAMPLES = 10;                  //frame count to average fps over
    private static long[] fpsTimes = new long[FRAMESAMPLES];    //time samples
    private static int currentFrame = 0;                        //current frame sample index
    private List<TextOverlay> text;             // List of text(s) to draw on the overlay
    private List<PolyLine> lines;           // List of lines to draw on the overlay
    private List<RectDraw> rects;           // List of rectangles to draw on the overlay
    private Mat imageOverride;              // Image override to display a custom image.
    private TextView textView;
    private Activity activity;



    public Overlay() {
        text = new Vector<>();
        lines = new Vector<>();
        rects = new Vector<>();
        imageOverride = null;
        activity = (Activity)MainActivity.context;
        textView = activity.findViewById(R.id.processingText);
    }

    /**
     * Used to retrieve fps and update with new frame timing.
     *
     * @return Average fps over a number of samples
     */
    private static String fpsNewFrame() {
        fpsTimes[currentFrame] = System.currentTimeMillis();
        DecimalFormat format = new DecimalFormat("#.##");
        format.setRoundingMode(RoundingMode.CEILING);

        if (fpsTimes[FRAMESAMPLES - 1] != 0) {    //Get average fps over a number of samples
            double avg = 0;
            //Calculate frame delay sum in MS
            for (int i = 0; i < FRAMESAMPLES - 1; i++) {
                avg += fpsTimes[(i + currentFrame + 2) % FRAMESAMPLES] - fpsTimes[(i + currentFrame + 1) % FRAMESAMPLES];
            }
            if(avg == 0){
                return "0";
            }
            avg /= FRAMESAMPLES - 1;         //divide by sample size -1(average delay in MS)
            avg /= 1000;                    //convert to seconds
            avg = 1 / avg;                 //convert to frames per second instead of avg delay
            currentFrame = (currentFrame + 1) % FRAMESAMPLES;
            return format.format(avg);
        }
        currentFrame = (currentFrame + 1) % FRAMESAMPLES;
        return "-1";

    }

    /**
     * Add new text for overlay drawing.
     *
     * @param textInput
     * @param pos
     */
    public void addText(String textInput, Point pos) {
        text.add(new TextOverlay(textInput, pos));
    }

    /**
     * Add new poly line for overlay drawing.
     *
     * @param pts
     */
    public void addPolyLine(List<Point> pts) {
        lines.add(new PolyLine(pts));
    }

    /**
     * Add new rectange for overlay drawing
     *
     * @param rect
     */
    public void addRect(Rect rect) {
        if (rect != null) {
            rects.add(new RectDraw(rect));
        }
    }

    /**
     * Used to override the image that should be drawn.
     * General
     *
     * @param override
     */
    public void overrideDisplayImage(Mat override) {
        imageOverride = override.clone();
    }

    /**
     * Draws overlay onto image and clears
     *
     * @param image
     */
    public Mat drawAndClear(Mat image) {
        //Set override to the input image if
        //it is not already set.

        if (imageOverride == null) {
            imageOverride = image;
        }

        addText("FPS: " + fpsNewFrame(), new Point(100, 200));
        /*
        for (PolyLine pl : lines) {
            pl.drawPolyLine(imageOverride);
        }*/
        for (TextOverlay ol : text) {
            ol.drawText(imageOverride);
        }/*
        for (RectDraw re : rects) {
            re.drawRect(imageOverride);
        }*/
        lines.clear();
        text.clear();
        rects.clear();
        return imageOverride;
    }

    /**
     * Used to draw text overlay.
     */
    public class TextOverlay {
        private final Scalar red = new Scalar(255, 0, 0);
        private String text;
        private Point pos;

        public TextOverlay(String text, Point pos) {
            this.text = text;
            this.pos = pos;
        }

        private void drawText(Mat inputImage) {
            //Imgproc.putText(inputImage, this.text, this.pos, Core.FONT_HERSHEY_PLAIN, 5, red, 10);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (textView == null) {
                        Log.e(TAG, "preview is null");
                    } else {
                        textView.setText(text);
                    }
                }
            });
        }
    }

    /**
     * Is used to draw poly line between a set of points
     */
    public class PolyLine {
        //Points that form the line
        private List<Point> pts;

        public PolyLine(List<Point> pts) {
            this.pts = pts;
        }

        private void drawPolyLine(Mat image) {
            for (int i = 0; i < pts.size(); i++) {
                //Draw lines between the points
                Imgproc.line(image, pts.get(i), pts.get((i + 1) % pts.size()), new Scalar(255, 255, 255), 5);
            }
        }
    }

    /**
     * Is used to draw rectangles
     */
    public class RectDraw {
        //Points that form the line
        private Rect rect;

        public RectDraw(Rect rect) {
            this.rect = rect;
        }

        private void drawRect(Mat image) {
            Imgproc.rectangle(image, rect.br(), rect.tl(), new Scalar(255, 255, 255), 3);
        }
    }
}
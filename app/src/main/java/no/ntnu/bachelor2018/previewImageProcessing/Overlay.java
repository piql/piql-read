package no.ntnu.bachelor2018.previewImageProcessing;

/**
 * Created by hcon on 12.03.18.
 */

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.Vector;

/**
 * Class is used to draw overlay onto an image.
 * Used to draw overlay after the image is processed.
 */
public class Overlay {
    private List<TextOverlay> text;
    private List<PolyLine> lines;

    public Overlay(){
        text = new Vector<>();
        lines= new Vector<>();
    }

    /**
     * Add new text for overlay drawing.
     * @param textInput
     * @param pos
     */
    public void addText(String textInput, Point pos){
        text.add(new TextOverlay(textInput,pos));
    }

    /**
     * Add new poly line for overlay drawing.
     * @param pts
     */
    public void addPolyLine(List<Point> pts){
        lines.add(new PolyLine(pts));
    }

    /**
     * Draws overlay onto image and clears
     * @param image
     */
    public void drawAndClear(Mat image){
        for(PolyLine pl: lines){
            pl.drawPolyLine(image);
        }
        for(TextOverlay ol: text){
            ol.drawText(image);
        }
        lines.clear();
        text.clear();
    }

    /**
     * Used to draw text overlay.
     */
    public class TextOverlay{
        private String text;
        private final Scalar red = new Scalar(255,0,0);
        private Point pos;
        public TextOverlay(String text, Point pos){
            this.text = text;
            this.pos = pos;
        }
        private void drawText(Mat inputImage){
            Imgproc.putText(inputImage, this.text, this.pos, Core.FONT_HERSHEY_PLAIN,5,red,10);
        }
    }

    /**
     * Is used to draw poly line between a set of points
     */
    public class PolyLine{
        //Points that form the line
        private List<Point> pts;
        public PolyLine(List<Point> pts){
            this.pts = pts;
        }

        private void drawPolyLine(Mat image){
            for(int i = 0; i<pts.size(); i++){
                //Draw lines between the points
                Imgproc.line(image,pts.get(i),pts.get((i+1)%pts.size()),new Scalar(255,255,255),5);
            }
        }
    }
}

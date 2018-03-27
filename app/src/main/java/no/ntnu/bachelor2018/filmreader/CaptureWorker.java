package no.ntnu.bachelor2018.filmreader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

/**
 * Created by hcon on 22.03.18.
 */

public class CaptureWorker {
    private int width = 0, height = 0;
    private Reader reader;
    private Bitmap bitmap;
    private Mat procImage, processedImage;
    private byte[] byteArray;
    private Activity mainActivity;
    private ImageView view;
    private Thread t1;

    public CaptureWorker(Activity activity){
        reader = new Reader();
        mainActivity = activity;
        view = mainActivity.findViewById(R.id.imageView);
    }

    /**
     * Used to adjust image size dependent variables.
     * @param iWidth
     * @param iHeight
     */
    private void calibSize(int iWidth, int iHeight){
        if(iWidth != this.width || iHeight != this.height){
            this.width = iWidth;
            this.height = iHeight;
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            procImage = new Mat(height, width, CvType.CV_8UC1);
            processedImage = new Mat(height, width, CvType.CV_8UC1);
            byteArray = new byte[width*height];

        }
    }

    /**
     * Entry point for capture processing
     * @param imreader
     */
    public void processFrame(ImageReader imreader){
        //Get the image
        if(t1 != null && t1.isAlive()){

            return;
        }

        Image img = imreader.acquireLatestImage();
        calibSize(img.getWidth(), img.getHeight());
        ByteBuffer buffer = img.getPlanes()[0].getBuffer();
        buffer.get(byteArray);
        img.close();

        t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                //Copy the image to an opencv Mat
                procImage.put(0,0,byteArray);
                processedImage = reader.processFrame(procImage);
                showImage(processedImage);
            }
        });
        t1.start();

        //Close image so camera can use the buffer



    }

    private void showImage(Mat processed){
        if(bitmap == null){
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        Utils.matToBitmap(processed, bitmap);
        if(bitmap != null) {
            if(processed.width() != bitmap.getWidth() || processed.height() != bitmap.getHeight()){
                bitmap.setWidth(processed.width());
                bitmap.setHeight(processed.height());
            }
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Scale bitmap and show
                    view.setImageBitmap(bitmap);
                }
            });
        }

    }
}
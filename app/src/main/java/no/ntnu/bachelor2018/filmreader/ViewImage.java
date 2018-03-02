package no.ntnu.bachelor2018.filmreader;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;
import no.ntnu.bachelor2018.imageProcessing.BgCamera;

/**
 * ViewImage class is for showing the contents of a {@link Mat}, this is
 * purely for testing purposes and debugging
 */
public class ViewImage extends AppCompatActivity {

    public final String TAG = this.getClass().getSimpleName();

    public static   Mat tempImg;    // Temporary static variable for transferring big data
    private         Mat img;        // Instanced variable for the big data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_view);
        ImageView imageView = findViewById(R.id.imageView2);

        // On create we store the big data that is set in the static variable to a safer place
        img = BgCamera.getHiresCapture();
        tempImg = null;

        // Change the mat to an image object and show it
        Bitmap bitmap = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bitmap);
        Log.d(TAG, img.size().toString());

        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(bitmap);
    }

}

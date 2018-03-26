package no.ntnu.bachelor2018.filmreader.PiqlLib;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * Created by hcon on 15.03.18.
 */

public class Wrapper {
    public Wrapper(){}
    public static void getFileFromImage(int width, int height, byte[] image){
        process(width,height,image);
        //TESTING:
        Log.d(TAG, "Processing done");
    }
    private static native void process(int width, int height, byte[] image);
}

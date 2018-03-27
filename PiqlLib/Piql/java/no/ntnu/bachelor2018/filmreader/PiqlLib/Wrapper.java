package no.ntnu.bachelor2018.filmreader.PiqlLib;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * Created by hcon on 15.03.18.
 */

public class Wrapper {
    public Wrapper(){}
    public static boolean getFileFromImage(int width, int height, byte[] image){

        Log.d(TAG, "Processing");
        return process(width,height,image);
    }
    private static native boolean process(int width, int height, byte[] image);
}

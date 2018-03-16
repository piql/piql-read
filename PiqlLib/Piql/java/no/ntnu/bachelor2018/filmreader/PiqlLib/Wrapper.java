package no.ntnu.bachelor2018.filmreader.PiqlLib;

/**
 * Created by hcon on 15.03.18.
 */

public class Wrapper {
    static{
        System.loadLibrary("unboxingdata");
    }
    public Wrapper(){}
    public void getFileFromImage(int width, int height, byte[] image){
        process(width,height,image);
    }
    private native void process(int width, int height, byte[] image);
}

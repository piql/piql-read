package no.ntnu.bachelor2018.filmreader;

import android.app.Activity;
import android.app.ActivityManager;
import android.util.Log;

import org.opencv.core.Mat;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * Used to reuse image buffers whenever possible to save memory.
 */
public class ImageBufferManager {
    private static final String TAG = "ImageBufferManager";
    private static List<Mat> matList = new Vector<>();
    private static List<Object> inUseList = new LinkedList<>();
    private static ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
    private static ActivityManager activityManager = (ActivityManager) MainActivity.context.getSystemService(Activity.ACTIVITY_SERVICE);

    /**
     *
     * @param width     width of required image buffer
     * @param height    height of required image buffer
     * @param type      Image buffer type(Cvtype. ...)
     * @return          Reused or new image buffer. Returns null if memory is low.
     */
    public static synchronized Mat getBuffer(int width, int height, int type, int channels, int depth){
        Mat tmpMat;

        Boolean clearedSpace = false;

        for (int i = matList.size() - 1; i >= 0; i--) {
            tmpMat = matList.get(i);

            //Re-usable mat found
            if(tmpMat.width() == width
                    && tmpMat.height() == height
                    && tmpMat.type() == type
                    && tmpMat.channels() == channels
                    && tmpMat.depth() == depth){

                //Move to end of map(LRU)
                matList.add(matList.remove(i));
                return matList.get(matList.size() - 1);
            }
        }

        // Check if app is low on memory.
        // Clear out enough of the least used buffers to make room for new image buffer.
        if(isLowOnMemory()){
            //Accumulated size of the buffers iterated over so far
            int accumulatedSize = 0;


            //Size needed for the new buffer
            int neededSize = width*height*channels*depth;

            //Clear enough space for new buffer using LRU by removing buffers not in use.
            for (int i = 0; i < matList.size() && !clearedSpace; i++) {
                tmpMat = matList.get(i);

                //Skip used image buffers
                if(matInUse(tmpMat)){
                    continue;
                }
                accumulatedSize += tmpMat.width() * tmpMat.height() * tmpMat.depth() * tmpMat.channels();

                // Start freeing if enough memory can be cleared.
                if(neededSize < accumulatedSize){
                    //Remove and release the buffers needed to make place for the new one.
                    for(int a = 0; a<i; a++){
                        tmpMat = matList.get(a);
                        if(!matInUse(tmpMat)){
                            tmpMat = matList.remove(a);
                            tmpMat.release();
                        }
                    }
                    clearedSpace = true;
                }
            }
        }

        // Low on memory and not enough unused buffers.
        if(memoryInfo.lowMemory && !clearedSpace){
            Log.d(TAG, "WARNING: Low memory and not enough unused buffers to clear\n Buffers allocated: " + inUseList.size());
            return null;
        }

        //Add and return new mat.
        matList.add(new Mat(width, height, type));
        inUseList.add(matList.get(matList.size() - 1));
        return matList.get(matList.size() - 1);
    }

    private static synchronized boolean matInUse(Mat mat){
        return inUseList.contains(mat);
    }

    /**
     * Set's mat as unused so it can be re-used or deleted.
     * @param mat
     */
    public static synchronized void setUnused(Mat mat){
        //Effectively removes the actual reuse portion... Was causing memory leak.
        inUseList.remove(mat);
        matList.remove(mat);
        mat.release();
    }

    /**
     * Check if memory is low.
     * @return
     */
    private static Boolean isLowOnMemory(){
        activityManager.getMemoryInfo(memoryInfo);
        return(memoryInfo != null && memoryInfo.lowMemory);
    }
}

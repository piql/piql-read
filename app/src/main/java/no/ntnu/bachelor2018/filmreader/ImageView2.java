package no.ntnu.bachelor2018.filmreader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class ImageView2 extends android.support.v7.widget.AppCompatImageView {
    public ImageView2(Context context) {
        super(context);
    }

    public ImageView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ((BitmapDrawable) getDrawable()).getBitmap().recycle();
    }
}

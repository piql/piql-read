package no.ntnu.bachelor2018.filmreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

public class ViewImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Intent intent = getIntent();
        byte[] bytes = intent.getByteArrayExtra("byte_arr");

        //imageView.setImageBitmap();
    }
}

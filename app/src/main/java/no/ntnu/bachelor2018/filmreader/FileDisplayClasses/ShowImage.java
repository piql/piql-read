package no.ntnu.bachelor2018.filmreader.FileDisplayClasses;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

/**
 * Simple activity for showing an image
 */
public class ShowImage extends AppCompatActivity {

	// Static variable for this one large file that we cant send over Intent
	private static Bitmap image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_image);

		if(image != null) {
			ImageView imageView = findViewById(R.id.fileDisplayImageView);
			imageView.setImageBitmap(image);
		}
	}

	@Override
	protected void onDestroy(){
		image = null;
		super.onDestroy();
	}

	public static Bitmap getImage() {
		return image;
	}

	public static void setImage(Bitmap image) {
		ShowImage.image = image;
	}

}

package no.ntnu.bachelor2018.filmreader.FileDisplayClasses;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toolbar;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

/**
 * Simple activity for showing an image
 */
public class ShowImage extends AppCompatActivity {

	// Static variable for this one large file that we cant send over Intent
	private static Bitmap image;
	private static android.support.v7.widget.Toolbar toolbar;
	private static String title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_image);

		if(image != null) {
			ImageView imageView = findViewById(R.id.fileDisplayImageView);
			imageView.setImageBitmap(image);

			android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar1);
			toolbar.setTitle(title);
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
	public static void setTitle(String title) {
		ShowImage.title = title;
	}

}

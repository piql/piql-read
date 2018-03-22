package no.ntnu.bachelor2018.filmreader.FileDisplayClasses;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

public class ShowText extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_text);

		TextView textView = findViewById(R.id.fileDisplayTextView);
		String text = new String(getIntent().getByteArrayExtra("text"), StandardCharsets.ISO_8859_1);
		textView.setText(text);
	}
}

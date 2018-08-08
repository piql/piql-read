package no.ntnu.bachelor2018.filmreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

public class WelcomeScreen extends AppCompatActivity {

    /**
     * When the user switches back this application after a stop
     */
    @Override
    protected void onStart() {
        super.onStart();

        setContentView(R.layout.activity_welcome_screen);
    }


    public void openMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}

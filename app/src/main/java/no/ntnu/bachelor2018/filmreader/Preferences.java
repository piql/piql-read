package no.ntnu.bachelor2018.filmreader;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

public class Preferences extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
								  WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// We are running Android 3.0+ so we should use a PreferenceFragment
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	}

	/**
	 * Custom {@link android.app.Fragment} class which extends PreferenceFragment
	 */
	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.activity_preferences);
		}
	}

}


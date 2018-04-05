package no.ntnu.bachelor2018.filmreader;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.WindowManager;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;
import no.ntnu.bachelor2018.previewImageProcessing.GeneralImgproc;

public class Preferences extends AppCompatActivity {

	@NonNull
	public static String getPreferenceString(String key, String defValue) {
		return PreferenceManager
				.getDefaultSharedPreferences(MainActivity.context)
				.getString(key, defValue);
	}

	/**
	 * @param type returns the preview type selected as integer.
	 * @return
	 */
	public static boolean isPreviewType(GeneralImgproc.PreviewType type) {
		return (Integer.parseInt(getPreferenceString("prev_type", "4")) == type.ordinal());
	}

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
	 * Loads the preferences set in the xml and adds preferences dynamically
	 */
	public static class SettingsFragment extends PreferenceFragment {
		// Staticly store the sizes from Capture class
		private static Size[] sizes;

		/**
		 * Statically way to add the sizes as this class is only instantiated on activity instantiation
		 *
		 * @param s Array with all the sizes
		 */
		public static void addSizes(Size[] s) {
			sizes = s;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Get preferences from xml
			addPreferencesFromResource(R.xml.activity_preferences);
			addPreferencesFromResource(R.xml.empty_preferences);

			// If the sizes for some reason has not been set we simply return
			if (sizes == null) {
				return;
			}

			// We create a new category and preference
			PreferenceScreen preferenceScreen = getPreferenceScreen();
			PreferenceCategory preferenceCategory = new PreferenceCategory(preferenceScreen.getContext());
			ListPreference listPreference = new ListPreference(preferenceScreen.getContext());

			// Create the entry name and values of the preference
			CharSequence sequenceValues[] = new CharSequence[sizes.length];
			CharSequence sequence[] = new CharSequence[sizes.length];
			for (int i = 0; i < sizes.length; i++) {
				sequence[i] = (sizes[i].getWidth() + "x" + sizes[i].getHeight());
				sequenceValues[i] = String.valueOf(i);
			}

			// Setters for the category
			preferenceCategory.setTitle(getResources().getString(R.string.capture_title));

			// Setters for the preference
			listPreference.setKey("resolution");
			listPreference.setTitle(getResources().getString(R.string.resolution));
			listPreference.setSummary(getResources().getString(R.string.resolution_desc));
			listPreference.setEntries(sequence);
			listPreference.setEntryValues(sequenceValues);
			listPreference.setDefaultValue("0");

			// Add the preference and category to the preference screen
			preferenceScreen.addPreference(preferenceCategory);
			preferenceCategory.addPreference(listPreference);
		}
	}

}


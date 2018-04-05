package no.ntnu.bachelor2018.filmreader;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

		private final String TAG = this.getClass().getSimpleName();
		private final int MIN_CALIB_SIZE = 1;
		private final int MAX_CALIB_SIZE = 50;
		private final int MIN_CALIB_NUM = 1;
		private final int MAX_CALIB_NUM = 50;

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
			PreferenceCategory cameraSettings = new PreferenceCategory(preferenceScreen.getContext());
			ListPreference cameraResolution = new ListPreference(preferenceScreen.getContext());

			// Create the entry name and values of the preference
			CharSequence sequenceValues[] = new CharSequence[sizes.length];
			CharSequence sequence[] = new CharSequence[sizes.length];
			for (int i = 0; i < sizes.length; i++) {
				sequence[i] = (sizes[i].getWidth() + "x" + sizes[i].getHeight());
				sequenceValues[i] = String.valueOf(i);
			}

			// Setters for the category
			cameraSettings.setTitle(getResources().getString(R.string.capture_title));

			// Setters for the preference
			cameraResolution.setKey("resolution");
			cameraResolution.setTitle(getResources().getString(R.string.resolution));
			cameraResolution.setSummary(getResources().getString(R.string.resolution_desc));
			cameraResolution.setEntries(sequence);
			cameraResolution.setEntryValues(sequenceValues);
			cameraResolution.setDefaultValue("0");

			// Add the preference and category to the preference screen
			preferenceScreen.addPreference(cameraSettings);
			cameraSettings.addPreference(cameraResolution);

			// Preference for entering the size of the calibration pattern
				// TODO cursor at end, min and max value (large values can cause extremely long startup time)
			EditTextPreference calibSize = new EditTextPreference(preferenceScreen.getContext());
			calibSize.setKey("calib_size");
			calibSize.setTitle(getResources().getString(R.string.calib_corner_size));
			calibSize.setSummary(getResources().getString(R.string.calib_corner_size_desc));
			calibSize.setDefaultValue(String.valueOf(15));
			calibSize.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
			// calibSize.getEditText().setSelection(calibSize.getEditText().length());
			calibSize.getEditText().addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}

				@Override
				public void afterTextChanged(Editable s) {
					try {
						int value = Integer.parseInt(s.toString());

						if (value > MAX_CALIB_SIZE) {
							value = MAX_CALIB_SIZE;
							s.replace(0, s.length(), String.valueOf(value));
						}

					} catch (NumberFormatException e){
					}
				}
			});
			cameraSettings.addPreference(calibSize);

			// Preference for number of calibration images
				// TODO cursor at end, min and max value (large values can cause extremely long startup time)
			EditTextPreference calibNum = new EditTextPreference(preferenceScreen.getContext());
			calibNum.setKey("calib_num");
			calibNum.setTitle(getResources().getString(R.string.calib_num));
			calibNum.setSummary(getResources().getString(R.string.calib_num_desc));
			calibNum.setDefaultValue(String.valueOf(20));
			calibNum.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
			// calibSize.getEditText().setSelection(calibSize.getEditText().length());
			calibNum.getEditText().addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}

				@Override
				public void afterTextChanged(Editable s) {
					try {
						int value = Integer.parseInt(s.toString());

						if (value > MAX_CALIB_NUM) {
							value = MAX_CALIB_NUM;
							s.replace(0, s.length(), String.valueOf(value));
						}

					} catch (NumberFormatException e){
					}
				}
			});
			cameraSettings.addPreference(calibNum);
		}

		//TODO reset to default

	}

}


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
		private static final String TAG = "SettingsFragment";
		private static Size[] sizes;

		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.activity_preferences);
			addPreferencesFromResource(R.xml.empty_preferences);

			if(sizes == null){
				return;
			}
			PreferenceScreen preferenceScreen = getPreferenceScreen();
			PreferenceCategory preferenceCategory = new PreferenceCategory(preferenceScreen.getContext());
			ListPreference listPreference = new ListPreference(preferenceScreen.getContext());

			CharSequence sequenceValues[] = new CharSequence[sizes.length];
			CharSequence sequence[] = new CharSequence[sizes.length];
			for(int i = 0; i < sizes.length; i++){
				sequence[i] = (sizes[i].getWidth() + "x" + sizes[i].getHeight());
				sequenceValues[i] = String.valueOf(i);
			}

			preferenceCategory.setTitle(getResources().getString(R.string.capture_title));

			listPreference.setKey("resolution");
			listPreference.setTitle(getResources().getString(R.string.resolution));
			listPreference.setSummary(getResources().getString(R.string.resolution_desc));
			listPreference.setEntries(sequence);
			listPreference.setEntryValues(sequenceValues);
			listPreference.setDefaultValue("0");

			preferenceScreen.addPreference(preferenceCategory);
			preferenceCategory.addPreference(listPreference);
		}

		public static void addSizes(Size[] s){
			sizes = s;
		}
	}

	@NonNull
	public static String getPreferenceString(String key, String defValue) {
		return PreferenceManager
				.getDefaultSharedPreferences(MainActivity.context)
				.getString(key, defValue);
	}

	public static boolean isPreviewType(GeneralImgproc.PreviewType type){
		return (Integer.parseInt(getPreferenceString("prev_type", "4")) == type.ordinal());
	}

}


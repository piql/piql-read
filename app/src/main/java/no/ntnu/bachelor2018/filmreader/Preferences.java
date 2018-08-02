package no.ntnu.bachelor2018.filmreader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;
import no.ntnu.bachelor2018.previewImageProcessing.GeneralImgproc;

/**
 * Activity for the preferences. New preferences are added in the xml file, or programatically if
 * they need to be added dynamically (e.g. resolution).
 */
public class Preferences extends AppCompatActivity {

    // Tag for debugging
    private final String TAG = this.getClass().getSimpleName();

    /**
     * Gets a preference string from preferences
     *
     * @param key      The key to get preference from
     * @param defValue The default value if a value could not be found
     * @return The value in preferences or defValue if it was not found
     */
    @NonNull
    public static String getPreferenceString(String key, String defValue) {
        return PreferenceManager
                .getDefaultSharedPreferences(MainActivity.context)
                .getString(key, defValue);
    }

    /**
     * Used in {@link GeneralImgproc} to check the currently selected preview type to a custom one
     *
     * @param type Returns the preview type selected as integer.
     * @return True if the preview type is the selected one in preferences
     */
    public static boolean isPreviewType(GeneralImgproc.PreviewType type) {
        return (Integer.parseInt(getPreferenceString("prev_type", "4")) == type.ordinal());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = findViewById(R.id.prefs_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // Load in the Preference Fragment in the constraintlayout below the toolbar
        getFragmentManager().beginTransaction().replace(R.id.prefs_constraintlayout, new SettingsFragment()).commit();
    }


    /**
     * Function for when the go back button is pressed in the toolbar
     *
     * @param view not used
     */
    public void goBack(View view) {
        finish();
    }

    /**
     * Function for resetting all the preferences
     *
     * @param view not used
     */
    public void resetPrefs(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // Setting Alert Dialog Title
        alertDialogBuilder.setTitle("Reset preferences");
        // Icon Of Alert Dialog
        // Setting Alert Dialog Message
        alertDialogBuilder.setMessage("Are you sure you want to reset preferences?");
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.commit();
                Log.d(TAG, "Preferences reset");

                // Reload the fragment
                getFragmentManager().beginTransaction().replace(R.id.prefs_constraintlayout, new SettingsFragment()).commit();
            }
        });

        alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Custom {@link android.app.Fragment} class which extends PreferenceFragment
     * Loads the preferences set in the xml and adds preferences dynamically
     */
    public static class SettingsFragment extends PreferenceFragment {
        // Staticly store the sizes from Capture class
        private static Size[] sizes;

        // Tag for debugging
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

            // Adding resolutions dynamically:
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


            int lowestResolution = 0;
            for (int i = 0; i < sizes.length; i++) {
                // If image size smaller then 500x500, stop adding sizes.
                // Added due to unboxing lib bugs on very small resolutions.
                if (sizes[i].getHeight() * sizes[i].getWidth() < 490000) {
                    break;
                }
                lowestResolution = i;
            }

            CharSequence sequenceValues[] = new CharSequence[lowestResolution];
            CharSequence sequence[] = new CharSequence[lowestResolution];

            for (int i = 0; i < lowestResolution; i++) {
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
            cameraResolution.setDefaultValue("6");

            // Add the preference and category to the preference screen
            preferenceScreen.addPreference(cameraSettings);
            cameraSettings.addPreference(cameraResolution);

            // Preference for entering the size of the calibration pattern
            // TODO cursor at end, min and max value improvements
            // (large values can cause extremely long startup time)
            EditTextPreference calibSize = new EditTextPreference(preferenceScreen.getContext());
            calibSize.setKey("calib_size");
            calibSize.setTitle(getResources().getString(R.string.calib_corner_size));
            calibSize.setSummary(getResources().getString(R.string.calib_corner_size_desc));
            calibSize.setDefaultValue(String.valueOf(15));
            calibSize.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            calibSize.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        int value = Integer.parseInt(s.toString());

                        if (value > MAX_CALIB_SIZE) {
                            value = MAX_CALIB_SIZE;
                            s.replace(0, s.length(), String.valueOf(value));
                        }

                    } catch (NumberFormatException e) {
                    }
                }
            });
            cameraSettings.addPreference(calibSize);

            // Preference for number of calibration images
            // TODO cursor at end, min and max value improvements
            // (large values can cause extremely long startup time)
            EditTextPreference calibNum = new EditTextPreference(preferenceScreen.getContext());
            calibNum.setKey("calib_num");
            calibNum.setTitle(getResources().getString(R.string.calib_num));
            calibNum.setSummary(getResources().getString(R.string.calib_num_desc));
            calibNum.setDefaultValue(String.valueOf(20));
            calibNum.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            calibNum.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        int value = Integer.parseInt(s.toString());

                        if (value > MAX_CALIB_NUM) {
                            value = MAX_CALIB_NUM;
                            s.replace(0, s.length(), String.valueOf(value));
                        }

                    } catch (NumberFormatException e) {
                    }
                }
            });
            cameraSettings.addPreference(calibNum);
        }

    }

}


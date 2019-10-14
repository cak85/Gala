package de.cak85.gala.preferences;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import de.cak85.gala.R;

/**
 * Created by ckuster on 19.01.2016.
 */
public class PreferencesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferencesFragment())
                .commit();
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(getString(R.string.settings));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(4f * getResources().getDisplayMetrics().density);

        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        int orientationScreen =Integer.valueOf(sharedPreferences.getString(
                getString(R.string.pref_key_user_interface_orientation),
                String.valueOf(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)));
        //noinspection WrongConstant
        setRequestedOrientation(orientationScreen);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

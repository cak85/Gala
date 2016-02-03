package de.cak85.gala.preferences;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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
        getSupportActionBar().setElevation(8);
    }
}

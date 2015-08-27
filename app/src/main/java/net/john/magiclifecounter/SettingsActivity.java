package net.john.magiclifecounter;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;

/**
 * Created by John on 8/27/2015.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_preferences);
        addPreferencesFromResource(R.xml.preferences);
        //getFragmentManager().beginTransaction().add(R.id.preferences_layout, PrefsFragment.newInstance(), "prefs frag").commit();
    }

    //does this even need a layout outside of preferences.xml?
    /*
    public static class PrefsFragment extends PreferenceFragment {
        public static PrefsFragment newInstance(){
            return new PrefsFragment();
        }

        @Override
        public void onCreate(final Bundle savedInstance){
            super.onCreate(savedInstance);
            this.addPreferencesFromResource(R.xml.preferences);
        }
    }
    */
}

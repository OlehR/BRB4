package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.os.Bundle;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncWaresHelper;
import ua.uz.vopak.brb4.brb4.helpers.WareListHelper;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        new AsyncWaresHelper(new WareListHelper(this)).execute();
    }
}

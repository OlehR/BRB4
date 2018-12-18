package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncWaresHelper;
import ua.uz.vopak.brb4.brb4.helpers.WareListHelper;

public class SettingsActivity extends Activity implements View.OnClickListener {
    Button loadDocsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        new AsyncWaresHelper(new WareListHelper(this)).execute();

        loadDocsData = findViewById(R.id.LoadDocumentsData);
        loadDocsData.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(this,MainActivity.class);
        i.putExtra("isReload","true");
        startActivity(i);
    }
}

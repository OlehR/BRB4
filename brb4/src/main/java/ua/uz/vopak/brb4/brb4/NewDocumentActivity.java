package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import ua.uz.vopak.brb4.brb4.databinding.NewDocumentActivityBinding;
import ua.uz.vopak.brb4.brb4.databinding.SettingsLayoutBinding;
import ua.uz.vopak.brb4.brb4.models.Config;
import ua.uz.vopak.brb4.brb4.models.NewDocModel;
import ua.uz.vopak.brb4.brb4.models.SetingModel;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;

public class NewDocumentActivity extends Activity implements View.OnClickListener {
    Config config = Config.instance();
    Context context;
    NewDocumentActivityBinding binding;
    NewDocModel NDM ;
    public int DocumentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_document_activity);
        binding = DataBindingUtil .setContentView(this, R.layout.new_document_activity);
        context = this;
        Intent i = getIntent();
        DocumentType =  i.getIntExtra("document_type",0);

        NDM = new NewDocModel(this);

        binding.setND(NDM);
    }

    @Override
    public void onClick(View v) {
    }

    public void Exit(String pNumberDoc)
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("NumberDoc",pNumberDoc);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

}

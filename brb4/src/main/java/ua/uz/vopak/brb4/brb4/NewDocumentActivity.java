package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
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
    EditText To;
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

 To = (EditText)findViewById(R.id.ND_Warehouse_To);
        To.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                int i=0;
                if(s.length() >= 3)
                    for (Warehouse el: config.Warehouses ) {
                        if(el.Name.indexOf(s.toString())>0)
                        {
                            NDM.ListWarehouseIdx.set(i);
                            return;
                        }
                        i++;
                    }
            }
        });

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

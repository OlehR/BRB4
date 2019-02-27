package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncInventories;
import ua.uz.vopak.brb4.brb4.helpers.IIncomeRender;
import ua.uz.vopak.brb4.brb4.models.DocWaresModel;
import ua.uz.vopak.brb4.brb4.models.DocWaresModelIncome;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class DocumentWeightActivity extends Activity implements IIncomeRender {
    String number, documentType;
    List<DocWaresModel> InventoryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_weight_layout);
        Intent i = getIntent();
        number = i.getStringExtra("number");
        documentType = i.getStringExtra("document_type");
        new AsyncInventories(GlobalConfig.GetWorker(), this).execute(number,documentType);
    }

    public void RenderTableIncome(final List<DocWaresModelIncome> model, List<DocWaresModel> inventoryModel) {
        final DocumentWeightActivity context = this;
        InventoryItems = inventoryModel;
    }

    public void renderTable(final List<DocWaresModel> model){

    }
}

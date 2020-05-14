package ua.uz.vopak.brb4.brb4;

import android.text.Editable;
import android.util.Log;

import ua.uz.vopak.brb4.brb4.models.WaresItemModel;

public class DocumentScanerEvent {

    public void onCountTextChanged(CharSequence s, int start, int before, int count, WaresItemModel pWIM) {
        WaresItemModel aa= pWIM;
        Log.w("tag", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!onTextChanged " + s);
    }
    public void onTextChange(Editable s)
    {
        Log.d("TAG","&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&New text: " + s.toString());
    }

}

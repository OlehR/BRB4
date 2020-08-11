package ua.uz.vopak.brb4.brb4.models;

import android.graphics.Color;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.lib.models.LabelInfo;

public class DocWaresItemModel implements Cloneable{
    public ObservableBoolean IsView =new ObservableBoolean(false);
    public ObservableField<String> NumberOutInvoice = new ObservableField<String>("");
    public ObservableArrayList<String> ListDate = new ObservableArrayList<>();
    public ObservableInt  ListDateIdx = new ObservableInt(0);
    //GlobalConfig config = GlobalConfig.instance();

    public void SetView() {
        IsView.set(!IsView.get());
    }
}



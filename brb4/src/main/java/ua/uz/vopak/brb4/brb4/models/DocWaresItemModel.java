package ua.uz.vopak.brb4.brb4.models;

import android.graphics.Color;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
    public void DocWaresItemMode()
    {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        for (int i = 0; i <10 ; i++) {
            ListDate.add(formatter.format(date));
            c.add(Calendar.DATE, -1);
            date=c.getTime();
        }
    }
}



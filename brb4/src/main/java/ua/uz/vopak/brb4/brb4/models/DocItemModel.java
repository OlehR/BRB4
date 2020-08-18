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

public class DocItemModel {
    public ObservableBoolean IsView =new ObservableBoolean(false);
    public ObservableField<String> NumberOutInvoice = new ObservableField<String>("");
    public ObservableArrayList<String> ListDate = new ObservableArrayList<>();
    public ObservableInt  ListDateIdx = new ObservableInt(0);
    public int IsClose=1;
    //GlobalConfig config = GlobalConfig.instance();

    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
    public void SetView() {
        IsView.set(!IsView.get());
    }
    public DocItemModel()
    {

        Date date = new Date(System.currentTimeMillis());
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        for (int i = 0; i <10 ; i++) {
            ListDate.add(formatter.format(date));
            c.add(Calendar.DATE, -1);
            date=c.getTime();
        }
    }
    public String GetStrDate()
    {
        return ListDate.get(ListDateIdx.get());
    }
    public Date GetDate()
    {
        Date date = new Date(System.currentTimeMillis());
        String  d= GetStrDate();

        try {
            date=formatter.parse(d);
        }
        catch (Exception ex){}
        return date;
    }

    public void SetDate(Date pDate)
    {
        Date date = new Date(System.currentTimeMillis());
        int d=daysBetween(pDate,date);
        if(d>=0 &&d<10)
            ListDateIdx.set(d);
    }

    public int daysBetween(Date d1, Date d2){
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }
}



package ua.uz.vopak.brb4.brb4.helpers;

import android.database.Cursor;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LogPrice {
    public String BarCode;
    public int Status;
    public Date DTInsert;
    public int IsSend;
    public int ActionType;
    public int PackageNumber;
    public int CodeWares;
    public String Article;
    public int LineNumber;
    public double NumberOfReplenishment;
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public String GetJsonPSU()
    {
        Object[] arr = {BarCode,Status,format.format(DTInsert),PackageNumber,CodeWares};
        Gson gson = new Gson();
        return gson.toJson(arr);
    }//return "[\""+BarCode+"\","+Status+",\""+format.format(DTInsert)+"\","+PackageNumber+","+ CodeWares+"]";}
    public String GetJsonSE(){return "{\"Barcode\":\""+BarCode+"\",\"Code\":\""+CodeWares+"\",\"Status\":"+Status+",\"LineNumber\":"+LineNumber+",\"NumberOfReplenishment\":"+ Double.toString(NumberOfReplenishment)+"}";}
    public LogPrice(){}
    public LogPrice(Cursor pCur)
    {
        Init(pCur);
    }
    String regex = "[0-9]+";
    public boolean IsGoodBarCode()
    {
        if(BarCode!=null && BarCode.trim().length()>2 && BarCode.trim().replace("-","").matches(regex) )
            return true;
        return false;
    }

    void Init( Cursor pCur)
    {
        BarCode= pCur.getString(0);
        Status= pCur.getInt(1);
        String  vDT=pCur.getString(2);
        try {
            DTInsert = format.parse(vDT);
        }catch (Exception e)
        {
            DTInsert= Calendar.getInstance().getTime();
        }
        PackageNumber=pCur.getInt(3);
        IsSend=pCur.getInt(4);
        ActionType=pCur.getInt(5);

        CodeWares=pCur.getInt(6);
        Article=pCur.getString(7);
        LineNumber=pCur.getInt(8);
        NumberOfReplenishment=pCur.getDouble(9);
    }
//,DT_insert,package_number
}
//select bar_code,Status,DT_insert,package_number,is_send,action_type,code_wares,article,Line_Number,Number_Of_Replenishment
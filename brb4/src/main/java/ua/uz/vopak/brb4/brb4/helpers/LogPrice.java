package ua.uz.vopak.brb4.brb4.helpers;

import android.database.Cursor;

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
    public String GetJsonPSU() { return "[\""+BarCode+"\","+Status+",\""+DTInsert.toString()+"\","+PackageNumber+"]";}
    public String GetJsonSE(){return "{\"Barcode\":\""+BarCode+"\",\"Code\":\""+CodeWares+"\",\"Status\":"+Status+",\"LineNumber\":"+LineNumber+",\"NumberOfReplenishment\":"+ Double.toString(NumberOfReplenishment)+"}";}

    public LogPrice(){}
    public LogPrice(Cursor pCur)
    {
        Init(pCur);
    }
    void Init( Cursor pCur)
    {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        BarCode= pCur.getString(0);
        Status= pCur.getInt(1);
        String  vDT=pCur.getString(2);
        try {
            DTInsert = format.parse(vDT);
        }catch (Exception e)
        {
            DTInsert= Calendar.getInstance().getTime();
        }
        IsSend=pCur.getInt(3);
        ActionType=pCur.getInt(4);
        PackageNumber=pCur.getInt(5);
        CodeWares=pCur.getInt(6);
        Article=pCur.getString(7);
        LineNumber=pCur.getInt(8);
        NumberOfReplenishment=pCur.getDouble(9);
    }
//,DT_insert,package_number
}

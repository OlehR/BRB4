package ua.uz.vopak.brb4.brb4.models;

public class DocSetting {
    public int TypeDoc;
    public String NameDoc;
    public boolean IsControlQuantity=false;
    public boolean IsUrlAdd=false;
    public boolean IsAddBarCode = false;
    public boolean IsViewReason =false;

    public DocSetting(int pTypeDoc,String pNameDoc)
    {
        TypeDoc=pTypeDoc;
        NameDoc=pNameDoc;
    }
    public DocSetting(int pTypeDoc,String pNameDoc,boolean pIsControlQuantity,boolean pIsUrlAdd,boolean pIsAddBarCode, boolean pIsViewReason)
    {
        this(pTypeDoc, pNameDoc);
        IsControlQuantity=pIsControlQuantity;
        IsUrlAdd=pIsUrlAdd;
        IsAddBarCode=pIsAddBarCode;
        IsViewReason= pIsViewReason;
    }

}

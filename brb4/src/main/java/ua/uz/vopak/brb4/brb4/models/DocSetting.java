package ua.uz.vopak.brb4.brb4.models;

import ua.uz.vopak.brb4.lib.enums.eTypeControlDoc;

public class DocSetting {
    public int TypeDoc;
    public String NameDoc;

    public eTypeControlDoc TypeControlQuantity =eTypeControlDoc.NoControl;
    // Доступ до документа за додатковим URL
    public boolean IsUrlAdd=false;
    //Шукати документ по штрихкоду
    public boolean IsAddBarCode = false;
    //Показувати причину( бій брак тощо)
    public boolean IsViewReason =false;
    // Показувати планові(фактичні) показники.
    public boolean IsViewPlan =false;
    // Показувати користувача в списку.
    public boolean IsShowUser =true;
    // -1 - стандартна, 1 - 7-23 Ревізія? 2 -7-23 Лоти.
    public int TypeColor =-1;
    // Скільки днів дивитись документи До і Після сьогодня.
    public int DayBefore =2;
    public int DayAfter = 5;
    //Чи показувати реквізити розхідного документа
    public boolean IsViewOut = false;
    // Чи можна повторно зберігати документ
    public boolean IsMultipleSave =true;
    //Передавати в 1с лише проскановані позиції чи і непроскановані з 0 кількістю.
    public boolean IsSaveOnlyScan=true;
    //Дозволяти в документ добавляти позиції з 0 кількістю (для мініревізій)
    public boolean IsAddZero = false;
    // Документ з назвою і штрихкодом в табличній частині
    public boolean IsSimpleDoc = false;
    // Код API для документа (723 -(0-2)
    public int CodeApi=0;

    public DocSetting(int pTypeDoc,String pNameDoc)
    {
        TypeDoc=pTypeDoc;
        NameDoc=pNameDoc;
    }
    public DocSetting(int pTypeDoc,String pNameDoc,eTypeControlDoc pTypeControlQuantity,boolean pIsUrlAdd,boolean pIsAddBarCode, boolean pIsViewReason,boolean pIsViewPlan,boolean pIsShowUser,int pTypeColor,int pDayBefore,int pDayAfter,boolean pIsViewOut,boolean pIsmultipleSave,boolean pIsSaveOnlyScan,boolean pIsAddZero,boolean pIsSimpleDoc,int pCodeApi )
    {
        this(pTypeDoc, pNameDoc);
        TypeControlQuantity =pTypeControlQuantity;
        IsUrlAdd=pIsUrlAdd;
        IsAddBarCode=pIsAddBarCode;
        IsViewReason= pIsViewReason;
        IsViewPlan=pIsViewPlan;
        IsShowUser=pIsShowUser;
        TypeColor=pTypeColor;
        DayBefore=pDayBefore;
        DayAfter=pDayAfter;
        IsViewOut=pIsViewOut;
        IsMultipleSave =pIsmultipleSave;
        IsSaveOnlyScan=pIsSaveOnlyScan;
        IsAddZero=pIsAddZero;
        IsSimpleDoc =pIsSimpleDoc;
        CodeApi = pCodeApi;

    }

}

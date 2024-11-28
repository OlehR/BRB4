package ua.uz.vopak.brb4.brb4.helpers;

import android.text.TextUtils;

import androidx.databinding.ObservableInt;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ua.uz.vopak.brb4.brb4.Connector.Connector;
import ua.uz.vopak.brb4.brb4.models.Doc;
import ua.uz.vopak.brb4.brb4.models.DocModel;
import ua.uz.vopak.brb4.brb4.models.DocSetting;
import ua.uz.vopak.brb4.brb4.models.DocWaresSample;
import ua.uz.vopak.brb4.brb4.models.DocumentModel;
import ua.uz.vopak.brb4.brb4.models.Config;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;
import ua.uz.vopak.brb4.lib.models.ParseBarCode;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eRole;
import ua.uz.vopak.brb4.lib.enums.eTypeControlDoc;
import ua.uz.vopak.brb4.lib.enums.eTypeOrder;
import ua.uz.vopak.brb4.lib.helpers.Utils;
import ua.uz.vopak.brb4.lib.models.Result;

public class Worker {
    protected static final String TAG = "BRB4/Worker";
    Config config = Config.instance();

    //public GetDataHTTP Http = new GetDataHTTP();
    SQLiteAdapter mDbHelper  = config.GetSQLiteAdapter();
    //public Connector c = Connector.instance();
    Gson gson = new Gson();

    public Worker() {}
    public void AddConfigPair(String name, String value) {
        mDbHelper.AddConfigPair(name, value);
    }

    public String GetConfigPair(String name) {
        return mDbHelper.GetConfigPair(name);
    }

    public void LoadStartData() {
        String strCompany = GetConfigPair("Company");
        if (TextUtils.isEmpty(strCompany))
            config.Company = eCompany.NotDefined;
        else
            config.Company = eCompany.fromOrdinal(Integer.valueOf(strCompany));


        config.IsTest = GetConfigPair("IsTest").equals("true");
        config.IsAutoLogin = GetConfigPair("IsAutoLogin").equals("true");
        config.IsLoginCO = GetConfigPair("IsLoginCO").equals("true");

        String LFU= GetConfigPair("LastFullUpdate");
        if(LFU!=null && !LFU.isEmpty())
        {
            try {config.LastFullUpdate  = config.FormatterDate.parse(LFU);}
            catch (Exception ex){}
        }

        config.ApiUrl=GetConfigPair("ApiUrl");
        if(config.ApiUrl==null || config.ApiUrl.isEmpty() )
                config.ApiUrl=(config.Company==eCompany.Sim23 ? "http://176.241.128.13/RetailShop/hs/TSD/":"http://api.spar.uz.ua/znp/");
        config.ApiURLadd=GetConfigPair("ApiUrladd");

        config.Login = GetConfigPair("Login");
        if(config.IsAutoLogin)
            config.Password= GetConfigPair("PassWord");

        try {
            config.CodeWarehouse = Integer.parseInt(GetConfigPair("Warehouse"));
        }
        catch (Exception e){config.CodeWarehouse=0;}

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        Date today = Calendar.getInstance().getTime();
        String todayAsString = df.format(today);

        String var = config.Worker.GetConfigPair("NumberPackege");
        String varNumberPackege = "1";
        if (var.length() > 8 && var.substring(0, 8).equals(todayAsString)) {
            varNumberPackege = var.substring(8);
        } else
            config.Worker.AddConfigPair("NumberPackege", todayAsString + varNumberPackege);

        config.NumberPackege = Integer.valueOf(varNumberPackege);
        config.IsLoadStartData=true;

        config.Reasons= mDbHelper.GetReason();
    }

    //Робота з документами.
    // Завантаження документів в ТЗД (HTTP)
    public Boolean LoadData(int pTypeDoc,String  pNumberDoc,ObservableInt pProgress,boolean pIsClearDoc) {
       // if(1==1)return true;
        Boolean Res=false;
        Date curDate = null;
        try {
            curDate = config.FormatterDate.parse(config.FormatterDate.format(new Date()));
        } catch (Exception ex) { }
        if(pTypeDoc==-2) {
            pTypeDoc=(config.LastFullUpdate == null || !config.LastFullUpdate.equals(curDate) )?-1:0;
        }

        Connector c = Connector.instance();
        Res=c.LoadDocsData(pTypeDoc, pNumberDoc, pProgress, pIsClearDoc);

        if(Res && pTypeDoc==-1 && curDate != null ) {
            config.LastFullUpdate=curDate;
            AddConfigPair("LastFullUpdate", config.FormatterDate.format(curDate));
        }
        return Res;
    }
    // Отримати список документів з БД
    public List<DocumentModel> LoadListDoc( int parTypeDoc, String parBarCode,String pExtInfo ) {
        return   mDbHelper.GetDocumentList(parTypeDoc, parBarCode,pExtInfo);
    }
    // Отримати Товари документа з БД
    public List<WaresItemModel> GetDocWares(int pTypeDoc, String pNumberDoc, int pTypeResult, eTypeOrder pTypeOrder) {
          return mDbHelper.GetDocWares(pTypeDoc, pNumberDoc, pTypeResult,pTypeOrder);
    }
    // Отримати Товар по штрихкоду
    public WaresItemModel GetWaresFromBarcode(int pTypeDoc, String pNumberDoc, String pBarCode,boolean pIsOnlyBarCode) {
        Connector c = Connector.instance();
        boolean IsSimpleDoc=false;
        DocSetting DS=config.GetDocSetting(pTypeDoc);
        if(pTypeDoc>0 && DS!=null)
            IsSimpleDoc = config.GetDocSetting(pTypeDoc).IsSimpleDoc;
        ParseBarCode PBarcode= c.ParsedBarCode(pBarCode,pIsOnlyBarCode&&!IsSimpleDoc);
        //if(pTypeDoc==7 || pTypeDoc==8) //Якщо переміщення ОЗ

        WaresItemModel res=mDbHelper.GetScanData(pTypeDoc, pNumberDoc,PBarcode);// pBarCode, pIsOnlyBarCode,false);

        String outLog="Null";

          if(config.Company== eCompany.Sim23 && (pTypeDoc==7 || pTypeDoc==8)&& PBarcode.Code!=0) { //Якщо ревізія а товар не знайдено
              if( IsSimpleDoc) {
                  res= c.GetWares( PBarcode.Code,IsSimpleDoc);
              }

              DocWaresSample[] DWS = new DocWaresSample[1];
              DWS[0] = new DocWaresSample();
              DWS[0].TypeDoc=pTypeDoc;
              DWS[0].NumberDoc=pNumberDoc;
              DWS[0].OrderDoc=100000+PBarcode.Code;
              DWS[0].CodeWares=PBarcode.Code;
              DWS[0].Quantity=1d;
              DWS[0].QuantityMax=1d;
              DWS[0].Name= (res==null?pBarCode:res.NameWares);
              c.SaveDocWaresSample(DWS,0);
             // res=new WaresItemModel(DWS[0]);
              res.TypeDoc=DWS[0].TypeDoc;
              res.NumberDoc=DWS[0].NumberDoc;
              res.CodeWares=DWS[0].CodeWares;
              res.NameWares= DWS[0].Name;
              res.QuantityMax= DWS[0].QuantityMax;
              res.Coefficient=1;
              res.CodeUnit=config.GetCodeUnitPiece();
              res.BaseCodeUnit=res.CodeUnit;
              res.NameUnit="Шт";
          }
          else
              if(res!=null )
        outLog=res.CodeWares+","+res.QuantityBarCode+","+res.NameWares;

        Utils.WriteLog("i",TAG,"SaveDocWares=>"+String.valueOf(pTypeDoc)+","+pNumberDoc+","+gson.toJson(PBarcode)+
                ",\nres=>"+outLog);
        return res;
    }
    // Збереження товару в БД
    public Result SaveDocWares(int pTypeDoc, String pNumberDoc, int pCodeWares, int pOrderDoc, Double pQuantity, int pCodeReason , Boolean pIsNullable) {
        if (pIsNullable)
            mDbHelper.SetNullableWares(pTypeDoc, pNumberDoc, pCodeWares);

        Result r = mDbHelper.SaveDocWares(pTypeDoc, pNumberDoc, pCodeWares, pOrderDoc, pQuantity, pCodeReason);
        // міняємо стан документа на готується при зміні кількості.
        mDbHelper.UpdateDocState(0, pTypeDoc, pNumberDoc);
        Utils.WriteLog("i",TAG,"SaveDocWares=>"+String.valueOf(pTypeDoc)+","+pNumberDoc+","+pCodeWares+","+pOrderDoc+","+pQuantity+","+pCodeReason+","+pIsNullable+
            ",res=>"+gson.toJson(r));
        return r;
    }
    // Зміна стану документа і відправляємо в 1С
    public Result UpdateDocState(int pState, int pTypeDoc, String pNumberDoc,Date pDateOutInvoice,String pNumberOutInvoice, int pIsClose) {
        DocSetting DS= config.GetDocSetting(pTypeDoc);
        if(DS!=null && !DS.IsMultipleSave)
        {
           int State= mDbHelper.GetStateDoc(pTypeDoc,pNumberDoc);
           if(State>=1)
               return new Result(-2,"Даний документ не можна повторно зберігати!");
        }
        Connector c = Connector.instance();
        mDbHelper.UpdateDocState(pState, pTypeDoc, pNumberDoc);
        List<WaresItemModel> wares = mDbHelper.GetDocWares(pTypeDoc, pNumberDoc,( DS==null || DS.IsSaveOnlyScan?2:1), eTypeOrder.Scan);
        return c.SyncDocsData(pTypeDoc, pNumberDoc, wares,pDateOutInvoice,pNumberOutInvoice,pIsClose);
    }

    public DocModel GetDocOut(int pTypeDoc, String pNumberDoc){
        return mDbHelper.GetDocOut(pTypeDoc,pNumberDoc);
    }

    public void SaveDocOut(Doc pDoc ){
        mDbHelper.SaveDocOut(pDoc);
        Utils.WriteLog("i",TAG,"SaveDocOut"+gson.toJson(pDoc));
    }

    public DocModel GetDoc(int pTypeDoc, String pNumberDoc, int pTypeResult, eTypeOrder pTypeOrder) {
        DocModel result= GetDocOut(pTypeDoc, pNumberDoc);
         result.WaresItem= mDbHelper.GetDocWares(pTypeDoc, pNumberDoc, pTypeResult,pTypeOrder);
        return result;
    }

    public int FindWhIP( Warehouse[] pWarehouses) {
        int res=-1;
        if(pWarehouses==null)
            return res;
        try {
            String Ip = config.cUtils.GetIp();
            if (Ip == null)
                return res;
            String[] IP = Ip.split("\\.");//192.168.1.235
            if (IP.length != 4)
                return res;
            for (int i = 0; i < pWarehouses.length; i++) {
                if(pWarehouses[i].InternalIP==null) continue;
                String[] WhIp = pWarehouses[i].InternalIP.split("\\.");
                if (WhIp.length != 4)
                    continue;
                if (IP[0].equals(WhIp[0]) && IP[1].equals(WhIp[1]) && IP[2].equals(WhIp[2])) {
                    return i;
                }
            }
        } catch (Exception e) {
            Utils.WriteLog("e",TAG, "FindWhIP=>" ,e);
        }
        return res;
    }

    public boolean DelOldData() {
        return  mDbHelper.DelOldData();
    }

    public  Warehouse[] GetWarehouse(){
        Warehouse[] Wh;
        Connector c = Connector.instance();
        Wh=c.LoadWarehouse();
        if(Wh !=null && Wh.length>0)
            mDbHelper.SaveWarehouse(Wh);

        List<Warehouse> lWh = mDbHelper.GetWarehouse();
        Wh = lWh.toArray(new Warehouse[lWh.size()]);
        return Wh;
    }

    public void SetConfig(String pBarCode)    {
        pBarCode=pBarCode.substring(6);
        String[]  par=pBarCode.split(" ");
        for ( String el:par) {
            String[]  El=el.split("=");
            if(El.length==2)
            {
                switch(El[0])
                {
                    case "Company":
                        config.Company= eCompany.fromOrdinal(Integer.valueOf(El[1]));
                        config.Worker.AddConfigPair("Company", Integer.toString(config.Company.getAction()));
                        break;
                    case "Warehouse":
                        config.CodeWarehouse= Integer.valueOf(El[1]);
                        config.Worker.AddConfigPair("Warehouse", Integer.toString(config.CodeWarehouse));
                        break;
                    case "Url":
                        config.ApiUrl= El[1];
                        config.Worker.AddConfigPair("ApiUrl", config.ApiUrl);

                        break;
                    case "URLadd":
                        config.ApiURLadd = El[1];
                        config.Worker.AddConfigPair("ApiUrladd", config.ApiURLadd);
                        break;
                    case "AutoLogin":
                        config.IsAutoLogin = El[1].equals("1");
                        config.Worker.AddConfigPair("IsAutoLogin",config.IsAutoLogin?"true":"false");
                        break;
                    case "Printer":
                        config.TypeUsePrinter = eTypeUsePrinter.fromOrdinal(Integer.valueOf(El[1]));
                        config.Worker.AddConfigPair("connectionPrinterType", config.TypeUsePrinter.GetStrCode());
                }
            }
        }
    };
}

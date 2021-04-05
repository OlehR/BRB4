package ua.uz.vopak.brb4.brb4.helpers;

import android.text.TextUtils;
import android.util.Log;

import androidx.databinding.ObservableInt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ua.uz.vopak.brb4.brb4.Connector.Connector;
import ua.uz.vopak.brb4.brb4.models.Doc;
import ua.uz.vopak.brb4.brb4.models.DocModel;
import ua.uz.vopak.brb4.brb4.models.DocSetting;
import ua.uz.vopak.brb4.brb4.models.DocumentModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eRole;
import ua.uz.vopak.brb4.lib.enums.eTypeControlDoc;
import ua.uz.vopak.brb4.lib.enums.eTypeOrder;
import ua.uz.vopak.brb4.lib.models.Result;



public class Worker {
    protected static final String TAG = "BRB4/Worker";
    GlobalConfig config = GlobalConfig.instance();

    //public GetDataHTTP Http = new GetDataHTTP();
    SQLiteAdapter mDbHelper  = config.GetSQLiteAdapter();
    //public Connector c = Connector.instance();

    public Worker() {}

    public DocSetting[] GenSettingDocs(eCompany pCompany, eRole pProfile) {
        DocSetting[] Setting=null;
        boolean[] Right;
        switch (pCompany)
        {
            case Sim23:
                switch (pProfile) {
                    case Admin:
                        Right = new boolean[]{true,true , true, true};
                        Setting =  new  DocSetting[4];
                        break;
                    case User:
                        Right = new boolean[]{true,true , true, false};
                        Setting =  new  DocSetting[3];
                        break;
                    case  Auditor:
                        Right = new boolean[]{false,true , false, true};
                        Setting =  new  DocSetting[2];
                        break;
                    default:
                        Right = new boolean[]{false,false , false, false};
                        break;
                }
                int step=0;
                if(Right[0])
                    Setting[step++] = new DocSetting(2, "Мініревізія", eTypeControlDoc.Ask, false, false, false, false, false, 1, 1, 0, false, true, false, true);
                if(Right[1])
                    Setting[step++] = new DocSetting(5,"Перевірка Лотів з ЛЦ",eTypeControlDoc.Ask,true,true,true,true,true,2,2,0,false,true,true,false);
                if(Right[2])
                    Setting[step++] = new DocSetting(1,"Прихід",eTypeControlDoc.Control,false,false,false,true,true,1,1,3,true,true,true,false);
                if(Right[3])
                    Setting[step++] = new DocSetting(6,"Ревізія", eTypeControlDoc.Ask,true,false,false,false,false,1,1,0,false,false,true,false);

                break;
            case SparPSU:
            case VopakPSU:
                Setting =  new  DocSetting[5];
                Setting[0] = new DocSetting(1,"Ревізія",eTypeControlDoc.Ask,false,false,false,false,true,1,1,0,false,true,false,false);
                Setting[1] = new DocSetting(2,"Прихід",eTypeControlDoc.Control,false,false,false,true,true,1,3,3,true,true,true,false);
                Setting[2] = new DocSetting(3,"Переміщення");
                Setting[3] = new DocSetting(4,"Списання");
                Setting[4] = new DocSetting(5,"Повернення");
                break;
        }
       return Setting;
    }

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
                config.ApiUrl=(config.Company==eCompany.Sim23 ? "http://176.241.128.13/RetailShop/hs/TSD/":"http://znp.vopak.local/api/api_v1_utf8.php");
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
        if(config.Company==eCompany.Sim23) {
            if (pTypeDoc == -1)
                c.LoadGuidData((pTypeDoc == -1), pProgress);
        }
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
    public WaresItemModel GetWaresFromBarcode(int pTypeDoc, String pNumberDoc, String pBarCode) {
        return mDbHelper.GetScanData(pTypeDoc, pNumberDoc, pBarCode);
    }
    // Збереження товару в БД
    public Result SaveDocWares(int pTypeDoc, String pNumberDoc, int pCodeWares, int pOrderDoc, Double pQuantity, int pCodeReason , Boolean pIsNullable) {
        if (pIsNullable)
            mDbHelper.SetNullableWares(pTypeDoc, pNumberDoc, pCodeWares);

        Result r = mDbHelper.SaveDocWares(pTypeDoc, pNumberDoc, pCodeWares, pOrderDoc, pQuantity, pCodeReason);
        // міняємо стан документа на готується при зміні кількості.
        mDbHelper.UpdateDocState(0, pTypeDoc, pNumberDoc);
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

    public void SaveDocOut(Doc pDoc ){mDbHelper.SaveDocOut(pDoc);}

    public DocModel GetDoc(int pTypeDoc, String pNumberDoc, int pTypeResult, eTypeOrder pTypeOrder) {
        DocModel result= GetDocOut(pTypeDoc, pNumberDoc);
         result.WaresItem= mDbHelper.GetDocWares(pTypeDoc, pNumberDoc, pTypeResult,pTypeOrder);
        return result;
    }

    public int FindWhIP( Warehouse[] pWarehouses) {
        int res=-1;
        try {
            String Ip = config.cUtils.GetIp();
            if (Ip == null)
                return res;
            String[] IP = Ip.split("\\.");//192.168.1.235
            if (IP.length != 4)
                return res;
            for (int i = 0; i < pWarehouses.length; i++) {
                String[] WhIp = pWarehouses[i].InternalIP.split("\\.");
                if (WhIp.length != 4)
                    continue;
                if (IP[0].equals(WhIp[0]) && IP[1].equals(WhIp[1]) && IP[2].equals(WhIp[2])) {
                    return i;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "FindWhIP=>" + e.getMessage());
        }
        return res;
    }



    public  Warehouse[] GetWarehouse(){
        Warehouse[] Wh;
        Connector c = Connector.instance();
        Wh=c.LoadWarehouse();
        if(Wh !=null && Wh.length>0)
            mDbHelper.SaveWarehouse(Wh);
        else {
            List<Warehouse> lWh = mDbHelper.GetWarehouse();
            Wh = lWh.toArray(new Warehouse[lWh.size()]);
        }
        return Wh;
    }

}

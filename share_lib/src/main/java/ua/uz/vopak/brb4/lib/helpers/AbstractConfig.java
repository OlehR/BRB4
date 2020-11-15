package ua.uz.vopak.brb4.lib.helpers;

import android.content.Context;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;

import static android.os.Build.USER;

public abstract class AbstractConfig {

    protected static AbstractConfig Instance = null;
    public Boolean IsDebug = false;
    public Context context;
    public int CodeWarehouse = 0;
    public eCompany Company = eCompany.VopakPSU;
    public boolean IsOnline=false;

    public String SN;
    public String NameDCT = USER;

    public boolean isAutorized;
    public String Login = "";
    public String Password = "123";
    public int GetCodeUnitWeight() { return Company == eCompany.VopakPSU?7:166;}

    public String ApiUrl,ApiURLadd;

    public eTypeScaner TypeScaner = eTypeScaner.NotDefine;
    public eTypeUsePrinter TypeUsePrinter;

    /// Взагалі йому тут не місце;
    public Integer NumberPackege = 0;
    //public Integer LogOrder = 0;

    //Номер сканування цінників за день
    public int LineNumber=0;
    public  Utils cUtils;

    public AbstractConfig(){};
    public AbstractConfig(String url){
        this.ApiUrl = url;
    }
    public void Init(Context pContext) {
        context = pContext;
        cUtils=new Utils(context);
        SN= cUtils.GetSN();
        //Визначаємо тип Сканера
        TypeScaner =cUtils.GetTypeScaner();
    }

    public String GetApiJson(int codeData, String data){
        return new String();
    }

    public String getCodeWarehouse() {
        String code = "000000000" + CodeWarehouse;
        return code.substring(code.length() - 9);
    }

    public String getApiUrl(){
        return this.ApiUrl;
    }

    public static AbstractConfig instance() //throws Exception
    {
        /*if (Instance == null ){
            throw new Exception("Відсутній екземпляр AbstractConfig");
        }*/
        return  Instance;
    }

}

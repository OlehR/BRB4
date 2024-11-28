package ua.uz.vopak.brb4.brb4.helpers;

import android.app.Activity;
import android.content.Intent;

import ua.uz.vopak.brb4.brb4.Connector.Connector;
import ua.uz.vopak.brb4.brb4.MainActivity;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.brb4.models.Config;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eRole;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;
import ua.uz.vopak.brb4.lib.helpers.Utils;
import ua.uz.vopak.brb4.lib.helpers.UtilsUI;
import ua.uz.vopak.brb4.lib.models.Result;

public class AuterizationsHelper {
    private static String TAG = "AuterizationsHelper";
    Config config = Config.instance();
    GetDataHTTP Http= GetDataHTTP.instance();// new GetDataHTTP(new String[]{config.ApiUrl, config.ApiURLadd});

    public String Login(Activity pActivity,final String pLogin,final String pPassWord,boolean pIsLoginCO,boolean IsRunMainActivity)
    {
        String Res=null;
        try {
            if ((pLogin.equals("Admin") && pPassWord.equals("13579")) || pLogin.equals("OffLine")) {
                SetLoginPw( pLogin, pPassWord,pIsLoginCO);
                config.Role= eRole.Admin;
                ExecuteMainActivity(pActivity,IsRunMainActivity);
                return "Ok";
            }

            Connector c = Connector.instance();
            Result r=c.Login(pLogin,pPassWord,pIsLoginCO);
            if(r.State==0) {
                Res="Ok";
                config.DocsSetting=c.GenSettingDocs(config.Company,config.Role);
                SetLoginPw(pLogin,pPassWord,pIsLoginCO);
                if( pIsLoginCO && config.Company==eCompany.Sim23) //Визначення магазина по IP
                {
                    Worker worker=config.GetWorker();
                    Warehouse[] Wh= worker.GetWarehouse(); //con.LoadWarehouse();

                    int i = worker.FindWhIP(Wh);
                    if(i>=0){
                        if(config.CodeWarehouse!=Wh[i].Code) {
                            config.CodeWarehouse = Wh[i].Code;
                            config.ApiUrl = Wh[i].Url;
                            worker.AddConfigPair("Warehouse", Integer.toString(config.CodeWarehouse));
                            worker.AddConfigPair("ApiUrl", config.ApiUrl);
                            Http.Init(new String[]{config.ApiUrl, config.ApiURLadd,config.ApiUrl3});
                            Res= "Знайдено новий магазин\n"+ Wh[i].Name+" " +config.ApiUrl;
                            //UtilsUI.Dialog("Знайдено новий магазин", Wh[i].Name+" " +config.ApiUrl);
                        }
                    }
                    else
                        Res="НЕ визначено  магазин \nIP=>" +config.cUtils.GetIp();

                }
                ExecuteMainActivity(pActivity,IsRunMainActivity);
                return Res;
            }
            else {
                MessageError(pActivity, r.Info, r.TextError);
                return Res;
            }

        }catch (Exception e){
            Utils.WriteLog("e",TAG, "Login >>"+" Res=>"+Res,e);
        }
        return null;
    }

    public AuterizationsHelper(){
    }

    public void ExecuteMainActivity(Activity activity,boolean IsRunMainActivity)
    {
        if(IsRunMainActivity) {
            Intent i = new Intent(activity, MainActivity.class);
            activity.startActivity(i);
        }
    }
    public void SetLoginPw(final String pLogin,final String pPassWord,boolean pIsLoginCO)
    {
        config.Login=pLogin;
        config.Password=pPassWord;
        config.isAutorized = true;
        config.IsLoginCO=pIsLoginCO;
        config.Worker.AddConfigPair("Login",config.Login);
        config.Worker.AddConfigPair("PassWord",config.Password);
        config.Worker.AddConfigPair("IsLoginCO",config.IsLoginCO?"true":"false");
    }
    void MessageError(final Activity activity,final String Message,final String exMessage)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UtilsUI UtilsUI = new UtilsUI(activity);
                UtilsUI.Dialog(Message,exMessage);
            }});
    }

}
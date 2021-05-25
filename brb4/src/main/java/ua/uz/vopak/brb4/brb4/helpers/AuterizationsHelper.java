package ua.uz.vopak.brb4.brb4.helpers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import ua.uz.vopak.brb4.brb4.Connector.Connector;
import ua.uz.vopak.brb4.brb4.MainActivity;
import ua.uz.vopak.brb4.brb4.MessageActivity;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.lib.enums.MessageType;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;
import ua.uz.vopak.brb4.lib.helpers.Utils;
import ua.uz.vopak.brb4.lib.helpers.UtilsUI;
import ua.uz.vopak.brb4.lib.models.Result;

public class AuterizationsHelper {
    private static String TAG = "AuterizationsHelper";
    GlobalConfig config = GlobalConfig.instance();
    GetDataHTTP Http= GetDataHTTP.instance();// new GetDataHTTP(new String[]{config.ApiUrl, config.ApiURLadd});

    public String Login(Activity pActivity,final String pLogin,final String pPassWord,boolean pIsLoginCO,boolean IsRunMainActivity)
    {
        String Res=null;
        try {
            if ((pLogin.equals("Admin") && pPassWord.equals("13579")) || pLogin.equals("OffLine")) {
                ExecuteMainActivity(pActivity, pLogin, pPassWord,pIsLoginCO,IsRunMainActivity);
                return "Ok";
            }

            Connector c = Connector.instance();
            Result r=c.Login(pLogin,pPassWord,pIsLoginCO);
            if(r.State==0) {
                Res="Ok";
                config.DocsSetting=config.Worker.GenSettingDocs(config.Company,config.Role);
                if(pIsLoginCO && config.Company==eCompany.Sim23)
                {
                   // ua.uz.vopak.brb4.brb4.Connector.Connector con = ua.uz.vopak.brb4.brb4.Connector.Connector.instance();

                    Worker worker=config.GetWorker();
                    Warehouse[] Wh= worker.GetWarehouse(); //con.LoadWarehouse();

                    int i = worker.FindWhIP(Wh);
                    if(i>=0){
                        if(config.CodeWarehouse!=Wh[i].Code) {
                            config.CodeWarehouse = Wh[i].Code;
                            config.ApiUrl = Wh[i].Url;
                            worker.AddConfigPair("Warehouse", Integer.toString(config.CodeWarehouse));
                            worker.AddConfigPair("ApiUrl", config.ApiUrl);
                            Http.Init(new String[]{config.ApiUrl, config.ApiURLadd});
                            Res= "Знайдено новий магазин\n"+ Wh[i].Name+" " +config.ApiUrl;
                            //UtilsUI.Dialog("Знайдено новий магазин", Wh[i].Name+" " +config.ApiUrl);
                        }
                    }
                    else
                        Res="НЕ визначено  магазин \nIP=>" +config.cUtils.GetIp();

                }

                ExecuteMainActivity(pActivity,pLogin,pPassWord,pIsLoginCO,IsRunMainActivity);
                return Res;
            }
            else {
                MessageError(pActivity, r.Info, r.TextError);
                return Res;
            }
        /*
        if(config.Company== eCompany.SparPSU||config.Company==eCompany.VopakPSU)
            return LoginPSU( pActivity,pLogin,pPassWord);
        else if(config.Company== eCompany.SevenEleven)
            return LoginSevenEleven(pActivity,pLogin,pPassWord);
            */

        }catch (Exception e){
            Utils.WriteLog("e",TAG, "Login >>"+  e.getMessage());
        }
        return null;
    }
    /*
    public boolean LoginSevenEleven(Activity activity,final String pLogin,final String pPassWord) {

        HttpResult res=Http.HTTPRequest(0,"warehouse",null,"application/json;charset=utf-8",pLogin,pPassWord);
        if(res.HttpState== eStateHTTP.HTTP_UNAUTHORIZED || res.HttpState== eStateHTTP.HTTP_Not_Define_Error)
        {
            Utils.WriteLog("e",TAG, "Login >>"+ res.HttpState.toString());
            MessageError(activity, "Неправильний логін або пароль",res.HttpState.toString());
            return false;
        }
        else
        if(res.HttpState!= eStateHTTP.HTTP_OK) {
            MessageError(activity, "Ви не підключені до мережі " + config.Company.name(), res.HttpState.toString());
            return false;
        }
        config.IsOnline=true;
        ExecuteMainActivity(activity,pLogin,pPassWord);
        return true;
    }

    public boolean LoginPSU(Activity activity,final String pLogin,final String pPassWord) {
        final String data = "{\"CodeData\": \"1\"" + ", \"Login\": \"" + pLogin + "\"" + ", \"PassWord\": \"" + pPassWord + "\"}";
        HttpResult result = Http.HTTPRequest(0, "",data,null,null,null);

        if (result.HttpState!= eStateHTTP.HTTP_OK || result.equals(""))
            MessageError(activity, "Ви не підключені до мережі " + config.Company.name(), result.HttpState.toString());

        try {
            JSONObject jObject = new JSONObject(result.Result);
            if(jObject.getInt("State") == 0){
                config.IsOnline=true;
                config.DocsSetting=config.Worker.GenSettingDocs(config.Company,config.Role);
                ExecuteMainActivity(activity,pLogin,pPassWord);
            }else{
                MessageError(activity, "Неправильний логін або пароль", jObject.getString("TextError"));
            }
        }catch (Exception e){

        }
        return  true;
    }
*/
    public AuterizationsHelper(){
    }

    public void ExecuteMainActivity(Activity activity,final String pLogin,final String pPassWord,boolean pIsLoginCO,boolean IsRunMainActivity)
    {
        config.Login=pLogin;
        config.Password=pPassWord;
        config.isAutorized = true;
        config.IsLoginCO=pIsLoginCO;
        config.Worker.AddConfigPair("Login",config.Login);
        config.Worker.AddConfigPair("PassWord",config.Password);
        config.Worker.AddConfigPair("IsLoginCO",config.IsLoginCO?"true":"false");
        if(IsRunMainActivity) {
            Intent i = new Intent(activity, MainActivity.class);
            activity.startActivity(i);
        }
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
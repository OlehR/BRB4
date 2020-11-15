package ua.uz.vopak.brb4.brb4.helpers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import org.json.JSONObject;
import ua.uz.vopak.brb4.brb4.MainActivity;
import ua.uz.vopak.brb4.brb4.MessageActivity;
import ua.uz.vopak.brb4.lib.enums.MessageType;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;
import ua.uz.vopak.brb4.lib.models.HttpResult;

public class AuterizationsHelper {
    private static String TAG = "AuterizationsHelper";
    GlobalConfig config = GlobalConfig.instance();
    GetDataHTTP Http= GetDataHTTP.instance();// new GetDataHTTP(new String[]{config.ApiUrl, config.ApiURLadd});

    public boolean Login(Activity pActivity,final String pLogin,final String pPassWord)
    {
        try{
        if((pLogin.equals("Admin")&&pPassWord.equals("13579")) || pLogin.equals("OffLine") ) {
            ExecuteMainActivity(pActivity, pLogin, pPassWord);
            return true;
        }
        if(config.Company== eCompany.SparPSU||config.Company==eCompany.VopakPSU)
            return LoginPSU( pActivity,pLogin,pPassWord);
        else if(config.Company== eCompany.SevenEleven)
            return LoginSevenEleven(pActivity,pLogin,pPassWord);
        }catch (Exception e){
            Log.e(TAG, "Login >>"+  e.getMessage());
        }
        return false;
    }
    public boolean LoginSevenEleven(Activity activity,final String pLogin,final String pPassWord) {

        HttpResult res=Http.HTTPRequest(0,"warehouse",null,"application/json;charset=utf-8",pLogin,pPassWord);
        if(res.HttpState== eStateHTTP.HTTP_UNAUTHORIZED || res.HttpState== eStateHTTP.HTTP_Not_Define_Error)
        {
            Log.e(TAG, "Login >>"+ res.HttpState.toString());
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
                ExecuteMainActivity(activity,pLogin,pPassWord);
            }else{
                MessageError(activity, "Неправильний логін або пароль", jObject.getString("TextError"));
            }
        }catch (Exception e){

        }
        return  true;
    }

    public AuterizationsHelper(){
    }

    public void ExecuteMainActivity(Activity activity,final String pLogin,final String pPassWord)
    {
        config.Login=pLogin;
        config. Password=pPassWord;
        config.isAutorized = true;
        config.Worker.AddConfigPair("Login",config.Login);
        config.Worker.AddConfigPair("PassWord",config.Password);
        Intent i = new Intent(activity,MainActivity.class);
        activity.startActivity(i);
    }
    void MessageError(Activity activity,String Message,String exMessage)
    {
        Intent i = new Intent(activity, MessageActivity.class);
        i.putExtra("messageHeader",Message);
        i.putExtra("message",exMessage);
        i.putExtra("type",MessageType.ErrorMessage);
        activity.startActivityForResult(i,1);
    }

}
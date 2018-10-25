package ua.uz.vopak.brb4.brb4.helpers;

import android.content.Intent;

import org.json.JSONObject;

import ua.uz.vopak.brb4.brb4.AuthActivity;
import ua.uz.vopak.brb4.brb4.MainActivity;
import ua.uz.vopak.brb4.brb4.MessageActivity;
import ua.uz.vopak.brb4.brb4.enums.MessageType;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class AuterizationsHelper {
    GlobalConfig config = GlobalConfig.instance();
    private static String token = null;
    public static Boolean isAutorized = false;
    AuthActivity activity;

    public AuterizationsHelper Start(String data){

        String result = new GetDataHTTP().HTTPRequest(config.ApiUrl, data);

        if(result.equals("")){
            Intent i = new Intent(activity, MessageActivity.class);
            i.putExtra("messageHeader","Ви не підключені до мережі Вопак");
            i.putExtra("message","");
            i.putExtra("type",MessageType.ErrorMessage);
            activity.startActivityForResult(i,1);
        }

        try {
            JSONObject jObject = new JSONObject(result);

            if(jObject.getInt("State") == 0){

                isAutorized = true;
                Intent i = new Intent(activity,MainActivity.class);
                activity.startActivity(i);

            }else{
                Intent i = new Intent(activity, MessageActivity.class);
                i.putExtra("messageHeader","Неправильний логін або пароль");
                i.putExtra("message",jObject.getString("TextError"));
                i.putExtra("type",MessageType.ErrorMessage);
                activity.startActivityForResult(i,1);
            }

        }catch (Exception e){

        }

        return  this;
    }

    public AuterizationsHelper(){

    }

    public AuterizationsHelper(AuthActivity context){
        activity = context;
    }
}
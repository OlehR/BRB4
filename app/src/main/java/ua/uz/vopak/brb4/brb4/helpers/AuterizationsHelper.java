package ua.uz.vopak.brb4.brb4.helpers;

import android.content.Intent;

import org.json.JSONObject;

import ua.uz.vopak.brb4.brb4.AuthActivity;
import ua.uz.vopak.brb4.brb4.MainActivity;

public class AuterizationsHelper {
    private static String token = null;
    public static Boolean isAutorized = false;
    AuthActivity activity;

    public AuterizationsHelper Start(String data){
        String url = "http://znp.vopak.local/api/api_v1.php";

        String result = new GetDataHTTP().HTTPRequest(url, data);

        try {
            JSONObject jObject = new JSONObject(result);

            if(jObject.getInt("State") == 0){

                isAutorized = true;
                Intent i = new Intent(activity,MainActivity.class);
                activity.startActivity(i);

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
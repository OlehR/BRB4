package ua.uz.vopak.brb4.brb4.helpers;

import org.json.JSONObject;

import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class WareHauseHelper {
    GlobalConfig config = GlobalConfig.instance();

    public WareHauseHelper getWares(){
        String CodeData = "\"CodeData\": \"1\"";
        String Login = "\"Login\": \"" + config.Login + "\"";
        String PassWord = "\"PassWord\": \"" + config.Password + "\"";
        String data = "{"+ CodeData +", "+ Login +", "+ PassWord +"}";

        String result = new GetDataHTTP().HTTPRequest(config.ApiUrl, data);

        try {
            JSONObject jObject = new JSONObject(result);

            if(jObject.getInt("State") == 0){


            }

        }catch (Exception e){

        }

        return  this;
    }
}

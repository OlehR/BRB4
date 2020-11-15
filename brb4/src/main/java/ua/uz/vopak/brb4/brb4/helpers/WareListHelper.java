package ua.uz.vopak.brb4.brb4.helpers;

import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ua.uz.vopak.brb4.brb4.SettingsActivity;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;
/*
public class WareListHelper {

    public SettingsActivity activity;
    public Map<String, String> map = new HashMap<String, String>();
    public ArrayAdapter<String> adapter;

    public WareListHelper getWares(){
        final GlobalConfig config = GlobalConfig.instance();

        String CodeData = "\"CodeData\": \"210\"";
        String Login = "\"Login\": \"" + config.Login + "\"";
        String PassWord = "\"PassWord\": \"" + config.Password + "\"";
        String data = "{"+ CodeData +", "+ Login +", "+ PassWord +"}";

        String result = new GetDataHTTP().HTTPRequest(config.ApiUrl, data);

        try {
            JSONObject jObject = new JSONObject(result);

            if(jObject.getInt("State") == 0){
                JSONArray arrJson = jObject.getJSONArray("Warehouse");
                String[] path = new String[arrJson.length()];

                for(int i = 0; i < arrJson.length(); i++) {
                    JSONArray innerArr = arrJson.getJSONArray(i);

                    path[i] = innerArr.getString(1);
                    map.put(innerArr.getString(1), innerArr.getString(0));
                }

                adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item,path);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            }

        }catch (Exception e){
            e.getMessage();
        }

        return  this;
    }

    public WareListHelper(SettingsActivity context){
        activity = context;
    }
}
*/
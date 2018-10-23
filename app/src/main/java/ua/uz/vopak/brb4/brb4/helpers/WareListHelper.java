package ua.uz.vopak.brb4.brb4.helpers;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import ua.uz.vopak.brb4.brb4.R;
import ua.uz.vopak.brb4.brb4.SettingsActivity;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

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

            if(jObject.getInt("State") == 1){
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

    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    public WareListHelper(SettingsActivity context){
        activity = context;
    }
}

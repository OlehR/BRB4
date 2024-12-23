package ua.uz.vopak.brb4.clientpricechecker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.os.Build;

import org.xmlpull.v1.XmlPullParser;

import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.helpers.AbstractConfig;

public class Config extends AbstractConfig {
    private static Context context;

    public static String SmbDomain;
    public static String SmbUser;
    public static String SmbPassword;
    public static String SmbPath;
    public static String SmbServer;
    public static Boolean IsHungary=false;

    XmlResourceParser xrp;

    SharedPreferences pref ;//= getApplicationContext().getSharedPreferences("Pref", 0); // 0 - for private mode
    SharedPreferences.Editor editor;// = pref.edit();

    protected Config(Context parContext){
        super("http://znp.vopak.local/api/api_v1_utf8.php");//
        context = parContext;

        pref = context.getApplicationContext().getSharedPreferences("ClientPriceChecker", 0); // 0 - for private mode
        editor = pref.edit();

        int Wh=pref.getInt("CodeWarehouse",0);

        xrp = context.getResources().getXml(R.xml.config);

        try {
            xrp.next();
            int eventType = xrp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    switch (xrp.getName()){
                        case "CodeWarehouse" :
                            CodeWarehouse = Integer.valueOf(xrp.getAttributeValue(null,"value"));
                            Company= (CodeWarehouse>30)?(CodeWarehouse==195 || CodeWarehouse==199 ?eCompany.LuboPSU :eCompany.SparPSU) :eCompany.VopakPSU;
                             IsHungary = (CodeWarehouse>30) && (CodeWarehouse<148);
                            break;
                        case "Login" :
                            Login = xrp.getAttributeValue(null,"value");
                            break;
                        case "Password" :
                            Password = xrp.getAttributeValue(null,"value");
                            break;
                        case "SmbDomain" :
                            SmbDomain = xrp.getAttributeValue(null,"value");
                            break;
                        case "SmbUser" :
                            SmbUser = xrp.getAttributeValue(null,"value");
                            break;
                        case "SmbPassword" :
                            SmbPassword = xrp.getAttributeValue(null,"value");
                            break;
                        case "SmbPath" :
                            SmbPath = xrp.getAttributeValue(null,"value");
                            break;
                        case "SmbServer" :
                            SmbServer = xrp.getAttributeValue(null,"value");
                            break;
                        case "ApiUrl" :
                            ApiUrl = xrp.getAttributeValue(null,"value");
                            break;
                    }
                }
                eventType = xrp.next();
            }
            xrp.setProperty("CodeWarehouse","9999");
        }catch (Exception e){
            e.toString();
        }
        Init(parContext);
    }

    public static Config instance(Context context) {
        if (Instance == null) {
            Instance = new Config(context);
        }
        return (Config) Instance;
    }

}

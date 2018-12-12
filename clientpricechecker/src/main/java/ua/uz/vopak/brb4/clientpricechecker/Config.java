package ua.uz.vopak.brb4.clientpricechecker;

import android.content.Context;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;

public class Config {
    private static Context context;
    private static Config Instance = null;
    public static String CodeWarehouse;
    public static String Login;
    public static String Password;
    public static String SmbDomain;
    public static String SmbUser;
    public static String SmbPassword;
    public static String SmbPath;
    XmlResourceParser xrp;

    public Config(Context parContext){
        context = parContext;
        xrp = context.getResources().getXml(R.xml.config);

        try {
            xrp.next();
            int eventType = xrp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    switch (xrp.getName()){
                        case "CodeWarehouse" :
                            CodeWarehouse = xrp.getAttributeValue(null,"value");
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
                    }
                }
                eventType = xrp.next();
            }
        }catch (Exception e){
            e.toString();
        }
    }

    public static String GetLoginJson() {
        return "\"Login\": \"" + Login + "\",\"PassWord\": \"" + Password + "\"";
    }

    public String getCodeWarehouse() {
        String code = "000000000" + CodeWarehouse;
        return code.substring(code.length() - 9);
    }

    public static Config instance(Context context) {
        if (Instance == null) {
            Instance = new Config(context);
        }
        return Instance;
    }
}

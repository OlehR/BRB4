package ua.uz.vopak.brb4.clientpricechecker;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Build;

import org.xmlpull.v1.XmlPullParser;

import ua.uz.vopak.brb4.lib.helpers.AbstractConfig;

public class Config extends AbstractConfig {
    private static Context context;
    //private static Config Instance = null;
    public static String CodeWarehouse;
    public static String Login;
    public String SN = Build.SERIAL;
    public String NameDCT = Build.USER;

    public static String Password;
    public static String SmbDomain;
    public static String SmbUser;
    public static String SmbPassword;
    public static String SmbPath;
    public static String SmbServer;
    public static boolean IsSpar;
    XmlResourceParser xrp;

    protected Config(Context parContext){
        super("http://znp.vopak.local/api/api_v1_utf8.php");//
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
                            IsSpar= (Integer.valueOf(CodeWarehouse)>30);
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
        }catch (Exception e){
            e.toString();
        }
    }

    @Override
    public String GetApiJson(int parCodeData, String parData) {
        return "{\"CodeData\":"+ Integer.toString(parCodeData) + ",\"SerialNumber\":\""+SN+"\",\"NameDCT\":\""+NameDCT+"\", \"Warehouse\":\""+this.getCodeWarehouse()+"\", \"CodeWarehouse\":\""+this.getCodeWarehouse()+"\", \"Login\": \"" + Login + "\",\"PassWord\": \"" + Password + "\"" +
                (parData==null?"":","+parData )+"}";
    }

    @Override
    public String getCodeWarehouse() {
        String code = "000000000" + CodeWarehouse;
        return code.substring(code.length() - 9);
    }

    public static Config instance(Context context) {
        if (Instance == null) {
            Instance = new Config(context);
        }
        return (Config) Instance;
    }


}

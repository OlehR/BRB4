package ua.uz.vopak.brb4.brb4.models;

public class GlobalConfig {
    private static GlobalConfig Instance = null;
    public static String CodeWarehouse = "9";
    public static String ApiUrl = "http://znp.vopak.local/api/api_v1_utf8.php";
    public static String Login = "";
    public static String Password = "";

    public String getCodeWarehouse(){
        String code = "00000000" + CodeWarehouse;
        return code.substring(code.length() - 9);
    }

    protected GlobalConfig(){

    }

    public static GlobalConfig instance(){
        if(Instance == null){
            Instance = new GlobalConfig();
        }

        return Instance;
    }
}

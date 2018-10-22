package ua.uz.vopak.brb4.brb4.models;

public class GlobalConfig {
    private static GlobalConfig Instance = null;
    public static String CodeWarehouse = "000000009";
    public static String ApiUrl = "http://znp.vopak.local/api/api_v1.php";
    public static String Login = "";
    public static String Password = "";

    protected  GlobalConfig(){

    }

    public static GlobalConfig instance(){
        if(Instance == null){
            Instance = new GlobalConfig();
        }

        return Instance;
    }
}

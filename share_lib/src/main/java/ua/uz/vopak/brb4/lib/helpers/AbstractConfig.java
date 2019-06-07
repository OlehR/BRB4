package ua.uz.vopak.brb4.lib.helpers;

public abstract class AbstractConfig {
    public String ApiUrl;

    public AbstractConfig(String url){
        this.ApiUrl = url;
    }
    public String GetApiJson(int codeData, String data){
        return new String();
    }
    public String getCodeWarehouse(){
        return new String();
    }
    public String getApiUrl(){
        return this.ApiUrl;
    }
}

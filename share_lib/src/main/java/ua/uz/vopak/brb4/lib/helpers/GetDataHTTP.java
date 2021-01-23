package ua.uz.vopak.brb4.lib.helpers;


import android.util.Log;
import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;

import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.models.HttpResult;

public class GetDataHTTP
{
    protected static GetDataHTTP Instance = null;
    String[][] Url;
    int [] DefaultApi;
    static AbstractConfig config;
    protected static final String TAG = "BRB4/GetDataHTTP";
    public GetDataHTTP(){};
    public GetDataHTTP(String [] pUrl)
    {
       Init(pUrl);
    };
    public void Init(String [] pUrl)
    {
        DefaultApi = new int[pUrl.length];
        Url = new String[2][];
        for (int i = 0; i < pUrl.length; i++) {
            DefaultApi[i]=0;
            if(pUrl[i]!=null) {
                String[] Urls = pUrl[i].split(";");
                Url[i] = Urls;
            }
        }
        Instance=this;
    }
    public static GetDataHTTP instance() {
        if (Instance == null) {
            config =  AbstractConfig.instance();
            Instance = (config==null? new GetDataHTTP() : new GetDataHTTP(new String[]{config.ApiUrl,config.ApiURLadd}));
         }
        return Instance;
    }

  /*  public String GetData(String parCodeShop,String parScanCode,String parCode) {
        if (parScanCode == null || parScanCode.isEmpty())
            parScanCode = "";
        if (parCode == null || parCode.isEmpty())
            parCode = "";

        String response = "";
        String varHTTPRegest = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\n" +
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "\n" +
                "<soap:Body><GetInfoForTheProduct xmlns=\"vopak\">" + "\n" +
                "<CodeOfShop>" + parCodeShop + "</CodeOfShop>" + "\n" +
                "<Scancode>" + parScanCode + "</Scancode>" + "\n" +
                "<CodeOfProduct>" + parCode + "</CodeOfProduct>" + "\n" +
                "</GetInfoForTheProduct>" + "\n" +
                "</soap:Body>" + "\n" +
                "</soap:Envelope>";
        String requestURL = "http://1CSRV/utppsu/ws/ws1.1cws";
        response=HTTPRequest(requestURL,varHTTPRegest);

            if( response!=null && !response.isEmpty() && response.indexOf("</m:return>")>0 && response.indexOf("-instance\">")>0) {
                response = response.substring(response.indexOf("-instance\">") + 11);
                response = response.substring(0, response.indexOf("</m:return>"));
            }
            else
                response="0;Товар не Знайдено;0,00;;0;;0";

        return response;
    }
*/
public String GetBaseAuth(String pLogin,String pPasWord){
   return "Basic YnJiOmJyYg==";
   // return "Basic " +  Base64.encode(("brb:brb").getBytes(), Base64.NO_WRAP);
    //return "Basic " +  Base64.encodeToString() ("brb:brb");
   // String s= Base64.encode((pLogin.trim()+":"+pPasWord.trim()).getBytes(), Base64.NO_WRAP);
    //s= Base64.encode((pLogin.trim()+":"+pPasWord.trim()).getBytes());
    //return "Basic "+Base64.encodeBytes("brb:brb".getBytes());
    //"Authorization"
}

    public HttpResult HTTPRequest(String pURL,String pData )    {
        return HTTPRequest(pURL,pData, "text/xml;charset=utf-8");
    }

    public HttpResult HTTPRequest(String pURL,String pData,String pContentType)    {
        return HTTPRequest( pURL, pData, pContentType,null,null);
    }

    public HttpResult HTTPRequest(String pURL,String pData,String pContentType,final String pLogin,final String pPassWord){
     if(pContentType==null)
         pContentType= "text/xml;charset=utf-8";
     HttpResult res = new HttpResult();
        String log="\n"+pURL+"\nData=>"+pData;
     if(pLogin!=null)
        Authenticator.setDefault(new Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(pLogin,pPassWord.toCharArray());
            }});
        URL url;
       // String response = "";
        HttpURLConnection conn = null;
        try {
            url = new URL(pURL);

             conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setUseCaches(false);
           /* if (pPropertyName != null)
                conn.setRequestProperty(pPropertyName, pPropertyValue);*/
            conn.setRequestProperty("Content-Type", pContentType);
            //conn.setRequestMethod(pMetod);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);

            if(pData!=null) {
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(pData);//(URLEncoder.encode(varHTTPRegest, "UTF-8"));

                writer.flush();
                writer.close();
                os.close();
            }
            Log.d(TAG,"Start");
            int responseCode=conn.getResponseCode();
            Log.d(TAG,"responseCode");
            res.HttpState = eStateHTTP.fromId(responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK ) {
                StringBuilder everything = new StringBuilder();
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                Log.d(TAG,"StartRead");
                while ((line=br. readLine()) != null) {
                    everything.append(line);
                    //response+=line;
                }
                res.Result=everything.toString();
                Log.d(TAG,"EndRead");
            }

            log+= "\nResponse=>"+ (res.Result.length()>2000? res.Result.substring(0,2000):res.Result);

        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
            res.HttpState= eStateHTTP.HTTP_Not_Define_Error;
            if(e.getMessage().equals("Read timed out")) {
                res.HttpState = eStateHTTP.HTTP_CLIENT_TIMEOUT;
                return res;
            }
            if(conn!=null)
                try {
                    int a = conn.getResponseCode();
                    res.HttpState = eStateHTTP.fromId(a);
                    log+="\n Error=>"+e.getMessage();
                }catch (Exception ex){};
        }
        Utils.WriteLog(log);
        return res;
    }

    public HttpResult HTTPRequest (int pUrlApi,String pApi,String pData,String pContentType, String pLogin, String pPassWord)
    {
        if(pLogin!=null && pLogin.equals("Admin")) {
            pLogin= (config.Company== eCompany.SevenEleven?"brb":"c");
            pPassWord = (config.Company== eCompany.SevenEleven?"brb":"c");
        }
        HttpResult res=new HttpResult() ;
        if(Url!=null && Url.length>=pUrlApi &&  Url[pUrlApi]!=null)
        {
            res= HTTPRequest(Url[pUrlApi][DefaultApi[pUrlApi]]+pApi,pData,pContentType,pLogin,pPassWord);
            if(res.HttpState!=eStateHTTP.HTTP_OK && res.HttpState != eStateHTTP.HTTP_UNAUTHORIZED)
            {
                for (int i = 0; i < Url[pUrlApi].length ; i++) {
                    if(i!=DefaultApi[pUrlApi] && Url[pUrlApi][i]!=null && !Url[pUrlApi][i].isEmpty())
                    {
                        res= HTTPRequest(Url[pUrlApi][i]+pApi,pData,pContentType,pLogin,pPassWord);
                        if(res.HttpState==eStateHTTP.HTTP_OK)
                            DefaultApi[pUrlApi]=i;
                    }
                }
            }
        }
        return res;
    }
}

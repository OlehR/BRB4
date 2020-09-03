package ua.uz.vopak.brb4.lib.helpers;


import android.util.Base64;
import android.util.Log;

import java.io.*;
//import java.io.UnsupportedEncodingException;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;

import ua.uz.vopak.brb4.lib.enums.eStateHTTP;

public class GetDataHTTP
{
    protected static final String TAG = "BRB4/GetDataHTTP";
    public eStateHTTP HttpState = eStateHTTP.HTTP_OK;

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

    public String HTTPRequest(String pURL,String pData )    {
        return HTTPRequest(pURL,pData, "text/xml;charset=utf-8");
    }

    public String HTTPRequest(String pURL,String pData,String pContentType)    {
        return HTTPRequest( pURL, pData, pContentType,null,null);
    }

    public String HTTPRequest(String pURL,String pData,String pContentType,final String pLogin,final String pPassWord){
        String log="\n"+pURL+"\nData=>"+pData;
     if(pLogin!=null)
        Authenticator.setDefault(new Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(pLogin,pPassWord.toCharArray());
            }});
        URL url;
        String response = "";
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
            HttpState = eStateHTTP.fromId(responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK ) {
                StringBuilder everything = new StringBuilder();
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                Log.d(TAG,"StartRead");
                while ((line=br. readLine()) != null) {
                    everything.append(line);
                    //response+=line;
                }
                response=everything.toString();
                Log.d(TAG,"EndRead");
            }
            else {
                response="";
            }
            log+= "\nResponse=>"+ (response.length()>2000? response.substring(0,2000):response);

        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
            HttpState= eStateHTTP.HTTP_Not_Define_Error;
            if(e.getMessage().equals("Read timed out")) {
                HttpState = eStateHTTP.HTTP_CLIENT_TIMEOUT;
                return response;
            }
            if(conn!=null)
                try {
                    int a = conn.getResponseCode();
                    HttpState = eStateHTTP.fromId(a);
                    log+="\n Error=>"+e.getMessage();
                }catch (Exception ex){};

        }
        Utils.WriteLog(log);
        return response;


    }





}

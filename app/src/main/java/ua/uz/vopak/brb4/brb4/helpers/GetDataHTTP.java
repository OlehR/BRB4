package ua.uz.vopak.brb4.brb4.helpers;


import java.io.*;
//import java.io.UnsupportedEncodingException;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;

import ua.uz.vopak.brb4.brb4.enums.eStateHTTP;

public class GetDataHTTP
{
    eStateHTTP HttpState = eStateHTTP.HTTP_OK;
    public String GetData(String parCodeShop,String parScanCode,String parCode) {
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
        if(response!=null && !response.isEmpty())
        {
            if(response.indexOf("</m:return>")>0 && response.indexOf("-instance\">")>0) {
                response = response.substring(response.indexOf("-instance\">") + 11);
                response = response.substring(0, response.indexOf("</m:return>"));
            }
            else
                response="0;Товар не Знайдено;0,00;;0;;0";
        }
        return response;
    }

    public String HTTPRequest(String parURL,String parData)
    {
        URL url;
        String response = "";
        try {
            url = new URL(parURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(parData) ;//(URLEncoder.encode(varHTTPRegest, "UTF-8"));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            HttpState = eStateHTTP.fromId(responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK ) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

}

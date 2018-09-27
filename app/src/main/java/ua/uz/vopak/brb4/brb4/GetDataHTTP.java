package ua.uz.vopak.brb4.brb4;


import android.os.AsyncTask;
import android.widget.Toast;
import android.content.Intent;

import java.io.*;
//import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.net.*;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.app.PendingIntent.getActivity;
import static android.widget.Toast.*;

public class GetDataHTTP extends AsyncTask<String, Void, String>
{
    @Override
    protected String doInBackground(String... param) {

        String res=GetData(param[0],param[1],param[2]);
        LabelInfo Lb = new LabelInfo(res);

       /* BluetoothPrinter bt=new BluetoothPrinter();
        try {
            bt.findBT();
            bt.openBT();
            String Label="^XA\n" +
                    "^LL280\n" +
                    "^FO0,12^A@N,20,20,B:904_MSSS_24.arf\n" +
                    "^FDВино \"La Famiglia\"\n" +
                    "^FS\n" +
                    "\n" +
                    "^FO0,40^A@N,20,20,B:904_MSSS_24.arf\n" +
                    "^FD Delicato біле н/сол 0.75л       \n" +
                    "^FS\n" +
                    "\n" +
                    "^FO  10,18^A@N,20,20,B:903_AB_120.arf\n" +
                    "^FD155\n" +
                    "^FS\n" +
                    "\n" +
                    "^FO335  ,51^A@N,20,20,B:901_AB_60.arf\n" +
                    "^FD37\n" +
                    "^FS\n" +
                    "\n" +
                    "^FO240,215^Ab\n" +
                    "^FD21.09.2018  \n" +
                    "^FS\n" +
                    "\n" +
                    "\n" +
                    "^FO248,240^Ab\n" +
                    "^FD00093272 \n" +
                    "^FS\n" +
                    "\n" +
                    "^FO330,140^A@N,20,20,B:904_MSSS_24.arf\n" +
                    "^FD грн/пл                                      \n" +
                    "^FS\n" +
                    "\n" +
                    "^FO0,247^A@N,20,20,B:904_MSSS_24.arf\n" +
                    "^FD-------------------------------------\n" +
                    "^FS\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "^FO340,180^BY3\n" +
                    "^BQN,2,4^FDMM,N299123456123456^FS\n" +
                    "\n" +
                    "^FO15,200^BY2\n" +
                    "^BEN,40,Y,N\n" +
                    "^FD3083680015394\n" +
                    "^FS\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "^XZ\n";
            byte[] b =Label.getBytes("Cp1251");
            bt.sendData(b);
            bt.closeBT();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }*/
        return res;

    }
    protected void onPostExecute(String result)
    {
        LabelInfo lb = new LabelInfo(result);

        /*Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                lb.Name, Toast.LENGTH_SHORT);
        toast.show();*/
    }

    public String GetData(String parCodeShop,String parScanCode,String parCode)
    {
        if(parScanCode == null || parScanCode.isEmpty())
            parScanCode="";
        if(parCode == null || parCode.isEmpty())
            parCode="";
        parCodeShop="000000009";
        parScanCode="9000100866484";
        String varHTTPRegest ="<?xml version=\"1.0\" encoding=\"utf-8\"?>"+"\n"+
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+"\n"+
                "<soap:Body><GetInfoForTheProduct xmlns=\"vopak\">"+"\n"+
                "<CodeOfShop>"+parCodeShop+"</CodeOfShop>"+"\n"+
                "<Scancode>"+parScanCode+"</Scancode>"+"\n"+
                "<CodeOfProduct>"+parCode+"</CodeOfProduct>"+"\n"+
                "</GetInfoForTheProduct>"+"\n"+
                "</soap:Body>"+"\n"+
                "</soap:Envelope>";
        String requestURL="http://1CSRV/utppsu/ws/ws1.1cws";
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

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
            writer.write(varHTTPRegest) ;//(URLEncoder.encode(varHTTPRegest, "UTF-8"));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK ) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
                response = response.substring(response.indexOf("-instance\">") + 11);
                response = response.substring(0,response.indexOf("</m:return>"));

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

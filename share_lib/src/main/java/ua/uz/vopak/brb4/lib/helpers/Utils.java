package ua.uz.vopak.brb4.lib.helpers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


import ua.uz.vopak.brb4.lib.enums.eTypeScaner;

import static android.os.Build.MANUFACTURER;
import static android.os.Build.MODEL;
import static android.os.Build.getSerial;

public class Utils {
    static final String TAG="Utils";
    private static Utils Instance = null;
    public Context vApplicationContext;
    Vibrator v;
    public Utils(){};
    public Utils(Context pApplicationContext)
    {
        this();
        vApplicationContext=pApplicationContext;
        v = (Vibrator) vApplicationContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public static Utils instance() { return Instance;}

    public static Utils instance(Context pApplicationContext) {
        if (Instance == null) {
            Instance = new Utils(pApplicationContext);
        }
        return Instance;
    }
    public boolean GetAddressReachable(String address, int port, int timeout) {
        try {

            try (Socket crunchifySocket = new Socket()) {
                // Connects this socket to the server with a specified timeout value.
                crunchifySocket.connect(new InetSocketAddress(address, port), timeout);
            }
            // Return true if connection successful
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            // Return false if connection fails
            return false;
        }
    }
    public boolean GetAddressReachable(String parHost)    {
        try {
            return InetAddress.getByName(parHost).isReachable(1000);
        }
        catch ( Exception ex)
        {
            String Res=ex.getMessage();

            return false;}
    }
    @SuppressLint("HardwareIds")
    public String GetSN() {
        try {
            if (Build.VERSION.SDK_INT > 25) {
                if (ActivityCompat.checkSelfPermission(vApplicationContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "Not PERMISSION READ_PHONE_STATE";
                }
                return getSerial();
            }
            else
              return Build.SERIAL;
        }
        catch (Exception e )
        {
            String ee=e.getMessage();
        }
        return "";

    }

    eTypeScaner GetTypeScaner() {
        if (vApplicationContext == null)
            return eTypeScaner.None;
        String model = MODEL;
        String manufacturer = MANUFACTURER;

        if (/*model.equals("TC20") &&*/ (manufacturer.contains("Zebra Technologies") || manufacturer.contains("Motorola Solutions")))
            return eTypeScaner.Zebra;
        if (model.equals("PM550") && (manufacturer.contains("POINTMOBILE") || manufacturer.contains("Point Mobile Co., Ltd.")))
            return eTypeScaner.PM550;

        return eTypeScaner.Camera;

    }

    public String GetStringFromAssetsFile(String parPath){
        String Label="";
        try {
            InputStream inputStream = vApplicationContext.getAssets().open(parPath);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
            Label=total.toString();
        }
        catch (Exception ex)
        {

        }
        return Label;
    }

    public void Vibrate(int time) {
        if(v==null)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(time);
        }
    }

    static public void SaveData(String pFileName,byte[] pData){
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, pFileName);
        try {
            FileOutputStream stream = new FileOutputStream(file, true);
            stream.write(pData);
            stream.close();
            Log.i(TAG, "Data Saved");
        } catch (IOException e) {
            Log.e(TAG, "Could not write file " + e.getMessage());
        }
    }

   static public void  WriteLog(String pText)
   {
       try {
           DateFormat df = new SimpleDateFormat("yyyyMMdd");
           Date today = Calendar.getInstance().getTime();
           String FileName = "Log_" + df.format(today) + ".txt";

           SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
           Date date = new Date(System.currentTimeMillis());
           String Text = formatter.format(date) + "Log=>\n" + pText;
           SaveData(FileName, Text.getBytes("UTF-8"));
       }
       catch(Exception e) {
           Log.e(TAG, "WriteLog=> " + e.getMessage());
       }

   }

        private  boolean isRedirected( Map<String, List<String>> header ) {
            for( String hv : header.get( null )) {
                if(   hv.contains( " 301 " )
                        || hv.contains( " 302 " )) return true;
            }
            return false;
        }

        public InputStream GetHTTP( String link)  throws Throwable
        {

            URL url  = new URL( link );
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            Map< String, List< String >> header = http.getHeaderFields();
            while( isRedirected( header )) {
                link = header.get( "Location" ).get( 0 );
                url    = new URL( link );
                http   = (HttpURLConnection)url.openConnection();
                header = http.getHeaderFields();
            }

            InputStream  input  = http.getInputStream();
            return input;
        }

        public  void GetFile( String link,String  fileName) throws Throwable
        {
            File file = new File(fileName);
            if (file.exists())
                file.delete();

            InputStream  input  =GetHTTP(link);
            byte[]       buffer = new byte[4096];
            int          n      = -1;
            OutputStream output = new FileOutputStream( new File( fileName ));
            while ((n = input.read(buffer)) != -1) {
                output.write( buffer, 0, n );
            }
            output.close();
        }

    public String isToString(InputStream is) throws Throwable {
        if(is == null)
            return null;
        final int bufferSize = 4096;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(is, "UTF-8");
        int          n      = -1;
        while ((n = in.read(buffer)) != -1) {
            out.append(buffer, 0, n);
        }
        return out.toString();
    }

    public void InstallAPK(File file,String ApplicationId) {
        try {
            if (file.exists()) {
                String[] fileNameArray = file.getName().split(Pattern.quote("."));
                if (fileNameArray[fileNameArray.length - 1].equals("apk")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                       // Uri downloaded_apk = getFileUri(vApplicationContext, file);

                        Uri downloaded_apk = FileProvider.getUriForFile(vApplicationContext, ApplicationId + ".provider", file);
                        Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(downloaded_apk,
                                "application/vnd.android.package-archive");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        vApplicationContext.startActivity(intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file),
                                "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        vApplicationContext.startActivity(intent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encoded = Files.readAllBytes(Paths.get(path));
        }
        return new String(encoded, encoding);
    }


    public String FileToString(String pFileName)
    {
        StringBuffer fileContent = new StringBuffer("");
        File file = new File(pFileName);
        if (!file.exists())
            return null;
        try(FileInputStream fis = new FileInputStream(file)) {

            byte[] buffer = new byte[4096];
            int n = -1;
            while ((n = fis.read(buffer)) != -1) {
                fileContent.append(new String(buffer, 0, n));
            }
        }
        catch(IOException ex){
            return null;
        }
        return fileContent.toString();
    }
/*
        void InstallAPK()
        {
            //get destination to update file and set Uri
            //TODO: First I wanted to store my update .apk file on internal storage for my app but apparently android does not allow you to open and install
            //aplication with existing package from there. So for me, alternative solution is Download directory in external storage. If there is better
            //solution, please inform us in comment
            String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
            String fileName = "AppName.apk";
            destination += fileName;
            final Uri uri = Uri.parse("file://" + destination);

            //Delete update file if exists
            File file = new File(destination);
            if (file.exists())
                //file.delete() - test this, I think sometimes it doesnt work
                file.delete();

            //get url of app on server
            String url = Main.this.getString(R.string.update_app_url);

            //set downloadmanager
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription(Main.this.getString(R.string.notification_description));
            request.setTitle(Main.this.getString(R.string.app_name));

            //set destination
            request.setDestinationUri(uri);

            // get download service and enqueue file
            final DownloadManager manager = (DownloadManager) vApplicationContext.getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = manager.enqueue(request);

            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    install.setDataAndType(uri,
                            manager.getMimeTypeForDownloadedFile(downloadId));
                    vApplicationContext.startActivity(install);

                    vApplicationContext.unregisterReceiver(this);
                    vApplicationContext.finish();
                }
            };
            //register receiver for when .apk download is compete
            vApplicationContext.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


        }*/



}

package ua.uz.vopak.brb4.lib.helpers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import ua.uz.vopak.brb4.lib.enums.eTypeScaner;

import static android.os.Build.MANUFACTURER;
import static android.os.Build.MODEL;
import static android.os.Build.getSerial;

public class Utils {
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




}

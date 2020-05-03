package ua.uz.vopak.brb4.lib.helpers;


import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import android.bluetooth.*;
import android.os.Handler;

import ua.uz.vopak.brb4.lib.enums.ePrinterError;
import ua.uz.vopak.brb4.lib.enums.eTypePrinter;
import ua.uz.vopak.brb4.lib.enums.TypeLanguagePrinter;

public class BluetoothPrinter {
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;
    public eTypePrinter varTypePrinter = eTypePrinter.NotDefined;
    public ePrinterError varPrinterError= ePrinterError.NotInit;
    // needed for communication to bluetooth device / network
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    AbstractConfig config;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    public BluetoothPrinter(AbstractConfig pConfig)  {config= pConfig;}

    public TypeLanguagePrinter GetTypeLanguagePrinter()    {
        //Для вилка бо не прошитий Argox потрібними шрифтами.
        if(varTypePrinter== eTypePrinter.Argox_3230 && Integer.parseInt(config.CodeWarehouse)==89)
            return  TypeLanguagePrinter.ZPL; //TypeLanguagePrinter.ZPL;
        if(varTypePrinter== eTypePrinter.Argox_3230)
            return  TypeLanguagePrinter.ZPL_ARGOX; //TypeLanguagePrinter.ZPL;
        if(varTypePrinter== eTypePrinter.Zebra_QLn320)
            return TypeLanguagePrinter.ZPL_ZEBRA;
        if(varTypePrinter== eTypePrinter.Godex_MX20)
            return TypeLanguagePrinter.EZPL;
        if(varTypePrinter== eTypePrinter.Sewoo_LK_P34)
            return TypeLanguagePrinter.ZPL_SEWOO;//CPCL_SEWOO; //ZPL_ZEBRA;//TypeLanguagePrinter.CPCL_SEWOO;
        return TypeLanguagePrinter.NotDefined;



        /*switch (varTypePrinter) {
            case TypePrinter.Argox_3230:
                return TypeLanguagePrinter.ZPL;
            case TypePrinter.Zebra_QLn320:
                return TypeLanguagePrinter.ZPL_ZEBRA;
            case TypePrinter.Godex_MX20:
                return TypeLanguagePrinter.EZPL;
            default:
                return TypeLanguagePrinter.NotDefined;

        }*/
    }

    public void findBT() {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(mBluetoothAdapter == null) {
                // myLabel.setText("No bluetooth adapter available");
                varPrinterError= ePrinterError.TurnOffBluetooth;
                return;
            }

            /*if(!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }*/


            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if(pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String name=device.getName();
                    switch (name)
                    {

                        case "Argox 3230":
                        case "00:0A:3A:32:1B:F1":
                        case "Argox AME-3230B":
                            varTypePrinter= eTypePrinter.Argox_3230;
                            break;
                        case "MX20":
                        case "Godex MX20":
                            varTypePrinter= eTypePrinter.Godex_MX20;
                            break;
                        case "LK-P34":
                            varTypePrinter= eTypePrinter.Sewoo_LK_P34;
                            break;
                        case "QLn320":
                        default:
                            varTypePrinter= eTypePrinter.Zebra_QLn320;

                    }


//                    if (device.getName().equals("RPP300")) {
                    if(varTypePrinter!= eTypePrinter.NotDefined)
                    {
                     mmDevice = device;
                     break;
                    }
                }
            }
            mBluetoothAdapter.cancelDiscovery();

            // myLabel.setText("Bluetooth device found.");

        }catch(Exception e){
            varPrinterError= ePrinterError.TurnOffBluetooth;
            e.printStackTrace();
        }
    }
    public void openBT() throws IOException {
        try {

            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            Method m = mmDevice.getClass().getMethod("createRfcommSocket",new Class[] { int.class });

            //mmSocket = (BluetoothSocket)m.invoke(mmDevice, Integer.valueOf(1));
            //Гребана магія з принтером Sewoo_LK_P34
            if(varTypePrinter== eTypePrinter.Sewoo_LK_P34)
              mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            else
              mmSocket = (BluetoothSocket)m.invoke(mmDevice, Integer.valueOf(1));

            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            //myLabel.setText("Bluetooth Opened");
            varPrinterError= ePrinterError.None;
        } catch (Exception e) {
            e.printStackTrace();
            varPrinterError= ePrinterError.CanNotOpen;
        }
    }
    /*
     * after opening a connection to bluetooth printer device,
     * we have to listen and check if a data were sent to be printed.
     */
    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = mmInputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
//                                                myLabel.setText(data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                            varPrinterError= ePrinterError.CanNotOpen;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendData(byte[] msg) throws IOException {
        try {
            if(varTypePrinter != eTypePrinter.NotDefined && varPrinterError== ePrinterError.None)
                mmOutputStream.write(msg);
            // tell the user data were sent
            //myLabel.setText("Data sent.");

        } catch (Exception e) {
            try
            {
                closeBT();
                Thread.sleep(100);
                openBT();
                Thread.sleep(100);
                mmOutputStream.write(msg);
            } catch (Exception ex) {
                varPrinterError = ePrinterError.ErrorSendData;
            }
        }
    }

    public void closeBT() throws IOException {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            //myLabel.setText("Bluetooth Closed");
        } catch (Exception e) {
            varPrinterError= ePrinterError.CanNotOpen;
            //e.printStackTrace();
        }
    }

}

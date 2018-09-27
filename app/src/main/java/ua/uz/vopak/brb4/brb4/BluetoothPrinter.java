package ua.uz.vopak.brb4.brb4;


import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import android.app.*;

import android.bluetooth.*;
import android.content.Intent;
import android.os.Handler;

public class BluetoothPrinter {
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    public TypePrinter  varTypePrinter = TypePrinter.NotDefined;
    // needed for communication to bluetooth device / network
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    public void findBT() {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(mBluetoothAdapter == null) {
                // myLabel.setText("No bluetooth adapter available");
            }
/*
            if(!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }*/


            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if(pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String name=device.getName();
                    switch (name)
                    {
                        case "ARGO 25":
                            varTypePrinter=TypePrinter.ARGO_25;
                            break;
                        case "Godex MX20":
                            varTypePrinter=TypePrinter.Godex_MX20;
                            break;
                        default:
                            varTypePrinter=TypePrinter.NotDefined;

                    }
                    varTypePrinter=TypePrinter.ARGO_25;

                    // RPP300 is the name of the bluetooth printer device
                    // we got this name from the list of paired devices
//                    if (device.getName().equals("RPP300")) {
                    mmDevice = device;
                    break;
//                    }
                }
            }

            // myLabel.setText("Bluetooth device found.");

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void openBT() throws IOException {
        try {

            // Standard SerialPortService ID
            //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            Method m = mmDevice.getClass().getMethod("createRfcommSocket",new Class[] { int.class });
            mmSocket = (BluetoothSocket)m.invoke(mmDevice, Integer.valueOf(1));
            //mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);

            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            //myLabel.setText("Bluetooth Opened");

        } catch (Exception e) {
            e.printStackTrace();
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

            mmOutputStream.write(msg);



            // tell the user data were sent
            //myLabel.setText("Data sent.");

        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

}


package ua.uz.vopak.brb4.brb4.fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;

import java.util.List;

import ua.uz.vopak.brb4.brb4.ActionType;
import ua.uz.vopak.brb4.brb4.AsyncWorker;
import ua.uz.vopak.brb4.brb4.LabelInfo;
import ua.uz.vopak.brb4.brb4.MainActivity;
import ua.uz.vopak.brb4.brb4.MessageActivity;
import ua.uz.vopak.brb4.brb4.MessageType;
import ua.uz.vopak.brb4.brb4.R;
import ua.uz.vopak.brb4.brb4.Worker;

/**
 * Created by Rishabh Bhatia on 12/5/17.
 */

public class ScanFragment extends Fragment {
    public Worker worker = new Worker(this);
    Context mcontext;
    BarcodeView barcodeView;
    private TextView codeView, textBarcodeView, perView, nameView, priceView, oldPriceView;
    private ProgressBar progresBar;
    final int PERMISSIONS_REQUEST_ACCESS_CAMERA=0;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.scan_fragment, container, false);

        if(!MainActivity.isCreatedScaner) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    getActivity().checkSelfPermission(Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_ACCESS_CAMERA);
            } else {
                barcodeView = (BarcodeView) view.findViewById(R.id.barcode_scanner);
                barcodeView.setVisibility(View.VISIBLE);
                barcodeView.decodeContinuous(callback);

            }
        }else{
            barcodeView = (BarcodeView) view.findViewById(R.id.barcode_scanner);
            barcodeView.setVisibility(View.INVISIBLE);
        }

        //Приклад відправки повідомлення користувачу
        //sendMessage("Блютуз не підключено!","StackTrace:...", MessageType.ErrorMessage);


        mcontext=getContext();

        return view;
    }



    @Override
    public void onResume() {
        super.onResume();

        barcodeView = (BarcodeView) view.findViewById(R.id.barcode_scanner);

        if(!MainActivity.isCreatedScaner) {
            barcodeView.setVisibility(View.VISIBLE);
            barcodeView.resume();
        }else{
            barcodeView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        barcodeView = (BarcodeView) view.findViewById(R.id.barcode_scanner);

        if(!MainActivity.isCreatedScaner) {
            barcodeView.setVisibility(View.VISIBLE);
            barcodeView.pause();
        }else{
            barcodeView.setVisibility(View.INVISIBLE);
        }
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                barcodeView.pause();
                SetProgres(0);
                //after the string has been read we prozess it

                //worker.execute(result);
                AsyncWorker aW =  new AsyncWorker(worker);
                aW.execute(result.getText());

                //worker.Start(result);

               /* if(!ScanText.equals("abc")){//if the tag was not scanned succesfully let us start the scan again
                    codeView.setText("code: " + ScanCode);
                    contentView.setText("content :" + ScanText);
                    //barcodeView.resume(); //notice we don't call decodeContinuous function again
                }*/
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(!MainActivity.isCreatedScaner) {
            if (requestCode == PERMISSIONS_REQUEST_ACCESS_CAMERA) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    barcodeView = (BarcodeView) view.findViewById(R.id.barcode_scanner);
                    barcodeView.setVisibility(View.VISIBLE);
                    barcodeView.decodeContinuous(callback);
                }
            }
        }else{
            barcodeView = (BarcodeView) view.findViewById(R.id.barcode_scanner);
            barcodeView.setVisibility(View.INVISIBLE);
        }
    }

    public void  setScanResult(LabelInfo LI){

        codeView = getActivity().findViewById(R.id.code);
        perView  = getActivity().findViewById(R.id.per);
        nameView  = getActivity().findViewById(R.id.title);
        oldPriceView  = getActivity().findViewById(R.id.old_price);
        priceView  = getActivity().findViewById(R.id.price);
        textBarcodeView = getActivity().findViewById(R.id.bar_code);

        codeView.setText(Integer.toString(LI.Code));
        perView.setText(LI.Unit);
        nameView.setText(LI.Name);

        TextView oldPriceText = getActivity().findViewById(R.id.old_price_text);
        TextView priceText = getActivity().findViewById(R.id.price_text);

        if(LI.OldPrice != LI.Price){
            oldPriceView.setTextColor(Color.parseColor("#ee4343"));
            priceView.setTextColor(Color.parseColor("#ee4343"));
            oldPriceText.setTextColor(Color.parseColor("#ee4343"));
            priceText.setTextColor(Color.parseColor("#ee4343"));
        }else {
            oldPriceView.setTextColor(Color.parseColor("#3bb46e"));
            priceView.setTextColor(Color.parseColor("#3bb46e"));
            oldPriceText.setTextColor(Color.parseColor("#3bb46e"));
            priceText.setTextColor(Color.parseColor("#3bb46e"));
        }

        oldPriceView.setText(String.format("%.2f",(double)LI.OldPrice/100));
        priceView.setText(String.format("%.2f",(double)LI.Price/100));
        textBarcodeView.setText(LI.BarCode);


        if(!MainActivity.isCreatedScaner) {
            barcodeView.resume();
        }
    }

    public void SetProgres(int progres){
        progresBar = getActivity().findViewById(R.id.progressBar);
        progresBar.setProgress(progres);
    }

    public  void sendMessage(String messageHeader, String message, MessageType type){

        Intent intent = new Intent(getActivity(), MessageActivity.class);
        intent.putExtra("messageHeader",messageHeader);
        intent.putExtra("message",message);
        intent.putExtra("type",type);
        startActivity(intent);

    }

    public  void sendMessage(String messageHeader, String message, MessageType type, ActionType action){

        Intent intent = new Intent(getActivity(), MessageActivity.class);
        intent.putExtra("messageHeader",messageHeader);
        intent.putExtra("message",message);
        intent.putExtra("type",type);
        intent.putExtra("action", action);
        startActivity(intent);
    }

}
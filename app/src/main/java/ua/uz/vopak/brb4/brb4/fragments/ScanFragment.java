
package ua.uz.vopak.brb4.brb4.fragments;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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

import ua.uz.vopak.brb4.brb4.LabelInfo;
import ua.uz.vopak.brb4.brb4.R;
import ua.uz.vopak.brb4.brb4.Worker;

/**
 * Created by Rishabh Bhatia on 12/5/17.
 */

public class ScanFragment extends Fragment {
    Worker worker = new Worker(this);
    Context mcontext;
    BarcodeView barcodeView;
    private TextView codeView, contentView;
    private ProgressBar progresBar;
    final int PERMISSIONS_REQUEST_ACCESS_CAMERA=0;
    View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.scan_fragment, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getActivity().checkSelfPermission(Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_ACCESS_CAMERA);
        } else {
            barcodeView = (BarcodeView) view.findViewById(R.id.barcode_scanner);
            barcodeView.decodeContinuous(callback);

        }


        mcontext=getContext();

        return view;
    }



    @Override
    public void onResume() {

        super.onResume();
        barcodeView = (BarcodeView) view.findViewById(R.id.barcode_scanner);
        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView = (BarcodeView) view.findViewById(R.id.barcode_scanner);
        barcodeView.pause();
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {

                contentView = getActivity().findViewById(R.id.scan_content);
                codeView = getActivity().findViewById(R.id.scan_format);

                barcodeView.pause();
                //after the string has been read we prozess it
                worker.Start(result);

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

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                barcodeView = (BarcodeView) view.findViewById(R.id.barcode_scanner);
                barcodeView.decodeContinuous(callback);
            }
        }
    }

    public void  setScanResult(LabelInfo LI){
        barcodeView.resume();
    }

    public void SetProgres(int progres){
        progresBar = getActivity().findViewById(R.id.progressBar);
        progresBar.setProgress(progres);
    }
}
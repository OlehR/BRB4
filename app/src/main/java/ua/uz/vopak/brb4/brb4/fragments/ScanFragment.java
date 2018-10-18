
package ua.uz.vopak.brb4.brb4.fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import java.util.List;
import ua.uz.vopak.brb4.brb4.enums.ActionType;
import ua.uz.vopak.brb4.brb4.PriceCheckerActivity;
import ua.uz.vopak.brb4.brb4.MessageActivity;
import ua.uz.vopak.brb4.brb4.enums.MessageType;
import ua.uz.vopak.brb4.brb4.R;

/**
 * Created by Rishabh Bhatia on 12/5/17.
 */

public class ScanFragment extends Fragment {
    Context mcontext;
    BarcodeView barcodeView;

    final int PERMISSIONS_REQUEST_ACCESS_CAMERA=0;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.scan_fragment, container, false);

        if(!PriceCheckerActivity.isCreatedScaner) {
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

        if(!PriceCheckerActivity.isCreatedScaner) {
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

        if(!PriceCheckerActivity.isCreatedScaner) {
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
                //after the string has been read we prozess it

                //worker.execute(result);
                //AsyncWorker aW =  new AsyncWorker(worker);
                //aW.execute(result.getText());
                ((PriceCheckerActivity)getActivity()).ExecuteWorker(result.getText());
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

        if(!PriceCheckerActivity.isCreatedScaner) {
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
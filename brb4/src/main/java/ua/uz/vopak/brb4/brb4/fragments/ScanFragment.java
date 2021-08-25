
package ua.uz.vopak.brb4.brb4.fragments;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import java.util.List;

import ua.uz.vopak.brb4.brb4.R;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.brb4.models.Config;

/**
 * Created by Rishabh Bhatia on 12/5/17.
 */

public class ScanFragment extends Fragment {
    Context mcontext;
    BarcodeView barcodeView;
    Config config = Config.instance();

    final int PERMISSIONS_REQUEST_ACCESS_CAMERA=0;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.scan_fragment, container, false);

        barcodeView = (BarcodeView) view.findViewById(R.id.barcode_scanner);

        if(config.TypeScaner==eTypeScaner.Camera) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    getActivity().checkSelfPermission(Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_ACCESS_CAMERA);
            } else {

                barcodeView.setVisibility(View.VISIBLE);
                barcodeView.decodeContinuous(callback);

            }
        }else{
            barcodeView.setVisibility(View.INVISIBLE);
        }

        //config.BarcodeImageLayout = view.findViewById(R.id.BarcodeImageLayout);

        //Приклад відправки повідомлення користувачу
        //sendMessage("Блютуз не підключено!","StackTrace:...", MessageType.ErrorMessage);

        mcontext=getContext();
        return view;
    }



    @Override
    public void onResume() {
        super.onResume();
        if(config.TypeScaner==eTypeScaner.Camera) {
            barcodeView.setVisibility(View.VISIBLE);
            barcodeView.resume();
        }else{
            barcodeView.setVisibility(View.INVISIBLE);
        }

        IntentIntegrator.forSupportFragment(this)
                .setBeepEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        if(config.TypeScaner==eTypeScaner.Camera) {
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
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 1000);
                toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP,250);
                config.Scaner.CallBack.Run(result.getText());
            }
        }
        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(config.TypeScaner==eTypeScaner.Camera) {
            if (requestCode == PERMISSIONS_REQUEST_ACCESS_CAMERA) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    barcodeView.setVisibility(View.VISIBLE);
                    barcodeView.decodeContinuous(callback);
                }
            }
        }else{
            barcodeView.setVisibility(View.INVISIBLE);
        }
    }

/*

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
    }*/

}
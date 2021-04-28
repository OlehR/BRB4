package ua.uz.vopak.brb4.brb4.models;

import android.view.View;

import com.journeyapps.barcodescanner.BarcodeView;

import ua.uz.vopak.brb4.brb4.AuthActivity;
import ua.uz.vopak.brb4.brb4.databinding.AuthLayoutBinding;
import ua.uz.vopak.brb4.brb4.helpers.AuterizationsHelper;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;

public class AuthModel {
    GlobalConfig config = GlobalConfig.instance();

    public boolean IsLoginCO = config.IsLoginCO;
    public String Login = config.Login;
    public String Password = config.Password;
    AuterizationsHelper aHelper=new AuterizationsHelper();
    AuthActivity authActivity;
    public AuthModel(AuthActivity pAuthActivity ) {authActivity=pAuthActivity;}

    public boolean IsUseCamera() {
        return IsCamera && config.IsUseCamera();
    }
    public boolean IsViewCentral() {return config.Company== eCompany.Sim23;}
    public boolean IsFlash = false;

    public void OnClickFlashLite() {
        if (authActivity.barcodeView != null)
            authActivity.barcodeView.setTorch(IsFlash);
        IsFlash = !IsFlash;
    }

    public boolean IsCamera = false;

    public void onClickCamera()
    {
        IsCamera = !IsCamera;
        authActivity.binding.invalidateAll();
        if(IsUseCamera())
            authActivity.barcodeView.resume();
    }

    public void onClickLogin()
    {
        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                aHelper.Login(authActivity,Login,Password,IsLoginCO,true);
                return null;
            }
        }).execute();
    }

    public View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean isFocused) {
            IsCamera = false;
            authActivity.binding.invalidateAll();
        }

    };

    public View.OnFocusChangeListener onFocusLostListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean isFocused) {
            if(isFocused) {
                if(IsCamera) {
                    IsCamera = false;
                    authActivity.binding.invalidateAll();
                }
            }
        }
    };

}
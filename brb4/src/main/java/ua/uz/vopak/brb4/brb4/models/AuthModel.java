package ua.uz.vopak.brb4.brb4.models;

import android.view.View;

import ua.uz.vopak.brb4.brb4.AuthActivity;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;

public class AuthModel {
    Config config = Config.instance();
    public boolean IsStarting = true;
    public boolean IsLoginCO = config.IsLoginCO;
    public String Log="Start";
    public String Login = config.Login;
    public String Password = (config.IsDebug ?config.Password:"");

    AuthActivity authActivity;
    public AuthModel(AuthActivity pAuthActivity ) {authActivity=pAuthActivity;}

    public String GetNameCompany() {return config.Company.GetName();}
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
                authActivity.aHelper.Login(authActivity,Login,Password,IsLoginCO,true);
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

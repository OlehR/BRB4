package ua.uz.vopak.brb4.brb4.models;

import androidx.databinding.ObservableField;

import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;

public class SetingModel {
    GlobalConfig config = GlobalConfig.instance();
    public ObservableField<String> apiURL= new ObservableField<>(config.ApiUrl);
    /*public void OnClickURL( SetingModel pSM ) {
        config.ApiUrl=   apiURL.get();
        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                config.Worker.AddConfigPair("ApiUrl", config.ApiUrl);
                return null;
            }
        }).execute();
    }*/
}

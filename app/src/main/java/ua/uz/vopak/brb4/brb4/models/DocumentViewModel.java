package ua.uz.vopak.brb4.brb4.models;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

public class DocumentViewModel {
    public ObservableField<String> ZKPO= new ObservableField<>("");
    public ObservableBoolean IsFilter = new ObservableBoolean(false);
    public ObservableBoolean IsEnterCodeZKPO = new ObservableBoolean(false);
    public ObservableInt TypeDoc = new ObservableInt(0);

    /* public void OnClickURL( SetingModel pSM ) {
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

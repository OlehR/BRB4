package ua.uz.vopak.brb4.brb4.models;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;

public class SetingModel {
    GlobalConfig config = GlobalConfig.instance();
    public ObservableField<String> apiURL= new ObservableField<>(config.ApiUrl);
    public ObservableArrayList<String> ListCompany= new ObservableArrayList<>();
    public ObservableInt ListCompanyIdx = new ObservableInt(0);
    public boolean IsAdmin(){return config.Login.equals("Admin");}

    public SetingModel(){
        for(eCompany el : eCompany.values()) {
            ListCompany.add(el.GetText());
        }
    }
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

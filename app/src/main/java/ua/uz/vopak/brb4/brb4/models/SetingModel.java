package ua.uz.vopak.brb4.brb4.models;

import android.text.TextUtils;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import ua.uz.vopak.brb4.brb4.helpers.Worker;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
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
        ListCompanyIdx.set(config.Company.getAction());
    }

    public void OnClickGen(){
        eCompany Company= eCompany.fromOrdinal(ListCompanyIdx.get());
        apiURL.set(Company==eCompany.SevenEleven?"http://176.241.128.13/RetailShop/hs/TSD/":"http://znp.vopak.local/api/api_v1_utf8.php");
    }

    public void OnClickSave() {
        Worker worker = config.GetWorker();
        config.Company = eCompany.fromOrdinal(ListCompanyIdx.get());
        worker.AddConfigPair("Company", Integer.toString(config.Company.getAction()));
        worker.AddConfigPair("ApiUrl", apiURL.get().trim());
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

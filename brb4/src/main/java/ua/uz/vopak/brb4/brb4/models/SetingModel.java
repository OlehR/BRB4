package ua.uz.vopak.brb4.brb4.models;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import ua.uz.vopak.brb4.brb4.Connector.SE.Connector;
import ua.uz.vopak.brb4.brb4.helpers.Worker;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;

public class SetingModel {
    GlobalConfig config = GlobalConfig.instance();
    public ObservableField<String> apiURL    = new ObservableField<>(config.ApiUrl);
    public ObservableField<String> apiURLadd = new ObservableField<>(config.ApiURLadd);
    public ObservableArrayList<String> ListCompany= new ObservableArrayList<>();
    public ObservableInt ListCompanyIdx = new ObservableInt(0);
    public boolean IsAdmin(){return config.Login.equals("Admin");}
    public ObservableInt  Progress = new ObservableInt(0);

    public SetingModel(){
        for(eCompany el : eCompany.values()) {
            ListCompany.add(el.GetText());
        }
        ListCompanyIdx.set(config.Company.getAction());
    }

    public boolean IsSevenEleven() {return config.Company==eCompany.SevenEleven;}
    public void OnClickGen(){
        eCompany Company= eCompany.fromOrdinal(ListCompanyIdx.get());
        apiURL.set(Company==eCompany.SevenEleven?"http://93.183.216.37:80/dev1/hs/TSD/":config.IsDebug? "http://195.16.78.134:7654/api/api_v1_utf8.php":"http://znp.vopak.local/api/api_v1_utf8.php");
        config.ApiUrl=apiURL.get();
        apiURLadd.set("http://93.183.216.37/copy_tk_2/hs/TSD/");
        config.ApiURLadd=apiURLadd.get();

    }

    public void OnClickSave() {
        Worker worker = config.GetWorker();
        config.Company = eCompany.fromOrdinal(ListCompanyIdx.get());
        worker.AddConfigPair("Company", Integer.toString(config.Company.getAction()));
        config.ApiUrl=apiURL.get();
        config.ApiURLadd=apiURLadd.get();
        worker.AddConfigPair("ApiUrl", apiURL.get().trim());
        worker.AddConfigPair("ApiUrladd", apiURLadd.get().trim());

    }

    public void OnClickLoad() {

            new AsyncHelper<Void>(new IAsyncHelper() {
                @Override
                public Void Invoke() {
                    config.Worker.LoadData( -1 ,null,Progress,false);
                    return null;
                }
            }).execute();

    }

    public void OnClickLoadDoc() {

            new AsyncHelper<Void>(new IAsyncHelper() {
                @Override
                public Void Invoke() {

                    config.Worker.LoadData(0,null,Progress,true);
                    return null;
                }
            }).execute();

    }
}

package ua.uz.vopak.brb4.brb4.models;

import android.os.Environment;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import java.io.File;

import ua.uz.vopak.brb4.brb4.BuildConfig;
import ua.uz.vopak.brb4.brb4.Connector.SE.Connector;
import ua.uz.vopak.brb4.brb4.helpers.Worker;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.Utils;

public class SetingModel {
    GlobalConfig config = GlobalConfig.instance();
    Worker worker = config.GetWorker();
    public ObservableField<String> apiURL    = new ObservableField<>(config.ApiUrl);
    public ObservableField<String> apiURLadd = new ObservableField<>(config.ApiURLadd);
    public ObservableArrayList<String> ListCompany= new ObservableArrayList<>();
    public ObservableInt ListCompanyIdx = new ObservableInt(0);

    public ObservableArrayList<String> ListPrinterType= new ObservableArrayList<>();
    public ObservableInt ListPrinterTypeIdx = new ObservableInt(0);

    public boolean IsAdmin(){return config.Login.equals("Admin");}
    public ObservableInt  Progress = new ObservableInt(0);

    public ObservableArrayList<String> ListWarehouse = new ObservableArrayList<>();
    public ObservableInt  ListWarehouseIdx = new ObservableInt(0);
    public Warehouse[] Warehouse;

    public SetingModel(){
        for(eCompany el : eCompany.values()) {
            ListCompany.add(el.GetText());
        }
        ListCompanyIdx.set(config.Company.getAction());

        for (eTypeUsePrinter el : eTypeUsePrinter.values()) {
            ListPrinterType.add(el.GetText());
        }
        ListPrinterTypeIdx.set(config.TypeUsePrinter.getAction());
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
        if(IsAdmin()) {

            config.Company = eCompany.fromOrdinal(ListCompanyIdx.get());
            worker.AddConfigPair("Company", Integer.toString(config.Company.getAction()));
            config.ApiUrl = apiURL.get();
            config.ApiURLadd = apiURLadd.get();
            worker.AddConfigPair("ApiUrl", apiURL.get().trim());
            worker.AddConfigPair("ApiUrladd", apiURLadd.get().trim());
        }
        else
        {
            config.CodeWarehouse = Warehouse[ListWarehouseIdx.get()].Code ;
            worker.AddConfigPair("Warehouse",Integer.toString(config.CodeWarehouse));
            config.TypeUsePrinter = eTypeUsePrinter.fromOrdinal(ListPrinterTypeIdx.get());
            config.Worker.AddConfigPair("connectionPrinterType", config.TypeUsePrinter.GetStrCode());
/*
            new AsyncHelper<Void>(new IAsyncHelper() {
                @Override
                public Void Invoke() {
                    config.Worker.AddConfigPair("Warehouse",Integer.toString(config.CodeWarehouse));
                    return null;
                }};*/
        }
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
                config.Worker.LoadData(0, null, Progress, true);
                return null;
            }
        }).execute();
    }



    public void OnClickUpdate() {
        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                try {
                    Progress.set(0);
                    String FileNameVer = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+"Ver.txt";
                    config.cUtils.GetFile("https://github.com/OlehR/BRB4/raw/master/apk/test/Ver.txt", FileNameVer );
                    String Ver= config.cUtils.FileToString(FileNameVer);
                    Progress.set(10);
                    if(Ver!=null&&Ver.length()>0) {
                        int ver=0;
                        try {
                            ver= Integer.parseInt(Ver);
                        } catch (NumberFormatException e) {

                        }
                        if(ver>BuildConfig.VERSION_CODE) {
                            String FileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + "brb4.apk";
                            config.cUtils.GetFile("https://github.com/OlehR/BRB4/raw/master/apk/test/brb4.apk", FileName);
                            Progress.set(60);
                            File file = new File(FileName);
                            config.cUtils.InstallAPK(file, BuildConfig.APPLICATION_ID);
                        }
                    }
                }
                catch (Exception e){}
                catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                Progress.set(100);
                return null;
            }
        }).execute();


    }


}

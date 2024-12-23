package ua.uz.vopak.brb4.brb4.models;

import android.os.Environment;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import ua.uz.vopak.brb4.brb4.BuildConfig;
import ua.uz.vopak.brb4.brb4.helpers.Worker;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeLog;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.Utils;

public class SetingModel {
    static final String TAG="SetingModel";
    Config config = Config.instance();
    Worker worker = config.GetWorker();

    public ObservableBoolean IsTest;
    public ObservableBoolean IsAutoLogin = new ObservableBoolean(false);
    public ObservableField<String> apiURL    = new ObservableField<>(config.ApiUrl);
    public ObservableField<String> apiURLadd = new ObservableField<>(config.ApiURLadd);
    public ObservableField<String> IdCamera = new ObservableField<>(String.valueOf( config.IdCamera));

    public ObservableArrayList<String> ListCompany= new ObservableArrayList<>();
    public ObservableInt ListCompanyIdx = new ObservableInt(0);

    public ObservableArrayList<String> ListPrinterType= new ObservableArrayList<>();
    public ObservableInt ListPrinterTypeIdx = new ObservableInt(0);

    public boolean IsAdmin(){return config.Login.equals("Admin");}
    public ObservableInt  Progress = new ObservableInt(0);

    public ObservableArrayList<String> ListWarehouse = new ObservableArrayList<>();
    public ObservableInt  ListWarehouseIdx = new ObservableInt(0);
    public Warehouse[] Warehouse;

    public ObservableArrayList<String> ListLog = new ObservableArrayList<>( );
    public ObservableInt  ListLogIdx = new ObservableInt(0);

    public boolean IsUseCamera() {return config.IsUseCamera();}
    public SetingModel(){
        for(eCompany el : eCompany.values()) {
            ListCompany.add(el.GetText());
        }
        ListCompanyIdx.set(config.Company.getAction());

        for (eTypeUsePrinter el : eTypeUsePrinter.values()) {
            ListPrinterType.add(el.GetText());
        }

        for(eTypeLog el : eTypeLog.values()) {
            ListLog.add(el.GetText());
        }
        ListPrinterTypeIdx.set(config.TypeUsePrinter.getAction());
        IsTest = new ObservableBoolean(config.IsTest);
        IsAutoLogin = new ObservableBoolean(config.IsAutoLogin);
        ListLogIdx.set(config.cUtils.TypeLog.getTypeLog());
    }

    public boolean IsSevenEleven() {return config.Company==eCompany.Sim23;}

    public void OnClickIP() {
        try {

            int i=worker.FindWhIP(Warehouse);
            if(i>=0)
             {
                    ListWarehouseIdx.set(i);
                    apiURL.set(Warehouse[i].Url);
                    OnClickSave();
            }
        } catch (Exception e) {
            Utils.WriteLog("e",TAG, "OnClickIP=>" , e);
        }

    }
    public void OnClickGen(){
        eCompany Company= eCompany.fromOrdinal(ListCompanyIdx.get());
        String url=null;
        if(Warehouse!=null && Warehouse.length>ListWarehouseIdx.get())
            url=Warehouse[ListWarehouseIdx.get()].Url;

        apiURL.set(Company==eCompany.Sim23 ? (url!=null && url.length()>0? url : "http://93.183.216.37:80/dev1/hs/TSD/"):
                "http://api.spar.uz.ua/znp/");
        config.ApiUrl=apiURL.get();
        apiURLadd.set(Company==eCompany.Sim23 ? "http://93.183.216.37/copy_tk/hs/TSD/"//http://qlik.sim23.ua/TK/hs/TSD/;http://vpn.sim23.ua/TK/hs/TSD/"//;http://music.sim23.ua/TK/hs/TSD/"//"http://160.238.125.158/TK/hs/TSD/;http://37.53.84.148/TK/hs/TSD/"
                :"http://api.spar.uz.ua/print/;http://znp.vopak.local:8088/Print");
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
            config.IsTest=IsTest.get();
            worker.AddConfigPair("IsTest",config.IsTest?"true":"false");
            config.IsAutoLogin=IsAutoLogin.get();
            worker.AddConfigPair("IsAutoLogin",config.IsAutoLogin?"true":"false");
            GetDataHTTP.instance().Init(new String[]{config.ApiUrl,config.ApiURLadd,config.ApiUrl3});
        }
        else
        {
            config.TypeUsePrinter = eTypeUsePrinter.fromOrdinal(ListPrinterTypeIdx.get());
            config.Worker.AddConfigPair("connectionPrinterType", config.TypeUsePrinter.GetStrCode());
        }
        if(Warehouse!=null && ListWarehouseIdx.get()<Warehouse.length) {
            config.CodeWarehouse = Warehouse[ListWarehouseIdx.get()].Code;
            worker.AddConfigPair("Warehouse", Integer.toString(config.CodeWarehouse));
        }
        config.IdCamera= Integer.valueOf( IdCamera.get());
        config.cUtils.TypeLog = eTypeLog.fromOrdinal(ListLogIdx.get());

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
                config.cUtils.UpdateAPK("https://github.com/OlehR/BRB4/raw/master/apk/"+(config.IsTest?"test":"work")+"/","brb4.apk",Progress,BuildConfig.VERSION_CODE,BuildConfig.APPLICATION_ID);
               /* try {
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
                Progress.set(100);*/
                return null;
            }
        }).execute();
    }

    public void OnCopyDB() {

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
             String pathDb =config.context.getApplicationInfo().dataDir + "/databases/"+"brb4.db";
             String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/brb_copy.db";
                config.cUtils.CopyFile(pathDb,path);
                return null;
            }
        }).execute();
    }

    public void OnRestoreDB() {

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                String path=null;
                try {
                    config.SQLiteAdapter.close();
                    String pathDb = config.context.getApplicationInfo().dataDir + "/databases/" + "brb4.db";
                    path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/brb4.db";
                    config.cUtils.CopyFile(path, pathDb);

                }
                catch (Exception e) {
                    Utils.WriteLog("e",TAG, "OnRestoreDB >>"+ e.toString() + " "+path);
                }
                return null;
            }
        }).execute();
    }

}

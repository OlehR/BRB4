package ua.uz.vopak.brb4.brb4.models;
import android.content.Context;
import android.content.Intent;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableInt;

import ua.uz.vopak.brb4.brb4.Connector.Connector;
import ua.uz.vopak.brb4.brb4.DocumentActivity;
import ua.uz.vopak.brb4.brb4.DocumentItemsActivity;
import ua.uz.vopak.brb4.brb4.NewDocumentActivity;
import ua.uz.vopak.brb4.brb4.helpers.Worker;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.UtilsUI;
import ua.uz.vopak.brb4.lib.models.Result;

public class NewDocModel {
    Config C= Config.instance();
    Worker Worker  = new Worker();
    Connector c = Connector.instance();
    public ObservableArrayList<String> ListWarehouse = new ObservableArrayList<>();
    public ObservableInt  ListWarehouseIdx = new ObservableInt(0);

    public String WarehouseFrom,WarehouseTo="";
    //public Warehouse[] Warehouses;
    NewDocumentActivity NDA;
    Warehouse WhFrom;
    public NewDocModel(NewDocumentActivity pNDA)
    {
        NDA=pNDA;
        WhFrom=C.GetWarehouse(C.CodeWarehouse);
        WarehouseFrom=WhFrom.Name;
        if(C.Warehouses!=null)
            for (Warehouse el:C.Warehouses) {
                ListWarehouse.add(el.Name);
            }
    }

    public void OnClickCreate() {
        new AsyncHelper<Void>(new IAsyncHelper<Void>() {
            @Override
            public Void Invoke() {
               Result Res = c.CreateNewDoc(NDA.DocumentType, Integer.parseInt( WhFrom.Number) ,  Integer.parseInt(C.Warehouses[ListWarehouseIdx.get()].Number));
               if(Res!=null && Res.State==0) {
                   c.LoadDocsData(NDA.DocumentType,"",null,false);
                   NDA.Exit(Res.Info);
               }
               else {
                   NDA.runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           new UtilsUI(NDA).Dialog("Документ не створено", Res==null?"Відсутня відповідь":Res.TextError);
                       }
                   });

               }
               return null;
               }
            }
        ).execute();

    }
}
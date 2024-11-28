package ua.uz.vopak.brb4.brb4.models;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeCreate;

public class DocumentViewModel {

    public DocSetting DS;
    public DocumentViewModel(DocSetting pDS)
    {
        Config config = Config.instance();
        DS=pDS;
        IsCreateNewDoc.set(DS.TypeCreateNewDoc != eTypeCreate.None);
        IsEnableCodeZKPO.set((config.Company== eCompany.Sim23 && DS.TypeDoc==2) ||(config.Company== eCompany.Sim23 &&( DS.TypeDoc==5||DS.TypeDoc==1 )));
    }
    public ObservableField<String> ZKPO= new ObservableField<>("");
    public ObservableBoolean IsFilter = new ObservableBoolean(false);
    public ObservableBoolean IsEnterCodeZKPO = new ObservableBoolean(true);
    public ObservableBoolean IsCreateNewDoc = new ObservableBoolean(false);
    public ObservableBoolean IsEnableCodeZKPO = new ObservableBoolean(false);

}

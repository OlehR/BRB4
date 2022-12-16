package ua.uz.vopak.brb4.brb4.models;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

public class DocumentViewModel {

    public DocSetting DS;
    public DocumentViewModel(DocSetting pDS)
    {
        DS=pDS;
        IsCreateNewDoc.set(DS.IsCreateNewDoc);
        IsEnterCodeZKPO.set(DS.TypeDoc==2);
    }
    public ObservableField<String> ZKPO= new ObservableField<>("");
    public ObservableBoolean IsFilter = new ObservableBoolean(false);
    public ObservableBoolean IsEnterCodeZKPO = new ObservableBoolean(false);
    public ObservableBoolean IsCreateNewDoc = new ObservableBoolean(false);

}

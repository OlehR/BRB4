package ua.uz.vopak.brb4.brb4.models;

import java.util.ArrayList;
import java.util.List;

public class DocModel extends Doc {
    public List<WaresItemModel> WaresItem = new ArrayList<>();

    public DocModel(int pTypeDoc, String pNumberDoc)  { super(pTypeDoc,pNumberDoc);}


}

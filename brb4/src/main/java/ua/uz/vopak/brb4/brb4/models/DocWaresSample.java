package ua.uz.vopak.brb4.brb4.models;

public class DocWaresSample {
    public int TypeDoc;//Тип документа (1-ревізія 2-приходи тощо)
    public String NumberDoc;//  (Номер документа в 1С)
    public int OrderDoc; //порядок по порядку в документі
    public int CodeWares;
    public Double  Quantity; // планова кількість для приходу(замовлення)
    public Double  QuantityMin; //не використовується
    public Double  QuantityMax; // Максимальна кількість товару в документі, контролюється якщо IsControl=1
    public String Name; // Назва ОЗ(Основного Засобу)
    public String BarCode; //Штрихкод ОЗ
}

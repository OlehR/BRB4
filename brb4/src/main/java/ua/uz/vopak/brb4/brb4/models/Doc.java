package ua.uz.vopak.brb4.brb4.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//Документ
public class Doc {
   public int TypeDoc;//Тип документа (1-ревізія 2-приходи тощо)
   public String NumberDoc;//  (Номер документа в 1С)
   public String DateDoc; // Дата документа
   public int IsControl; // 1- Якщо треба контролювати асортимент та кількість, для замовлень та можливо інших документів.
   public String ExtInfo; // Додаткова інформація, яка може вплинути на обробку документа
   public String NameUser; // користувач який створив документ рядок
   public String BarCode;//штрихкод документа, якщо є для швидкого пошуку
   public String  Description; // (опис для ревізій коментар, якщо використовують для приходів назва контрагента)
   public String  NumberDoc1C; // Для замовлень номер прихідної, якщо створено.
   public String DateOutInvoice; // Дата розхідної накладної для приходу
   public String NumberOutInvoice; // Номер розхідної накладної для приходу

   //public int isClose; //0- не закривати, 1 - закривати.
   public Doc(){}
   public Doc(int pTypeDoc, String pNumberDoc)
   {
      TypeDoc=pTypeDoc;
      NumberDoc= pNumberDoc;
   }


   public Date GetDateOutInvoice()
   {
      SimpleDateFormat formatterDate= new SimpleDateFormat("yyyy-MM-dd");
      Date DateOut=Calendar.getInstance().getTime();
       try {
          DateOut = formatterDate.parse(  DateOutInvoice);
                }catch (Exception e)
                {
                   try{DateOut= formatterDate.parse(formatterDate.format(DateOut));}catch (Exception ee){}
                }
      return DateOut;
   }
}

package ua.uz.vopak.brb4.brb4.models;

import java.util.Date;

//Документ
public class Doc {
   public int TypeDoc;//Тип документа (1-ревізія 2-приходи тощо)
   public String NumberDoc;//  (Номер документа в 1С)
   public Date DateDoc; // Дата документа
   public int IsControl; // 1- Якщо треба контролювати асортимент та кількість, для замовлень та можливо інших документів.
   public String ExtInfo; // Додаткова інформація, яка може вплинути на обробку документа
   public String NameUser; // користувач який створив документ рядок
   public String BarCode;//штрихкод документа, якщо є для швидкого пошуку
   public String  Description; // (опис для ревізій коментар, якщо використовують для приходів назва контрагента)
   public String  NumberDoc1C; // Для замовлень номер прихідної, якщо створено.
}

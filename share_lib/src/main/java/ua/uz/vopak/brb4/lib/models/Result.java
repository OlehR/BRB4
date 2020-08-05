package ua.uz.vopak.brb4.lib.models;

public class Result {
   public int State=0;
   public String TextError="Ok";
   public String Info="";
   public Result(){};
   public Result(int pState, String pTextError)
   {
       State=pState;
       TextError=pTextError;
   }
}

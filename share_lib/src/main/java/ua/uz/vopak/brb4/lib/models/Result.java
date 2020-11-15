package ua.uz.vopak.brb4.lib.models;

import ua.uz.vopak.brb4.lib.enums.eStateHTTP;

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
    public Result(HttpResult httpResult)
    {
        if(httpResult.HttpState== eStateHTTP.HTTP_OK)
        {
            Info=httpResult.Result;
        }
        else
        {
            State=-1;
            TextError=httpResult.HttpState.toString();
        }

    }
}

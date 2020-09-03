package ua.uz.vopak.brb4.brb4.models;

public class Warehouse {
    public int Code;
    public String Number;
    public String Name;
    public String Url;
    public String InternalIP;
    public String ExternalIP;
    public Warehouse(){};
    public Warehouse( int pCode, String pNumber, String pName, String pUrl, String pInternalIP, String pExternalIP)
    {
      Code=pCode;
      Number=pNumber;
      Name=pName;
      Url=pUrl;
      InternalIP=pInternalIP;
      ExternalIP=pExternalIP;
    };
}

package ua.uz.vopak.brb4.lib.enums;

public enum eTypeLog {
    NoLog(0),
    Short(1),
    Full(2);

    private static eTypeLog[] allValues = values();
    public static eTypeLog fromOrdinal(int n) {return allValues[n];}

    private int TypeLog;
    // getter method

    public static eTypeLog[] getAllValues() {
        return allValues;
    }

    public int getTypeLog()
    {
        return this.TypeLog;
    }

    // enum constructor - cannot be public or protected
    private eTypeLog(int pTypeLog)
    {
        this.TypeLog = pTypeLog;
    }

    public String GetText() {
        switch(TypeLog) {
            case 1: return "Short";
            case 2: return "Full";
            default: return "NotLog";
        }
    }

}

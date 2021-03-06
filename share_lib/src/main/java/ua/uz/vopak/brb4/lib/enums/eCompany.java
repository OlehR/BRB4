package ua.uz.vopak.brb4.lib.enums;

public enum eCompany {
    NotDefined(0),
    VopakPSU(1),
    SparPSU(2),
    Sim23(3);
    private static eCompany[] allValues = values();
    public static eCompany fromOrdinal(int n) {return allValues[n];}

    private int action;
    // getter method
    public int getAction()
    {
        return this.action;
    }

    // enum constructor - cannot be public or protected
    private eCompany(int action)
    {
        this.action = action;
    }

    public String GetText() {
        switch(action) {
            case 0: return "NotDefined";
            case 1: return "VopakPSU";
            case 2: return "SparPSU";
            case 3: return "Sim23";
            default: return "NotDefined";
        }
    }
    public String GetName() {
        switch(action) {
            case 1: return "Вопак";
            case 2: return "Spar";
            case 3: return "Сім23";
            default: return "Невідомий";
        }
    }

}

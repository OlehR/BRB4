package ua.uz.vopak.brb4.lib.enums;

public enum eCompany {
    NotDefined(0),
    VopakPSU(1),
    SparPSU(2),
    SevenEleven(3);
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
}

package ua.uz.vopak.brb4.lib.enums;

public enum eRole {
    NotDefined(0),
    Admin(1),
    User(2),
    Auditor(3);

    private static eRole[] allValues = values();
    public static eRole fromOrdinal(int n) {return allValues[n];}
    //public static eRole fromOrdinal(String n) {return allValues[Integer.valueOf(n) ];}
    private int action;

    // getter method
    public int getAction()
    {
        return this.action;
    }

    // enum constructor - cannot be public or protected
    private eRole(int action)
    {
        this.action = action;
    }
    public String GetStrCode(){return Integer.toString(this.action);}

    public String GetText() {
        switch(action) {
            case 1: return "Адміністратор";
            case 2: return "Кортстувач";
            case 3: return "Ревізор";
            default: return "Невідомий профайл";
        }
    }
}

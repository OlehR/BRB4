package ua.uz.vopak.brb4.lib.enums;

public enum eTypeUsePrinter {
    NotDefined(0),
    OnlyStart(1),
    AutoConnect(2),
    StationaryWithCut(3),
    StationaryWithCutAuto(4);
    // declaring private variable for getting values

    private static eTypeUsePrinter[] allValues = values();
    public static eTypeUsePrinter fromOrdinal(int n) {return allValues[n];}
    public static eTypeUsePrinter fromOrdinal(String n) {return allValues[Integer.valueOf(n) ];}
    private int action;

    // getter method
    public int getAction()
    {
        return this.action;
    }

    // enum constructor - cannot be public or protected
    private eTypeUsePrinter(int action)
    {
        this.action = action;
    }
    public String GetStrCode(){return Integer.toString(this.action);}

    public String GetText() {
        switch(action) {
            case 0: return "Без Принтера";
            case 1: return "Тільки при вході";
            case 2: return "Авто підключення";
            case 3: return "Стаціонарний з обрізжчиком";
            case 4: return "Стаціонарний з обрізжчиком (автовибір)";
            default: return "Невідоме значення";
        }
    }
    }

package ua.uz.vopak.brb4.lib.enums;

public enum eTypeDocWeight {
    Both(0),
    NoWeight(1),
    Weight(2);
    private int action;

    public int getAction()
    {
        return this.action;
    }

    private eTypeDocWeight(int action)
    {
        this.action = action;
    }
    public static eTypeDocWeight fromId(int id) {
        for (eTypeDocWeight type : values()) {
            if (type.getAction() == id) {
                return type;
            }
        }
        return null;
    }

}

package ua.uz.vopak.brb4.brb4.helpers;

import java.util.Map;

public class HashMapHelper {
    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }
}

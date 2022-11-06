package com.vaccines.areas;

public enum CountyType {
    Grodzki, Ziemski;
    public static CountyType parseType(String type) {
        if (type.toLowerCase().equals("grodzki")) {
            return Grodzki;
        }
        else {
            return Ziemski;
        }
    }
}

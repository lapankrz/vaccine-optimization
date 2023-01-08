package com.vaccines.areas;

public enum PowiatType {
    Grodzki, Ziemski;
    public static PowiatType parseType(String type) {
        if (type.toLowerCase().equals("grodzki")) {
            return Grodzki;
        }
        else {
            return Ziemski;
        }
    }
}

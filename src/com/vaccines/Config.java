package com.vaccines;

public final class Config {

    public static double naturalDeathRate = 0.01 / 365;
    public static double susceptibleTransmissionRate = 0.2;
    public static double vaccinatedTransmissionRate = 0.1;
    public static double vaccineImmunizationRate = 1.0 / 14;
    public static double recoveryRate = 1.0 / 21;

    private Config() { }
}

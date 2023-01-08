package com.vaccines;

public final class Config {

    public static double maxVaccinationPercentage = 0.6;

    // SVIR
    public static double susceptibleTransmissionRate = 0.16;
    public static double vaccinatedTransmissionRate = 0.08;
    public static double vaccineImmunizationRate = 1.0 / 14;
    public static double recoveryRate = 1.0 / 14;

    // SVEIR
    public static double contactRate = 0.514;
    public static double exposedInfectiousness = 0.25;
    public static double infectedInfectiousness = 1.0;
    public static double vaccinationInfectionChanceReduction = 0.9;
    public static double meanDurationOfLatency = 2.0;
    public static double recoveryRateOfLatents = 1.857 / 1000;
    public static double meanInfectedRecoveryRate = 0.2;

    private Config() { }
}

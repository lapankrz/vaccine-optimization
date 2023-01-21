package com.vaccines;

public final class Config {

    public static int SIMULATION_LENGTH = 182; // długość symulacji w dniach (26 tygodni - pół roku)
    public static int MAX_WEEKLY_VACCINES = 600000; // maksymalna tygodniowa liczba szczepień
    public static String ALGORITHM_NAME = "OMOPSO"; // algorytm optymalizacji
    public static int MAX_EVALUATIONS = 1000000;
    public static int NUMBER_OF_SEEDS = 5;
    public static double maxVaccinationPercentage = 0.6;
    public static int biggestCitiesThreshold = 300000;

    // SVIR
    public static double susceptibleTransmissionRate = 0.2;
    public static double vaccinatedTransmissionRate = 0.1;
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

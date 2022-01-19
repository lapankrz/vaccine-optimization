package com.vaccines;

import java.io.FileWriter;

public class SVIR {
    int simulationStep = 0;
    int susceptible, vaccinated, infected, recovered;
    int[] timeseriesS, timeseriesV, timeseriesI, timeseriesR;
    double naturalDeathRate = 0.01 / 365;
    double susceptibleTransmissionRate = 0.011, vaccinatedTransmissionRate = 0.011;
    int[] vaccineAvailability; // number of vaccines available in each week of simulation
    double vaccineImmunizationRate = 1.0 / 14;
    double recoveryRate = 1.0 / 14;

    int simulationLength;

    public SVIR(int simulationLength) {
        this.simulationLength = simulationLength;
        vaccineAvailability = new int[getNumberOfWeeks()];
    }

    public void setPopulations(int S, int V, int I, int R) {
        susceptible = S;
        vaccinated = V;
        infected = I;
        recovered = R;
    }

    public void setVaccineAvailability(int[] availability)
    {
        vaccineAvailability = availability;
    }

    public void setParameters(double naturalDeathRate, double susceptibleTransmissionRate,
                              double vaccinatedTransmissionRate, double vaccineImmunizationRate, double recoveryRate) {
        this.naturalDeathRate = naturalDeathRate;
        this.susceptibleTransmissionRate = susceptibleTransmissionRate;
        this.vaccinatedTransmissionRate = vaccinatedTransmissionRate;
        this.vaccineImmunizationRate = vaccineImmunizationRate;
        this.recoveryRate = recoveryRate;
    }

    protected void simulateStep() {
        susceptible += calculateSusceptibleDiff();
        vaccinated += calculateVaccinatedDiff();
        infected += calculateInfectedDiff();
        recovered += calculateRecoveredDiff();
    }

    public void simulate() {
        timeseriesS = new int[simulationLength];
        timeseriesV = new int[simulationLength];
        timeseriesI = new int[simulationLength];
        timeseriesR = new int[simulationLength];
        for (simulationStep = 0; simulationStep < simulationLength; ++simulationStep)
        {
            simulateStep();
            timeseriesS[simulationStep] = susceptible;
            timeseriesV[simulationStep] = vaccinated;
            timeseriesI[simulationStep] = infected;
            timeseriesR[simulationStep] = recovered;
        }
    }

    public int[][] getResults() {
        return new int[][] { timeseriesS, timeseriesV, timeseriesI, timeseriesR };
    }

    public void saveResultsToFile() {
        int[][] results = getResults();
        try {
            FileWriter writer = new FileWriter("svir_data.csv");
            for (int[] arr: results)
            {
                for (int i = 0; i < arr.length; ++i) {
                    writer.write(String.valueOf(arr[i]));
                    if (i != arr.length - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }
            writer.close();
        } catch (Exception ignored) {

        }
    }

    private int calculateSusceptibleDiff() {
        return -(int)(susceptibleTransmissionRate * susceptible * infected
                + naturalDeathRate * susceptible
                + getVaccinationsForToday());
    }

    private int calculateVaccinatedDiff() {
        return (int)(getVaccinationsForToday()
                - vaccinatedTransmissionRate * vaccinated * infected
                - (vaccineImmunizationRate + naturalDeathRate) * vaccinated);
    }

    private int calculateInfectedDiff() {
        return (int)((susceptibleTransmissionRate * susceptible
                + vaccinatedTransmissionRate * vaccinated) * infected
                - (recoveryRate + naturalDeathRate) * infected);
    }

    private int calculateRecoveredDiff() {
        return (int)(vaccineImmunizationRate * vaccinated
                + recoveryRate * infected
                - naturalDeathRate * recovered);
    }

    private int getVaccinationsForToday() {
        int week = getSimulationWeek();
        return vaccineAvailability[week] / 7;
    }

    private int getSimulationWeek()
    {
        return simulationStep / 7;
    }

    private int getNumberOfWeeks()
    {
        return (int)Math.ceil((double)simulationLength / 7.0);
    }
}

package com.vaccines;

public class SVEIR {
    int simulationStep = 0;
    int susceptible, vaccinated, exposed, infected, recovered;
    int[] timeseriesS, timeseriesV, timeseriesE, timeseriesI, timeseriesR;

    double interactionRate;
    double exposedTransmissionRate = 0.011;
    double infectedTransmissionRate = 0.011;
    double vaccinatedTransmissionRate = 0.011;
    double incubationRate;
    double recoveryRate;
    double naturalDeathRate = 0.01 / 365;
    double exposedImmunityPercentage;
    double covidDeathRate;
    int[] vaccineAvailability; // number of vaccines available in each week of simulation

    int simulationLength;

    public SVEIR(int simulationLength) {
        this.simulationLength = simulationLength;
        vaccineAvailability = new int[getNumberOfWeeks()];
    }

    public void setPopulations(int S, int V, int E, int I, int R) {
        susceptible = S;
        vaccinated = V;
        exposed = E;
        infected = I;
        recovered = R;
    }

    public void setVaccineAvailability(int[] availability)
    {
        vaccineAvailability = availability;
    }

    protected void simulateStep() {
        susceptible += calculateSusceptibleDiff();
        vaccinated += calculateVaccinatedDiff();
        exposed += calculateExposedDiff();
        infected += calculateInfectedDiff();
        recovered += calculateRecoveredDiff();
    }

    public void simulate() {
        timeseriesS = new int[simulationLength];
        timeseriesV = new int[simulationLength];
        timeseriesE = new int[simulationLength];
        timeseriesI = new int[simulationLength];
        timeseriesR = new int[simulationLength];
        for (simulationStep = 0; simulationStep < simulationLength; ++simulationStep)
        {
            simulateStep();
            timeseriesS[simulationStep] = susceptible;
            timeseriesV[simulationStep] = vaccinated;
            timeseriesE[simulationStep] = exposed;
            timeseriesI[simulationStep] = infected;
            timeseriesR[simulationStep] = recovered;
        }
    }

    public int[][] getResults() {
        return new int[][] { timeseriesS, timeseriesV, timeseriesE, timeseriesI, timeseriesR };
    }

    private int calculateSusceptibleDiff() {
        int pop = getTotalPopulation();
        return (int)(-interactionRate * exposedTransmissionRate * exposed * susceptible / pop
                     -interactionRate * infectedTransmissionRate * infected * susceptible / pop
                     -getVaccinationsForToday() - naturalDeathRate * susceptible);
    }

    private int calculateVaccinatedDiff() {
        int pop = getTotalPopulation();
        return (int)(-interactionRate * exposedTransmissionRate * vaccinatedTransmissionRate * exposed * vaccinated / pop
                     -interactionRate * infectedTransmissionRate * vaccinatedTransmissionRate * infected * vaccinated / pop
                     -naturalDeathRate * vaccinated + getVaccinationsForToday());
    }

    private int calculateExposedDiff() {
        int pop = getTotalPopulation();
        return (int)(interactionRate * exposedTransmissionRate * exposed * susceptible / pop
                   + interactionRate * infectedTransmissionRate * infected * susceptible / pop
                   + interactionRate * exposedTransmissionRate * vaccinatedTransmissionRate * exposed * vaccinated / pop
                   + interactionRate * infectedTransmissionRate * vaccinatedTransmissionRate * infected * vaccinated / pop
                   - (naturalDeathRate + exposedImmunityPercentage + incubationRate) * exposed);
    }

    private int calculateInfectedDiff() {
        return (int)(incubationRate * exposed - (naturalDeathRate + covidDeathRate + recoveryRate) * infected);
    }

    private int calculateRecoveredDiff() {
        return (int)(exposedImmunityPercentage * exposed + recoveryRate * infected - naturalDeathRate * recovered);
    }

    private int getTotalPopulation() {
        return susceptible + vaccinated + exposed + infected + recovered;
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

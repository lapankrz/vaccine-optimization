package com.vaccines.models;

import com.vaccines.areas.Country;
import com.vaccines.evaluations.Evaluation;

import java.util.ArrayList;

public class EpidemiologicalModel {
    public Country country;
    public ModelType type;
    int simulationStep = 0;
    int simulationLength;
    int[][] vaccineAvailability; // number of vaccines available in each week of simulation
    public Evaluation evaluation = new Evaluation();
    public final float STARTING_INFECTED = 4835;
    public double[] infections;
    public double[] mostConcurrent;
    public double[] deaths;

    public EpidemiologicalModel(int simulationLength, ModelType type) {
        this.simulationLength = simulationLength;
        this.type = type;
        country = new Country();
        country.loadPolishData(type);
        vaccineAvailability = new int[getNumberOfWeeks()][getLowestDivisionCount()];
    }

    public EpidemiologicalModel(EpidemiologicalModel model) {
        simulationLength = model.simulationLength;
        type = model.type;
        country = new Country(model.country);
        vaccineAvailability = new int[getNumberOfWeeks()][getLowestDivisionCount()];

        simulationStep = 0;
        evaluation = new Evaluation();
    }

    public void simulate() {
        evaluation.infectedSum = STARTING_INFECTED;
        infections = new double[simulationLength];
        mostConcurrent = new double[simulationLength];
        deaths = new double[simulationLength];
        for (simulationStep = 0; simulationStep < simulationLength; ++simulationStep)
        {
            simulateStep();
        }
    }

    protected void simulateStep() {
        int[] vaccinesForCurrentWeek = getVaccinationsForCurrentWeek();
        Evaluation eval = country.simulateStep(vaccinesForCurrentWeek);

        evaluation.infectedSum += eval.infectedSum;
        evaluation.deadSum += eval.deadSum;
        if (eval.mostConcurrentInfected > evaluation.mostConcurrentInfected)
            evaluation.mostConcurrentInfected = eval.mostConcurrentInfected;

        infections[simulationStep] = evaluation.infectedSum;
        mostConcurrent[simulationStep] = evaluation.mostConcurrentInfected;
        deaths[simulationStep] = evaluation.deadSum;
        country.applyChanges();
    }

    public void setVaccineAvailability(int[][] availability)
    {
        vaccineAvailability = availability;
    }
    public void setVaccineAvailability(ArrayList<ArrayList<Double>> availability)
    {
        vaccineAvailability = new int[getNumberOfWeeks()][getLowestDivisionCount()];
        for (var week : availability) {
            for (double d : week) {

            }
        }
        for (int i = 0; i < getNumberOfWeeks(); ++i) {
            for (int j = 0; j < getLowestDivisionCount(); ++j) {
                vaccineAvailability[i][j] = availability.get(i).get(j).intValue();
            }
        }
    }

    private int getVaccinationsForToday(int subdivisionNo) {
        int week = getSimulationWeek();
        return vaccineAvailability[week][subdivisionNo] / 7;
    }

    protected int[] getVaccinationsForCurrentWeek() {
        int week = getSimulationWeek();
        return vaccineAvailability[week];
    }

    protected int getSimulationWeek()
    {
        return simulationStep / 7;
    }

    public int getNumberOfWeeks()
    {
        return (int)Math.ceil((double)simulationLength / 7.0);
    }

    public int getLowestDivisionCount() {
        return country.powiaty.size();
    }
}

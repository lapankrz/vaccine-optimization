package com.vaccines.models;

import com.vaccines.areas.AdminDivision;
import com.vaccines.areas.Country;
import com.vaccines.evaluations.Evaluation;

public abstract class EpidemiologicalModel {
    Country country;
    ModelType type;
    int simulationStep = 0;
    int simulationLength;
    int administrativeLevel = 3;
    int[][] vaccineAvailability; // number of vaccines available in each week of simulation
    public Evaluation evaluation = new Evaluation();

    // administrative level: 3 - simulate communes, 2 - simulate counties, 1 - simulate voivodeships, 0 - simulate country
    public EpidemiologicalModel(int simulationLength, int administrativeLevel) {
        this.simulationLength = simulationLength;
        this.administrativeLevel = administrativeLevel;
        country = new Country();
    }

    public EpidemiologicalModel(int simulationLength) {
        this.simulationLength = simulationLength;
        country = new Country();
    }

    protected abstract void simulateStep();

    public void setVaccineAvailability(int[][] availability)
    {
        vaccineAvailability = availability;
    }

    private int getVaccinationsForToday(int subdivisionNo) {
        int week = getSimulationWeek();
        return vaccineAvailability[week][subdivisionNo] / 7;
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
        return country.getAllDivisionsOnLevel(administrativeLevel).size();
    }
}

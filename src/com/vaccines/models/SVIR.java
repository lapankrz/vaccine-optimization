package com.vaccines.models;

import com.vaccines.areas.AdminDivision;
import com.vaccines.areas.Country;
import com.vaccines.evaluations.Evaluation;
import com.vaccines.populations.SVIRPopulation;
import java.io.FileWriter;

public class SVIR extends EpidemiologicalModel {
//    int[] timeSeriesS, timeSeriesV, timeSeriesI, timeSeriesR;
    public Evaluation evaluation = new Evaluation();

    public SVIR(int simulationLength, int administrativeLevel) {
        super(simulationLength, administrativeLevel);
        type = ModelType.SVIR;
        vaccineAvailability = new int[getNumberOfWeeks()][getLowestDivisionCount()];
        country.loadPolishData(type);
    }

    public SVIR(int simulationLength) {
        super(simulationLength);
        type = ModelType.SVIR;
        vaccineAvailability = new int[getNumberOfWeeks()][getLowestDivisionCount()];
        country.loadPolishData(type);
    }

    public SVIR(SVIR model) {
        super(model.simulationLength, model.administrativeLevel);
        type = ModelType.SVIR;
        vaccineAvailability = new int[getNumberOfWeeks()][getLowestDivisionCount()];
        country = new Country(model.country);
    }

    public void setVaccineAvailability(int[][] availability)
    {
        vaccineAvailability = availability;
    }

    @Override
    protected void simulateStep() {
        int[] vaccinesForCurrentWeek = getVaccinationsForCurrentWeek();
        Evaluation eval = country.simulateStep(vaccinesForCurrentWeek, administrativeLevel);
        evaluation.infectedSum += eval.infectedSum;
        evaluation.deadSum += eval.deadSum;
        if (eval.infectedSum > evaluation.mostConcurrentInfected)
            evaluation.mostConcurrentInfected = eval.infectedSum;
        country.applyChanges(administrativeLevel);
    }

    public void simulate() {
        for (simulationStep = 0; simulationStep < simulationLength; ++simulationStep)
        {
            simulateStep();
        }
    }

    private int[] getVaccinationsForCurrentWeek() {
        int week = getSimulationWeek();
        return vaccineAvailability[week];
    }

    public int getLowestDivisionCount() {
        int count = 0;
        if (administrativeLevel == 0) {
            count += 1;
        }
        else {
            for (AdminDivision division : country.lowerDivisions) {
                if (administrativeLevel == 1) { // voivodeship count
                    count += 1;
                } else {
                    for (AdminDivision division1 : division.lowerDivisions) {
                        if (administrativeLevel == 2) { // county count
                            count += 1;
                        }
                        else {
                            for (AdminDivision division2 : division1.lowerDivisions) {
                                count += 1; // commune count
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

//    public int[][] getResults() {
//        return new int[][] {timeSeriesS, timeSeriesV, timeSeriesI, timeSeriesR};
//    }

//    public void saveResultsToFile() {
//        int[][] results = getResults();
//        try {
//            FileWriter writer = new FileWriter("svir_data.csv");
//            for (int[] arr: results)
//            {
//                for (int i = 0; i < arr.length; ++i) {
//                    writer.write(String.valueOf(arr[i]));
//                    if (i != arr.length - 1) {
//                        writer.write(",");
//                    }
//                }
//                writer.write("\n");
//            }
//            writer.close();
//        } catch (Exception ignored) {
//
//        }
//    }
}

package com.vaccines.models;

import com.vaccines.areas.AdminDivision;
import com.vaccines.areas.Country;
import com.vaccines.evaluations.Evaluation;
import com.vaccines.populations.SVIRPopulation;
import java.io.FileWriter;

public class SVIR extends EpidemiologicalModel {

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

    @Override
    protected void simulateStep() {
        int[] vaccinesForCurrentWeek = getVaccinationsForCurrentWeek();
        Evaluation eval = country.simulateStep(vaccinesForCurrentWeek, administrativeLevel);

        evaluation.infectedSum += eval.infectedSum;
        evaluation.deadSum += eval.deadSum;
        if (eval.mostConcurrentInfected > evaluation.mostConcurrentInfected)
            evaluation.mostConcurrentInfected = eval.mostConcurrentInfected;

        double s = 0.0, v = 0.0, i = 0.0, r = 0.0;
        for (AdminDivision division : country.lowerDivisions) {
            SVIRPopulation pop = (SVIRPopulation)(division.population);
            s += pop.S.getTotalPopulation();
            v += pop.V.getTotalPopulation();
            i += pop.I.getTotalPopulation();
            r += pop.R.getTotalPopulation();
        }
//        System.out.println("Step " + simulationStep + " - S: " + s + ", V: " + v + ", I: " + i + ", R: " + r);
//        System.out.println("Step " + simulationStep + " - Most concurrent so far: " + (int)evaluation.mostConcurrentInfected);


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

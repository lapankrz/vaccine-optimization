package com.vaccines;

import com.vaccines.areas.Country;
import com.vaccines.evaluations.EvaluationType;
import com.vaccines.models.EpidemiologicalModel;
import com.vaccines.models.ModelType;
import com.vaccines.models.SVIR;
import com.vaccines.problems.OptimizationProblem;
import com.vaccines.problems.SVIRProblem;
import org.apache.commons.lang3.time.StopWatch;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    static int COUNTRY_LEVEL = 0; // Poziom całego kraju
    static int VOIVODESHIP_LEVEL = 1; // Poziom województw
    static int COUNTY_LEVEL = 2; // Poziom powiatów
    static int COMMUNE_LEVEL = 3; // Poziom gmin

    static int SIMULATION_LENGTH = 182; // długość symulacji w dniach (26 tygodni - pół roku)
    static int MAX_WEEKLY_VACCINES = 500000; // maksymalna tygodniowa liczba szczepień
//    static String ALGORITHM_NAME = "OMOPSO"; // algorytm optymalizacji
    static String ALGORITHM_NAME = "NSGAII"; // algorytm optymalizacji
    static int MAX_EVALUATIONS = 500000;
    static int NUMBER_OF_SEEDS = 1;

    public static int solutionNumber = 0;
    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm");

    public static void main(String[] args) {

        var solution = loadSolutionFromFile("solutions\\NSGAII InfectedSum Counties\\problem0_eval16000_score107297_04-12-2022_18-09.csv");
        plotByTotalPop(solution);
//        // Infected sum - solution1
//        SVIR svir = new SVIR(SIMULATION_LENGTH, COUNTY_LEVEL);
//        SVIRProblem svirProblem = new SVIRProblem(svir, MAX_WEEKLY_VACCINES, EvaluationType.InfectedSum);
//        runLongOptimization(svirProblem);
//
//        // Most concurrent infected - solution2
//        svir = new SVIR(SIMULATION_LENGTH, COUNTY_LEVEL);
//        svirProblem = new SVIRProblem(svir, MAX_WEEKLY_VACCINES, EvaluationType.InfectedSum);
//        runLongOptimization(svirProblem);
//
//        // Dead sum - solution3
//        svir = new SVIR(SIMULATION_LENGTH, COUNTY_LEVEL);
//        svirProblem = new SVIRProblem(svir, MAX_WEEKLY_VACCINES, EvaluationType.InfectedSum);
//        runLongOptimization(svirProblem);
    }

    static void runLongOptimization(OptimizationProblem problem) {
        StopWatch watch = new StopWatch();
        watch.start();

        List<NondominatedPopulation> results = new Executor()
                .withProblem(problem)
                .withAlgorithm(ALGORITHM_NAME)
                .withProperty("swarmSize", 1000)
                .withMaxEvaluations(MAX_EVALUATIONS)
                .distributeOnAllCores()
                .runSeeds(NUMBER_OF_SEEDS);

        watch.stop();
        System.out.println("Elapsed time: " + watch.getTime() / 1000.0 + " s");

        System.out.println("Results:");
        Solution bestSolution = null;
        for (var result : results) {
            for (Solution solution : result) {
                if (!solution.violatesConstraints()) {
                    double objective = solution.getObjective(0);
                    System.out.println(objective);
                    if (bestSolution == null || objective < bestSolution.getObjective(0)) {
                        bestSolution = solution;
                    }
                }
            }
        }
        if  (bestSolution != null) {
            System.out.println("Best solution: " + bestSolution.getObjective(0));
            String modelType = problem instanceof SVIRProblem ? "svir" : "sveir";
            String fileName = "solution" + solutionNumber++ + "_" + modelType + "_" + dtf.format(LocalDateTime.now()) + ".csv";
            writeSolutionToFile(bestSolution, fileName, problem.lengthInWeeks, problem.subdivisionCount);
        }
        else
            System.out.println("No solution met the constraints.");
    }

    public static void writeSolutionToFile(Solution solution, String fileName, int weeks, int subdivisionCount) {
        try {
            FileWriter writer = new FileWriter("solutions\\" + fileName);

            for (int i = 0; i < weeks; i++) {
                for (int j = 0; j < subdivisionCount; j++) {
                    if (j != 0)
                        writer.append(", ");
                    int varIndex = i * subdivisionCount + j;
                    writer.append(String.valueOf(solution.getVariable(varIndex)));
                }
                writer.append("\n");
            }
            writer.close();
        }
        catch (Exception ex) {
            System.out.println("Error during saving to file: " + ex.toString());
            System.out.print("Saved solution: ");
            for (int i = 0; i < solution.getNumberOfVariables(); ++i) {
                System.out.print(String.valueOf(solution.getVariable(i)) + ", ");
            }
        }
    }

    public static ArrayList<ArrayList<Double>> loadSolutionFromFile(String fileName) {
        ArrayList<ArrayList<Double>> solution = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.replace(" ", "").split(",");
                ArrayList<Double> divisions = new ArrayList<>();
                for (String value : values) {
                    divisions.add(Double.parseDouble(value));
                }
                solution.add(divisions);
            }
        }
        catch (Exception ex) {
            System.out.print("Error during loading solution from file: " + ex.toString());
            return null;
        }
        return solution;
    }

    public static void plotByTotalPop(ArrayList<ArrayList<Double>> solution) {

        String title = "Waga przydziału szczepionek w zależności od populacji";
        String xAxisLabel = "Populacja powiatu";
        String yAxisLabel = "Waga przydziału szczepionek";

        Country country = new Country();
        country.loadPolishData(ModelType.SVIR);
        var counties = country.getAllDivisionsOnLevel(Main.COUNTY_LEVEL);

        XYSeriesCollection dataset = new XYSeriesCollection();

//        for (int j = 13; j < solution.size(); ++j) {
//            XYSeries series = new XYSeries("Week " + (j + 1));
//            for (int i = 0; i < solution.get(0).size(); ++i) {
//                var weight = solution.get(j).get(i);
//                var county = counties.get(i);
//                series.add(county.population.getTotalOutFlow() / county.population.getTotalPopulation(), weight);
//            }
//            dataset.addSeries(series);
//            break;
//        }

        XYSeries series = new XYSeries("Sum across all weeks");
        int divCount = solution.get(0).size();
        int weeks = solution.size();
        for (int divIndex = 0; divIndex < divCount; ++divIndex) {
            double sum = 0;
            for (ArrayList<Double> week : solution) {
                sum += week.get(divIndex);
            }
            var county = counties.get(divIndex);
            double x = county.population.getTotalInFlow() / county.getTotalPopulation();
            double totpop = county.getTotalPopulation();
            double y = sum / weeks;
            series.add(x, y);
        }
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

        ChartFrame frame = new ChartFrame("Demo", chart);
        frame.pack();
        frame.setVisible(true);
    }

    public static void plotByPopDensity(String fileName) {

    }

    public static void plotByTotalInflow() {

    }

    public static void plotByTotalOutflow() {

    }
}

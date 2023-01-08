package com.vaccines;

import com.vaccines.areas.Country;
import com.vaccines.areas.Powiat;
import com.vaccines.evaluations.EvaluationType;
import com.vaccines.models.EpidemiologicalModel;
import com.vaccines.models.ModelType;
import com.vaccines.populations.Population;
import com.vaccines.populations.SVEIRPopulation;
import com.vaccines.populations.SVIRPopulation;
import com.vaccines.problems.OptimizationProblem;
import com.vaccines.problems.SVIRProblem;
import org.apache.commons.lang3.time.StopWatch;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static int SIMULATION_LENGTH = 182; // długość symulacji w dniach (26 tygodni - pół roku)
    static int MAX_WEEKLY_VACCINES = 600000; // maksymalna tygodniowa liczba szczepień
    static String ALGORITHM_NAME = "OMOPSO"; // algorytm optymalizacji
    static int MAX_EVALUATIONS = 1000000;
    static int NUMBER_OF_SEEDS = 5;

    public static int solutionNumber = 1;
    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm");

    public static void main(String[] args) {
        // SVIR
//        EpidemiologicalModel svir = new EpidemiologicalModel(SIMULATION_LENGTH, ModelType.SVIR);
//        SVIRProblem svirProblem = new SVIRProblem(svir, MAX_WEEKLY_VACCINES);
//        runLongOptimization(svirProblem);
        testModel();
    }

    static void testModel() {
        EpidemiologicalModel svir = new EpidemiologicalModel(SIMULATION_LENGTH, ModelType.SVIR);
        var plan = getVaccinationPlanBasedOnTotalPopulation(svir.country);
        svir.setVaccineAvailability(plan);
        svir.simulate();
        Plotter.plotEvaluations(svir.infections, svir.mostConcurrent);
        System.out.println("INF: " + (int)svir.evaluation.infectedSum + ", MCI: " + (int)svir.evaluation.mostConcurrentInfected);
    }

    static void startFromCheckpoint(OptimizationProblem problem, String filename) {
        ArrayList<Double> variables = new ArrayList<Double>();
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(filename));
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                for (var d : data) {
                    variables.add(Double.parseDouble(d));
                }
            }
            csvReader.close();
        } catch (Exception e) {
            System.out.println("Error reading starting variable data. " + e.toString());
        }

        problem.loadStartingVariables(variables.stream().mapToDouble(i -> i).toArray());

        StopWatch watch = new StopWatch();
        watch.start();

        List<NondominatedPopulation> results = new Executor()
                .withProblem(problem)
                .withAlgorithm(ALGORITHM_NAME)
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

    static void runLongOptimization(OptimizationProblem problem) {
        StopWatch watch = new StopWatch();
        watch.start();

        List<NondominatedPopulation> results = new Executor()
                .withProblem(problem)
                .withAlgorithm(ALGORITHM_NAME)
                .withProperty("swarmSize", 500)
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

    public static ArrayList<ArrayList<Double>> getVaccinationPlanBasedOnTotalPopulation(Country country) {
        var plan = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> weekPlan = new ArrayList<>();
        double totalPopulation = country.getTotalPopulation();
        for (Powiat powiat : country.powiaty) {
            double weight = powiat.getPopulationCount() / totalPopulation;
            weekPlan.add(weight * MAX_WEEKLY_VACCINES);
        }

        for (int i = 0; i < SIMULATION_LENGTH / 7; ++i) {
            plan.add(weekPlan);
        }
        return plan;
    }

    public static ArrayList<ArrayList<Double>> getVaccinationPlanBasedOnPopulationDensity(Country country) {
        var plan = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> weekPlan = new ArrayList<>();
        double sum = 0;
        for (Powiat powiat : country.powiaty) {
            sum += (powiat.getPopulationCount() / powiat.area);
        }

        for (Powiat powiat : country.powiaty) {
            double weight = powiat.getPopulationCount() / powiat.area;
            weekPlan.add(weight / sum * MAX_WEEKLY_VACCINES);
        }

        for (int i = 0; i < SIMULATION_LENGTH / 7; ++i) {
            plan.add(weekPlan);
        }
        return plan;
    }

    public static ArrayList<ArrayList<Double>> getVaccinationPlanBasedOnTotalInflow(Country country) {
        var plan = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> weekPlan = new ArrayList<>();
        double sum = 0;

        for (Powiat powiat : country.powiaty) {
            sum += powiat.getPopulation().getTotalInFlow();
        }

        for (Powiat powiat : country.powiaty) {
            double weight = powiat.getPopulation().getTotalInFlow();
            weekPlan.add(weight / sum * MAX_WEEKLY_VACCINES);
        }

        for (int i = 0; i < SIMULATION_LENGTH / 7; ++i) {
            plan.add(weekPlan);
        }
        return plan;
    }

    public static ArrayList<ArrayList<Double>> getVaccinationPlanBasedOnTotalOutflow(Country country) {
        var plan = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> weekPlan = new ArrayList<>();
        double sum = 0;

        for (Powiat powiat : country.powiaty) {
            sum += powiat.getPopulation().getTotalInFlow();
        }

        for (Powiat powiat : country.powiaty) {
            double weight = powiat.getPopulation().getTotalOutFlow();
            weekPlan.add(weight / sum * MAX_WEEKLY_VACCINES);
        }

        for (int i = 0; i < SIMULATION_LENGTH / 7; ++i) {
            plan.add(weekPlan);
        }
        return plan;
    }
}

package com.vaccines;

import com.vaccines.models.EpidemiologicalModel;
import com.vaccines.models.ModelType;
import com.vaccines.problems.OptimizationProblem;
import com.vaccines.problems.SVEIRProblem;
import com.vaccines.problems.SVIRProblem;
import org.apache.commons.lang3.time.StopWatch;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        EpidemiologicalModel svir = new EpidemiologicalModel(Config.SIMULATION_LENGTH, ModelType.SVIR);
        var solution = SolutionUtils.loadSolutionFromFile("./solutions/final/svir_final.csv");
        if (solution != null) {
            svir.setVaccineAvailability(solution);
            testModel(svir);
        }
    }

    static void testModel(EpidemiologicalModel model) {
        model.simulate();
        Plotter.plotEvaluations(model.infections, model.mostConcurrent);
        System.out.println("INF: " + (int)model.evaluation.infectedSum + ", MCI: " + (int)model.evaluation.mostConcurrentInfected);
    }

    static void runSVIROptimization() {
        EpidemiologicalModel svir = new EpidemiologicalModel(Config.SIMULATION_LENGTH, ModelType.SVIR);
        SVIRProblem svirProblem = new SVIRProblem(svir, Config.MAX_WEEKLY_VACCINES);
        runLongOptimization(svirProblem);
    }

    static void runSVEIROptimization() {
        EpidemiologicalModel sveir = new EpidemiologicalModel(Config.SIMULATION_LENGTH, ModelType.SVEIR);
        SVEIRProblem sveirProblem = new SVEIRProblem(sveir, Config.MAX_WEEKLY_VACCINES);
        runLongOptimization(sveirProblem);
    }

    static void runLongOptimization(OptimizationProblem problem) {

        StopWatch watch = new StopWatch();
        watch.start();

        List<NondominatedPopulation> results = new Executor()
                .withProblem(problem)
                .withAlgorithm(Config.ALGORITHM_NAME)
                .withMaxEvaluations(Config.MAX_EVALUATIONS)
                .distributeOnAllCores()
                .runSeeds(Config.NUMBER_OF_SEEDS);

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
            String fileName = "solution" + "_" + modelType + "_" + SolutionUtils.dtf.format(LocalDateTime.now()) + ".csv";
            SolutionUtils.writeSolutionToFile(bestSolution, fileName, problem.lengthInWeeks, problem.subdivisionCount);
        }
        else
            System.out.println("No solution met the constraints.");
    }
}

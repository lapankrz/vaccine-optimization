package com.vaccines;

import com.vaccines.evaluations.EvaluationType;
import com.vaccines.models.SVIR;
import org.apache.commons.lang3.time.StopWatch;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        int simulationLength = 180;
        int adminLevel = 2;
        int maxWeeklyVaccines = 1000000;
        EvaluationType evaluationType = EvaluationType.InfectedSum;

        SVIR svir = new SVIR(simulationLength, adminLevel);
        SVIRProblem problem = new SVIRProblem(svir, maxWeeklyVaccines, evaluationType);

        StopWatch watch = new StopWatch();
        watch.start();
        List<NondominatedPopulation> results = new Executor()
                .withProblem(problem)
                .withAlgorithm("PAES")
                .withMaxEvaluations(10)
                .distributeOnAllCores()
                .runSeeds(1);
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
        if  (bestSolution != null)
            System.out.println("Best solution: " + bestSolution.getObjective(0));
        else
            System.out.println("No solution met the constraints.");
    }
}

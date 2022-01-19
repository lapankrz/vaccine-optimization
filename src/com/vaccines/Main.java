package com.vaccines;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Variable;

import java.util.List;

public class Main {

    public static void main(String[] args) {
//        List<NondominatedPopulation> results = new Executor()
//                .withProblemClass(SVIRProblem.class)
//                .withAlgorithm("CMAES")
//                .withMaxEvaluations(10000)
//                .runSeeds(10);
//        double minObj = Double.MAX_VALUE;
//        var bestResult = new Variable[3];
//        for (NondominatedPopulation pop: results) {
//            double obj = pop.get(0).getObjective(0);
//            if (obj < minObj) {
//                minObj = obj;
//                bestResult = new Variable[]{pop.get(0).getVariable(0), pop.get(0).getVariable(1), pop.get(0).getVariable(2)};
//            }
//        }
//        System.out.println("Error = " + minObj +
//                "\nnatural death rate = " + bestResult[0] +
//                "\nsusceptible transmission rate = " + bestResult[1] +
//                "\nrecoveryRate = " + bestResult[2]);

        SVIRProblem problem = new SVIRProblem();
        double[] bestParams = problem.parameterGridSearch();
        problem.model.saveResultsToFile();
        problem.saveRealDataToFile();
    }
}

package com.vaccines.problems;

import com.vaccines.Main;
import com.vaccines.evaluations.EvaluationType;
import com.vaccines.models.EpidemiologicalModel;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;

import java.time.LocalDateTime;
import java.util.Arrays;

public class SVEIRProblem extends OptimizationProblem {

    public SVEIRProblem(EpidemiologicalModel model, int maxWeeklyVaccines) {
        super(model, maxWeeklyVaccines);
    }

    void evaluateWithWeightScaling(Solution solution) {
        EpidemiologicalModel sveir = new EpidemiologicalModel(model);
        int[][] vaccines = new int[lengthInWeeks][subdivisionCount];

        for (int week = 0; week < lengthInWeeks; ++week) {
            double weeklyVariableSum = 0.0;
            for (int subdivision = 0; subdivision < subdivisionCount; ++subdivision) {
                int index = subdivisionCount * week + subdivision;
                double variable = ((RealVariable)solution.getVariable(index)).getValue();
                weeklyVariableSum += variable;
                vaccines[week][subdivision] = (int) (variable * maxWeeklyVaccines);// / subdivisionCount);
            }
            for (int subdivision = 0; subdivision < subdivisionCount; ++subdivision) {
                vaccines[week][subdivision] /= weeklyVariableSum;
            }
        }

        sveir.setVaccineAvailability(vaccines);
        sveir.simulate();
        solution.setObjective(0, sveir.evaluation.infectedSum);
        solution.setObjective(1, sveir.evaluation.mostConcurrentInfected);

        for (int i = 0; i < lengthInWeeks; ++i) {
            solution.setConstraint(i, 0);
        }

        if (evaluation % 100 == 0 && evaluation > 0) {
            int eval = evaluation++;
            if (eval % 100 == 0) {
                System.out.println("Evaluation " + eval + " - INF: " + (int)sveir.evaluation.infectedSum
                        + ", MCI: " + (int)sveir.evaluation.mostConcurrentInfected);
                String fileName = "problem" + Main.solutionNumber + "_eval" + eval + "_inf" + (int)sveir.evaluation.infectedSum
                        + "_mci" + (int)sveir.evaluation.mostConcurrentInfected + "_" + Main.dtf.format(LocalDateTime.now()) + ".csv";
                Main.writeSolutionToFile(solution, fileName, lengthInWeeks, subdivisionCount);
            }
        }
        else {
            evaluation++;
        }
    }
}

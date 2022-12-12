package com.vaccines.problems;

import com.vaccines.evaluations.EvaluationType;
import com.vaccines.models.SVEIR;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

import java.util.Arrays;

public class SVEIRProblem extends OptimizationProblem {

    public SVEIRProblem(SVEIR model, int maxWeeklyVaccines, EvaluationType evaluationType) {
        super(model, maxWeeklyVaccines, evaluationType);
    }

    void evaluateWithWeightScaling(Solution solution) {
        SVEIR sveir = new SVEIR((SVEIR)model);
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
        double score = calculateObjective(sveir);
        solution.setObjective(0, score);

        for (int i = 0; i < lengthInWeeks; ++i) {
            solution.setConstraint(i, 0);
        }
        if (evaluation % 1 == 0)
            System.out.println("Evaluation " + evaluation++ + " - score: " + score);

        evaluation++;
    }

    void evaluateWithConstraintChecking(Solution solution) {
        SVEIR sveir = new SVEIR((SVEIR)model);
        int[][] vaccines = new int[lengthInWeeks][subdivisionCount];
        for (int i = 0; i < getNumberOfVariables(); ++i) { //format: (week1:) subdiv1, subdiv2,..., (week2:) subdiv1,etc
            double variable = ((RealVariable)solution.getVariable(i)).getValue();
            int week = i / subdivisionCount;
            int subdivision = i - week * subdivisionCount;
            vaccines[week][subdivision] = (int) (variable * maxWeeklyVaccines);
        }
        sveir.setVaccineAvailability(vaccines);
        sveir.simulate();
        double score = calculateObjective(sveir);
        solution.setObjective(0, score);
        for (int i = 0; i < lengthInWeeks; ++i) {
            int sum = Arrays.stream(vaccines[i]).sum();
            boolean constraintSatisfied = sum <= maxWeeklyVaccines;
            solution.setConstraint(i, constraintSatisfied ? 0 : 1);
        }

        System.out.println("Evaluation " + evaluation++);
    }
}

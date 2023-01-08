package com.vaccines.problems;

import com.vaccines.evaluations.EvaluationType;
import com.vaccines.models.EpidemiologicalModel;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

import java.util.Arrays;

public abstract class OptimizationProblem extends AbstractProblem {

    public EpidemiologicalModel model;
    public int lengthInWeeks;
    public int maxWeeklyVaccines;
    public int subdivisionCount;
    int evaluation = 0;
    double[] startingVariables = null;

    public OptimizationProblem(EpidemiologicalModel model, int maxWeeklyVaccines) {
        super(model.getNumberOfWeeks() * model.getLowestDivisionCount(), 2, model.getNumberOfWeeks());
        this.model = model;
        this.maxWeeklyVaccines = maxWeeklyVaccines;
        this.lengthInWeeks = model.getNumberOfWeeks();
        this.subdivisionCount = model.getLowestDivisionCount();
    }

    public void loadStartingVariables(double[] variables) {
        startingVariables = variables;
    }

    @Override
    public Solution newSolution() {
        Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives(), getNumberOfConstraints());
        for (int i = 0; i < getNumberOfVariables(); ++i) {
            if (startingVariables != null) {
                solution.setVariable(i, new RealVariable(startingVariables[i], 0.0, 1.0));
            }
            else {
                solution.setVariable(i, new RealVariable(0.5, 0.0, 1.0));
            }
        }
        return solution;
    }

    @Override
    public void evaluate(Solution solution) {
        evaluateWithWeightScaling(solution);
    }

    abstract void evaluateWithWeightScaling(Solution solution);
}
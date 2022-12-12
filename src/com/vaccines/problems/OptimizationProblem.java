package com.vaccines.problems;

import com.vaccines.evaluations.EvaluationType;
import com.vaccines.models.EpidemiologicalModel;
import com.vaccines.models.SVEIR;
import com.vaccines.models.SVIR;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

import java.util.Arrays;

public abstract class OptimizationProblem extends AbstractProblem {

    public EpidemiologicalModel model;
    public int lengthInWeeks;
    public int maxWeeklyVaccines;
    public int subdivisionCount;
    public EvaluationType evaluationType;
    int evaluation = 0;

    public OptimizationProblem(EpidemiologicalModel model, int maxWeeklyVaccines, EvaluationType evaluationType) {
        super(model.getNumberOfWeeks() * model.getLowestDivisionCount(), 1, model.getNumberOfWeeks());
        this.model = model;
        this.maxWeeklyVaccines = maxWeeklyVaccines;
        this.lengthInWeeks = model.getNumberOfWeeks();
        this.subdivisionCount = model.getLowestDivisionCount();
        this.evaluationType = evaluationType;
    }

    @Override
    public Solution newSolution() {
        Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives(), getNumberOfConstraints());
        for (int i = 0; i < getNumberOfVariables(); ++i) {
            solution.setVariable(i, new RealVariable(0.5, 0.0, 1.0));
        }
        return solution;
    }

    @Override
    public void evaluate(Solution solution) {
        evaluateWithWeightScaling(solution);
    }

    abstract void evaluateWithWeightScaling(Solution solution);

    abstract void evaluateWithConstraintChecking(Solution solution);

    protected double calculateObjective(EpidemiologicalModel model) {
        if (evaluationType == EvaluationType.InfectedSum) {
            return model.evaluation.infectedSum;
        } else if (evaluationType == EvaluationType.DeadSum) {
            return model.evaluation.deadSum;
        } else if (evaluationType == EvaluationType.MostConcurrentInfected) {
            return model.evaluation.mostConcurrentInfected;
        }
        return 0;
    }
}
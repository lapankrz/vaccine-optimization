package com.vaccines.populations;

import com.vaccines.areas.AdminDivision;
import com.vaccines.evaluations.Evaluation;

import java.util.HashMap;

public abstract class Population {
    public HashMap<AdminDivision, Flow> inFlow = new HashMap<>();
    double totalInFlow = -1;
    public HashMap<AdminDivision, Flow> outFlow = new HashMap<>();
    double totalOutFlow = -1;

    // updates this population based on all interactions and migrations to other places
    public abstract Evaluation update(int vaccines);
    public abstract double getTotalPopulation();
    public abstract double getTotalStudents();
    public abstract double getTotalAdults();
    public abstract double getTotalSeniors();
    public abstract void applyChanges();

    public double getPopulationAfterFlows() {
        return getTotalPopulation() + getTotalInFlow() - getTotalOutFlow();
    }

    public void copyFlows(Population population) {
        inFlow = population.inFlow;
        outFlow = population.outFlow;
    }

    public double getTotalInFlow() {
        if (totalInFlow == -1) {
            totalInFlow = 0;
            for (Flow flow : inFlow.values()) {
                totalInFlow += flow.getTotalFlow();
            }
        }
        return totalInFlow;
    }

    public double getTotalOutFlow() {
        if (totalOutFlow == -1) {
            totalOutFlow = 0;
            for (Flow flow : outFlow.values()) {
                totalOutFlow += flow.getTotalFlow();
            }
        }
        return totalOutFlow;
    }
}

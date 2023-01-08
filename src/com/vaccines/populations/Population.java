package com.vaccines.populations;

import com.vaccines.areas.Powiat;
import com.vaccines.evaluations.Evaluation;

import java.util.HashMap;
import java.util.Objects;

public abstract class Population {
    public Powiat powiat;
    public double selfMigration;
    public HashMap<Powiat, Double> inFlow = new HashMap<>();
    public HashMap<Powiat, Double> outFlow = new HashMap<>();

    // updates this population based on all interactions and migrations to other places
    public abstract Evaluation update(int vaccines);
    public abstract double getTotalPopulation();
    public abstract void applyChanges();

    public void copyFlows(Population population) {
        inFlow = population.inFlow;
        outFlow = population.outFlow;
    }

    double totalPopulationAfterFlows = -1;
    public double getPopulationAfterFlows() {
        if (totalPopulationAfterFlows < 0) {
            totalPopulationAfterFlows = getTotalPopulation() + getTotalInFlow() - getTotalOutFlow();
        }
        return totalPopulationAfterFlows;
    }

    double totalInFlow = -1;
    public double getTotalInFlow() {
        if (totalInFlow == -1) {
            totalInFlow = 0;
            for (Powiat sourcePowiat : inFlow.keySet()) {
                if (!Objects.equals(sourcePowiat.code, powiat.code)) {
                    double flow = inFlow.get(sourcePowiat);
                    totalInFlow += flow;
                }
            }
        }
        return totalInFlow;
    }

    double totalOutFlow = -1;
    public double getTotalOutFlow() {
        if (totalOutFlow == -1) {
            totalOutFlow = 0;
            for (Powiat targetPowiat : outFlow.keySet()) {
                if (!Objects.equals(targetPowiat.code, powiat.code)) {
                    double flow = outFlow.get(targetPowiat);
                    totalOutFlow += flow;
                }
            }
        }
        return totalOutFlow;
    }
}

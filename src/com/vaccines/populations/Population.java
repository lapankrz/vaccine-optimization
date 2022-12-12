package com.vaccines.populations;

import com.vaccines.areas.AdminDivision;
import com.vaccines.evaluations.Evaluation;

import java.util.HashMap;
import java.util.Objects;

public abstract class Population {
    public AdminDivision adminDivision;
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
            for (AdminDivision div : inFlow.keySet()) {
                if (!Objects.equals(div.code, adminDivision.code)) {
                    Flow flow = inFlow.get(div);
                    totalInFlow += flow.getTotalFlow();
                }
                else
                {
                    int k = 0;
                    System.out.println();
                }
            }
        }
        return totalInFlow;
    }

    double totalStudentOutFlow = -1;
    public double getTotalStudentOutFlow() {
        if (totalStudentOutFlow == -1) {
            totalStudentOutFlow = 0;
            for (AdminDivision div : outFlow.keySet()) {
                if (div != adminDivision) {
                    Flow flow = outFlow.get(div);
                    totalStudentOutFlow += flow.students;
                }
            }
        }
        return totalStudentOutFlow;
    }

    double totalAdultOutFlow = -1;
    public double getTotalAdultOutFlow() {
        if (totalAdultOutFlow == -1) {
            totalAdultOutFlow = 0;
            for (AdminDivision div : outFlow.keySet()) {
                if (div != adminDivision) {
                    Flow flow = outFlow.get(div);
                    totalAdultOutFlow += flow.adults;
                }
            }
        }
        return totalAdultOutFlow;
    }

    double totalSeniorOutFlow = -1;
    public double getTotalSeniorOutFlow() {
        if (totalSeniorOutFlow == -1) {
            totalSeniorOutFlow = 0;
            for (AdminDivision div : outFlow.keySet()) {
                if (div != adminDivision) {
                    Flow flow = outFlow.get(div);
                    totalSeniorOutFlow += flow.seniors;
                }
            }
        }
        return totalSeniorOutFlow;
    }

    public double getTotalOutFlow() {
        if (totalOutFlow == -1) {
            totalOutFlow = 0;
            for (AdminDivision div : outFlow.keySet()) {
                if (div != adminDivision) {
                    Flow flow = outFlow.get(div);
                    totalOutFlow += flow.getTotalFlow();
                }
            }
        }
        return totalOutFlow;
    }
}

package com.vaccines.populations;

import com.vaccines.Config;
import com.vaccines.areas.Powiat;
import com.vaccines.compartments.Compartment;
import com.vaccines.compartments.CompartmentType;
import com.vaccines.evaluations.Evaluation;

public class SVEIRPopulation extends Population {
    public Compartment S, V, E, I, R;

    public SVEIRPopulation(int[] compartments) {
        S = new Compartment(CompartmentType.Susceptible, compartments[0]);
        V = new Compartment(CompartmentType.Vaccinated,compartments[1]);
        V = new Compartment(CompartmentType.Exposed,compartments[2]);
        I = new Compartment(CompartmentType.Infected,compartments[3]);
        R = new Compartment(CompartmentType.Recovered,compartments[4]);
    }

    public SVEIRPopulation(int size) {
        S = new Compartment(CompartmentType.Susceptible, size);
        V = new Compartment(CompartmentType.Vaccinated, 0);
        E = new Compartment(CompartmentType.Exposed, 0);
        I = new Compartment(CompartmentType.Infected, 0);
        R = new Compartment(CompartmentType.Recovered, 0);
    }

    public SVEIRPopulation(SVEIRPopulation population) {
        copyFlows(population);
        S = new Compartment(population.S);
        V = new Compartment(population.V);
        E = new Compartment(population.E);
        I = new Compartment(population.I);
        R = new Compartment(population.R);
    }

    @Override
    public Evaluation update(int vaccines) {
        Evaluation evaluation = new Evaluation();

        // expositions in local area (S -> E)
        double s = (getTotalPopulation() - getTotalOutFlow()) * getSusceptiblePercentage();
        double i = getTotalInfectedWithFlows();
        double e = getTotalExposedWithFlows();
        double total = getPopulationAfterFlows();
        double sToE = Config.contactRate * Config.infectedInfectiousness * s * i / total
                + Config.contactRate * Config.exposedInfectiousness * s * e / total;
        changePopulation(S, -sToE);
        changePopulation(E, sToE);

        // expositions in local area (V -> E)
        double v = (getTotalPopulation() - getTotalOutFlow()) * getVaccinatedPercentage();
        double vToE = Config.contactRate * Config.infectedInfectiousness * (1 - Config.vaccinationInfectionChanceReduction) * v * i / total
                    + Config.contactRate * Config.exposedInfectiousness * (1 - Config.vaccinationInfectionChanceReduction) * v * e / total;
        changePopulation(V, -vToE);
        changePopulation(E, vToE);

        // expositions in other areas
        for (Powiat powiat : outFlow.keySet()) {
            SVEIRPopulation pop = (SVEIRPopulation)powiat.getPopulation();
            double flow = outFlow.get(powiat);

            sToE = calculateStoE(flow, pop);
            changePopulation(S, -sToE);
            changePopulation(E, sToE);

            vToE = calculateVtoE(flow, pop);
            changePopulation(V, -vToE);
            changePopulation(E, vToE);
        }

        double sToV = calculateStoV(vaccines);
        changePopulation(S, -sToV);
        changePopulation(V, sToV);

        double eToI = calculateEtoI();
        changePopulation(E, -eToI);
        changePopulation(I, eToI);
        evaluation.infectedSum += eToI;

        double eToR = calculateEtoR();
        changePopulation(E, -eToR);
        changePopulation(R, eToR);

        double iToR = calculateItoR();
        changePopulation(I, -iToR);
        changePopulation(R, iToR);

        evaluation.mostConcurrentInfected = I.size + I.diff;
        return evaluation;
    }

    @Override
    public void applyChanges() {
        S.applyChanges();
        V.applyChanges();
        E.applyChanges();
        I.applyChanges();
        R.applyChanges();
    }

    private double calculateStoV(int vaccines) {
        double vAfter = V.size + vaccines;
        double maxVaccinated = Config.maxVaccinationPercentage * getTotalPopulation();
        if (vAfter > maxVaccinated) {
            return maxVaccinated - V.size;
        } else {
            return vaccines;
        }
    }

    private double calculateStoE(double flow, SVEIRPopulation pop) {
        double s = flow * getSusceptiblePercentage();
        double i = pop.getTotalInfectedWithFlows();
        double e = pop.getTotalExposedWithFlows();
        double total = pop.getPopulationAfterFlows();
        if (total > 0)
            return Config.contactRate * Config.infectedInfectiousness * s * i / total
                    + Config.contactRate * Config.exposedInfectiousness * s * e / total;
        else
            return  0;
    }

    private double calculateVtoE(double flow, SVEIRPopulation pop) {
        double v = flow * getVaccinatedPercentage();
        double i = pop.getTotalInfectedWithFlows();
        double e = pop.getTotalExposedWithFlows();
        double total = pop.getPopulationAfterFlows();
        if (total > 0)
            return Config.contactRate * Config.infectedInfectiousness * (1 - Config.vaccinationInfectionChanceReduction) * v * i / total
                    + Config.contactRate * Config.exposedInfectiousness * (1 - Config.vaccinationInfectionChanceReduction) * v * e / total;
        else
            return  0;
    }

    private double calculateEtoI() {
        return (1 / Config.meanDurationOfLatency) * E.size;
    }

    private double calculateEtoR() {
        return Config.recoveryRateOfLatents * E.size;
    }

    private double calculateItoR() {
        return Config.meanInfectedRecoveryRate * I.size;
    }

    private void changePopulation(Compartment compartment, double diff) {
        compartment.diff += diff;
    }

    double totalPopulation = -1;
    @Override
    public double getTotalPopulation() {
        if (totalPopulation < 0)
        {
            totalPopulation = S.size + V.size + E.size + I.size + R.size;
        }
        return totalPopulation;
    }

    public double getSusceptiblePercentage() {
        return S.size / getTotalPopulation();
    }

    public double getVaccinatedPercentage() {
        return V.size / getTotalPopulation();
    }

    public double getInfectedPercentage() {
        return I.size / getTotalPopulation();
    }

    public double getExposedPercentage() {
        return E.size / getTotalPopulation();
    }

    public double getTotalInfectedWithFlows() {
        double totalI = I.size;
        totalI += getTotalInfectedInFlow();
        totalI -= getTotalInfectedOutFlow();
        return totalI;
    }

    public double getTotalInfectedInFlow() {
        double total = 0;
        for (Powiat sourcePowiat : inFlow.keySet()) {
            SVEIRPopulation pop = (SVEIRPopulation)sourcePowiat.getPopulation();
            total += inFlow.get(sourcePowiat) * pop.getInfectedPercentage();
        }
        return total;
    }

    public double getTotalInfectedOutFlow() {
        double total = 0;
        for (Powiat targetPowiat : outFlow.keySet()) {
            total += outFlow.get(targetPowiat) * getInfectedPercentage();
        }
        return total;
    }

    public double getTotalExposedWithFlows() {
        double totalE = E.size;
        totalE += getTotalExposedInFlow();
        totalE -= getTotalExposedOutFlow();
        return totalE;
    }

    public double getTotalExposedInFlow() {
        double total = 0;
        for (Powiat sourcePowiat : inFlow.keySet()) {
            SVEIRPopulation pop = (SVEIRPopulation)sourcePowiat.getPopulation();
            total += inFlow.get(sourcePowiat) * pop.getExposedPercentage();
        }
        return total;
    }

    public double getTotalExposedOutFlow() {
        double total = 0;
        for (Powiat targetPowiat : outFlow.keySet()) {
            total += outFlow.get(targetPowiat) * getExposedPercentage();
        }
        return total;
    }
}

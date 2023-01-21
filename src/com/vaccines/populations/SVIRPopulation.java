package com.vaccines.populations;

import com.vaccines.Config;
import com.vaccines.areas.Powiat;
import com.vaccines.compartments.Compartment;
import com.vaccines.compartments.CompartmentType;
import com.vaccines.evaluations.Evaluation;

public class SVIRPopulation extends Population {
    public Compartment S, V, I, R;

    public SVIRPopulation(int[] compartments) {
        S = new Compartment(CompartmentType.Susceptible, compartments[0]);
        V = new Compartment(CompartmentType.Vaccinated,compartments[1]);
        I = new Compartment(CompartmentType.Infected,compartments[2]);
        R = new Compartment(CompartmentType.Recovered,compartments[3]);
    }

    public SVIRPopulation(int size) {
        S = new Compartment(CompartmentType.Susceptible, size);
        V = new Compartment(CompartmentType.Vaccinated, 0);
        I = new Compartment(CompartmentType.Infected, 0);
        R = new Compartment(CompartmentType.Recovered, 0);
    }

    public SVIRPopulation(SVIRPopulation population) {
        copyFlows(population);
        S = new Compartment(population.S);
        V = new Compartment(population.V);
        I = new Compartment(population.I);
        R = new Compartment(population.R);
        this.powiat = population.powiat;
        this.totalPopulation = -1;
    }

    public void sumPopulation(SVIRPopulation pop) {
        S.addPopulation(pop.S);
        V.addPopulation(pop.V);
        I.addPopulation(pop.I);
        R.addPopulation(pop.R);
    }

    @Override
    public Evaluation update(int vaccines) {
        Evaluation evaluation = new Evaluation();

        // infections in local area (S -> I)
        double s = (getTotalPopulation() - getTotalOutFlow()) * getSusceptiblePercentage();
        double i = getTotalInfectedWithFlows();
        double total = getPopulationAfterFlows();
        double sToI = Config.susceptibleTransmissionRate * s * i / total;
        evaluation.infectedSum += sToI;
        changePopulation(S, -sToI);
        changePopulation(I, sToI);

        // infections in local area (V -> I)
        double v = (getTotalPopulation() - getTotalOutFlow()) * getVaccinatedPercentage();
        double vToI = Config.vaccinatedTransmissionRate * v * i / total;
        evaluation.infectedSum += vToI;
        changePopulation(V, -vToI);
        changePopulation(I, vToI);

        // infections in other areas
        for (Powiat powiat : outFlow.keySet()) {
            SVIRPopulation pop = (SVIRPopulation)powiat.getPopulation();
            double flow = outFlow.get(powiat);

            sToI = calculateStoI(flow, pop);
            evaluation.infectedSum += sToI;
            changePopulation(S, -sToI);
            changePopulation(I, sToI);

            vToI = calculateVtoI(flow, pop);
            evaluation.infectedSum += vToI;
            changePopulation(V, -vToI);
            changePopulation(I, vToI);
        }

        double sToV = calculateStoV(vaccines);
        changePopulation(S, -sToV);
        changePopulation(V, sToV);
        updateVaccinationPercentage(sToV);

        double vToR = calculateVtoR();
        changePopulation(V, -vToR);
        changePopulation(R, vToR);

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
        I.applyChanges();
        R.applyChanges();
    }

    void updateVaccinationPercentage(double vaccinated) {
        int size = powiat.vaccinationPercentage.size();
        if (size > 0) {
            vaccinated += powiat.vaccinationPercentage.get(size - 1);
        }
        powiat.vaccinationPercentage.add(vaccinated);
    }

    private double calculateStoI(double flow, SVIRPopulation pop) {
        double s = flow * getSusceptiblePercentage();
        double i = pop.getTotalInfectedWithFlows();
        double total = pop.getPopulationAfterFlows();
        if (total > 0)
            return Config.susceptibleTransmissionRate * s * i / total;
        else
            return  0;
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

    private double calculateVtoI(double flow, SVIRPopulation pop) {
        double v = flow * getVaccinatedPercentage();
        double i = pop.getTotalInfectedWithFlows();
        double total = pop.getPopulationAfterFlows();
        if (total > 0)
            return Config.vaccinatedTransmissionRate * v * i / total;
        else
            return  0;
    }

    private double calculateItoR() {
        return Config.recoveryRate * I.size;
    }

    private double calculateVtoR() {
        return Config.vaccineImmunizationRate * V.size;
    }

    private void changePopulation(Compartment compartment, double diff) {
        compartment.diff += diff;
    }

    double totalPopulation = -1;
    @Override
    public double getTotalPopulation() {
        if (totalPopulation < 0)
        {
            totalPopulation = S.size + V.size + I.size + R.size;
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


    public double getTotalSusceptibleWithFlows() {
        double totalS = S.size;
        for (Powiat sourcePowiat : inFlow.keySet()) {
            SVIRPopulation pop = (SVIRPopulation)sourcePowiat.getPopulation();
            totalS += inFlow.get(sourcePowiat) * pop.getSusceptiblePercentage();
        }
        for (Powiat targetPowiat : outFlow.keySet()) {
            totalS -= outFlow.get(targetPowiat) * getSusceptiblePercentage();
        }
        return totalS;
    }

    public double getTotalVaccinatedWithFlows() {
        double totalV = V.size;
        for (Powiat sourcePowiat : inFlow.keySet()) {
            SVIRPopulation pop = (SVIRPopulation)sourcePowiat.getPopulation();
            totalV += inFlow.get(sourcePowiat) * pop.getVaccinatedPercentage();
        }
        for (Powiat targetPowiat : outFlow.keySet()) {
            totalV -= outFlow.get(targetPowiat) * getVaccinatedPercentage();
        }
        return totalV;
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
            SVIRPopulation pop = (SVIRPopulation)sourcePowiat.getPopulation();
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
}

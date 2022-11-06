package com.vaccines.populations;

import com.vaccines.Config;
import com.vaccines.areas.AdminDivision;
import com.vaccines.compartments.Compartment;
import com.vaccines.compartments.CompartmentType;
import com.vaccines.evaluations.Evaluation;

import java.util.ArrayList;

public class SVIRPopulation extends Population {
    public Compartment S, V, I, R;

    public SVIRPopulation(int[][] compartments) {
        S = new Compartment(CompartmentType.Susceptible, compartments[0][0], compartments[0][1], compartments[0][2]);
        V = new Compartment(CompartmentType.Vaccinated,compartments[1][0], compartments[1][1], compartments[1][2]);
        I = new Compartment(CompartmentType.Infected,compartments[2][0], compartments[2][1], compartments[2][2]);
        R = new Compartment(CompartmentType.Recovered,compartments[3][0], compartments[3][1], compartments[3][2]);
    }

    public SVIRPopulation(int students, int adults, int seniors) {
        S = new Compartment(CompartmentType.Susceptible, students, adults, seniors);
        V = new Compartment(CompartmentType.Vaccinated, 0, 0, 0);
        I = new Compartment(CompartmentType.Infected, 0, 0, 0);
        R = new Compartment(CompartmentType.Recovered, 0, 0, 0);
    }

    public SVIRPopulation(ArrayList<Population> populations) {
        S = new Compartment(CompartmentType.Susceptible, 0, 0, 0);
        V = new Compartment(CompartmentType.Vaccinated, 0, 0, 0);
        I = new Compartment(CompartmentType.Infected, 0, 0, 0);
        R = new Compartment(CompartmentType.Recovered, 0, 0, 0);
        for (Population p : populations) {
            SVIRPopulation pop = (SVIRPopulation) p;
            sumPopulation(pop);
            addFlowsFromSubdivision(pop);
        }
    }

    public SVIRPopulation(SVIRPopulation population) {
        copyFlows(population);
        S = new Compartment(population.S);
        V = new Compartment(population.V);
        I = new Compartment(population.I);
        R = new Compartment(population.R);
    }

    public void sumPopulation(SVIRPopulation pop) {
        S.addPopulation(pop.S);
        V.addPopulation(pop.V);
        I.addPopulation(pop.I);
        R.addPopulation(pop.R);
    }

    public void addFlowsFromSubdivision(SVIRPopulation pop) {
        // inFlow
        for (AdminDivision division : pop.inFlow.keySet()) {
            AdminDivision from = division.higherDivision;
            if (inFlow.containsKey(from)) {
                Flow oldFlow = inFlow.get(from);
                Flow newFlow = new Flow(oldFlow, pop.inFlow.get(division));
                inFlow.replace(from, newFlow);
            }
            else {
                inFlow.put(from, pop.inFlow.get(division));
            }
        }
        // outFlow
        for (AdminDivision division : pop.outFlow.keySet()) {
            AdminDivision from = division.higherDivision;
            if (outFlow.containsKey(from)) {
                Flow oldFlow = outFlow.get(from);
                Flow newFlow = new Flow(oldFlow, pop.outFlow.get(division));
                outFlow.replace(from, newFlow);
            }
            else {
                outFlow.put(from, pop.outFlow.get(division));
            }
        }
    }

    @Override
    public Evaluation update(int vaccines) {
        Evaluation evaluation = new Evaluation();
        for (AdminDivision division : outFlow.keySet()) {
            SVIRPopulation pop = (SVIRPopulation)division.population;
            double flow = outFlow.get(division).getTotalFlow();

            double sToI = calculateStoI(flow, pop);
            evaluation.infectedSum += sToI;
            changePopulationsProportionally(S, -sToI);
            changePopulationsProportionally(I, sToI);

            double vToI = calculateVtoI(flow, pop);
            evaluation.infectedSum += vToI;
            changePopulationsProportionally(V, -vToI);
            changePopulationsProportionally(I, vToI);
        }
        double sToV = calculateStoV(vaccines);
        changePopulationsProportionally(S, -sToV);
        changePopulationsProportionally(V, sToV);

        double vToR = calculateVtoR();
        changePopulationsProportionally(V, -vToR);
        changePopulationsProportionally(R, vToR);

        double iToR = calculateItoR();
        changePopulationsProportionally(I, -iToR);
        changePopulationsProportionally(R, iToR);

        return evaluation;
    }

    @Override
    public void applyChanges() {
        S.applyChanges();
        V.applyChanges();
        I.applyChanges();
        R.applyChanges();
    }

    private double calculateStoI(double flow, SVIRPopulation pop) {
        double s = flow * getSusceptiblePercentage();
        double i = pop.getTotalInfectedInFlow();
        double total = pop.getTotalInFlow(); //pop.getPopulationAfterFlows();
        if (total > 0)
            return Config.susceptibleTransmissionRate * s * i / total;
        else
            return  0;
    }

    private double calculateStoV(int vaccines) {
        return vaccines;
    }

    private double calculateVtoI(double flow, SVIRPopulation pop) {
        double v = flow * getVaccinatedPercentage();
        double i = pop.getTotalInfectedInFlow();
        double total = pop.getTotalInFlow(); //pop.getPopulationAfterFlows();
        if (total > 0)
            return Config.vaccinatedTransmissionRate * v * i / total;
        else
            return  0;
    }

    private double calculateItoR() {
        return Config.recoveryRate * I.getTotalPopulation();
    }

    private double calculateVtoR() {
        return Config.vaccineImmunizationRate * V.getTotalPopulation();
    }

    private void changePopulationsProportionally(Compartment compartment, double diff) {
        double total = getTotalPopulation();
        if (total != 0) {
            compartment.studentsDiff += diff * getTotalStudents() / total;
            compartment.adultsDiff += diff * getTotalAdults() / total;
            compartment.seniorsDiff += diff * getTotalSeniors() / total;
        }
    }

    @Override
    public double getTotalPopulation() {
        return S.getTotalPopulation() + V.getTotalPopulation() + I.getTotalPopulation() + R.getTotalPopulation();
    }

    @Override
    public double getTotalStudents() {
        return S.students + V.students + I.students + R.students;
    }

    @Override
    public double getTotalAdults() {
        return S.adults + V.adults + I.adults + R.adults;
    }

    @Override
    public double getTotalSeniors() {
        return S.seniors + V.seniors + I.seniors + R.seniors;
    }

    public double getSusceptiblePercentage() {
        return S.getTotalPopulation() / getTotalPopulation();
    }

    public double getVaccinatedPercentage() {
        return V.getTotalPopulation() / getTotalPopulation();
    }

    public double getInfectedPercentage() {
        return I.getTotalPopulation() / getTotalPopulation();
    }

    public double getRecoveredPercentage() {
        return R.getTotalPopulation() / getTotalPopulation();
    }

    public double getTotalSusceptibleWithFlows() {
        double totalS = S.getTotalPopulation();
        for (AdminDivision division : inFlow.keySet()) {
            SVIRPopulation pop = (SVIRPopulation)division.population;
            totalS += inFlow.get(division).getTotalFlow() * pop.getSusceptiblePercentage();
        }
        for (AdminDivision commune : outFlow.keySet()) {
            totalS -= outFlow.get(commune).getTotalFlow() * getSusceptiblePercentage();
        }
        return totalS;
    }

    public double getTotalVaccinatedWithFlows() {
        double totalV = V.getTotalPopulation();
        for (AdminDivision division : inFlow.keySet()) {
            SVIRPopulation pop = (SVIRPopulation)division.population;
            totalV += inFlow.get(division).getTotalFlow() * pop.getVaccinatedPercentage();
        }
        for (AdminDivision division : outFlow.keySet()) {
            totalV -= outFlow.get(division).getTotalFlow() * getVaccinatedPercentage();
        }
        return totalV;
    }

    public double getTotalInfectedWithFlows() {
        double totalI = I.getTotalPopulation();
        totalI += getTotalInfectedInFlow();
        totalI -= getTotalInfectedOutFlow();
        return totalI;
    }

    public double getTotalInfectedInFlow() {
        double total = 0;
        for (AdminDivision division : inFlow.keySet()) {
            SVIRPopulation pop = (SVIRPopulation)division.population;
            total += inFlow.get(division).getTotalFlow() * pop.getInfectedPercentage();
        }
        return total;
    }

    public double getTotalInfectedOutFlow() {
        double total = 0;
        for (AdminDivision division : outFlow.keySet()) {
            total += outFlow.get(division).getTotalFlow() * getInfectedPercentage();
        }
        return total;
    }

    public double getTotalRecoveredWithFlows() {
        double totalR = R.getTotalPopulation();
        for (AdminDivision division : inFlow.keySet()) {
            SVIRPopulation pop = (SVIRPopulation)division.population;
            totalR += inFlow.get(division).getTotalFlow() * pop.getRecoveredPercentage();
        }
        for (AdminDivision division : outFlow.keySet()) {
            totalR -= outFlow.get(division).getTotalFlow() * getRecoveredPercentage();
        }
        return totalR;
    }
}

package com.vaccines;

import com.vaccines.evaluations.EvaluationType;
import com.vaccines.models.SVIR;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

import java.util.Arrays;

public class SVIRProblem extends AbstractProblem {
    public SVIR model;
    public int lengthInWeeks;
    public int maxWeeklyVaccines;
    public int subdivisionCount;
    public EvaluationType evaluationType;
    int evaluation = 0;

    public SVIRProblem(SVIR model, int maxWeeklyVaccines, EvaluationType evaluationType) {
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
            solution.setVariable(i, new RealVariable(0, 1));
        }
        System.out.println("Starting new seed...");
        return solution;
    }

    @Override
    public void evaluate(Solution solution) {
        SVIR svir = new SVIR(model);
        int[][] vaccines = new int[lengthInWeeks][subdivisionCount];
        for (int i = 0; i < getNumberOfVariables(); ++i) { //format: (week1:) subdiv1, subdiv2,..., (week2:) subdiv1,etc
            double variable = ((RealVariable)solution.getVariable(i)).getValue();
            int week = i / subdivisionCount;
            int subdivision = i - week * subdivisionCount;
            vaccines[week][subdivision] = (int) (variable * maxWeeklyVaccines / subdivisionCount);
        }
        svir.setVaccineAvailability(vaccines);
        svir.simulate();
        double score = calculateObjective(svir);
        solution.setObjective(0, score);
        for (int i = 0; i < lengthInWeeks; ++i) {
            int sum = Arrays.stream(vaccines[i]).sum();
            boolean constraintSatisfied = sum <= maxWeeklyVaccines;
            solution.setConstraint(i, constraintSatisfied ? 0 : 1);
        }

        System.out.println("Evaluation " + evaluation++);
    }

    private double calculateObjective(SVIR svir) {
        if (evaluationType == EvaluationType.InfectedSum) {
            return svir.evaluation.infectedSum;
        }
        else if (evaluationType == EvaluationType.DeadSum) {
            return svir.evaluation.deadSum;
        }
        else if (evaluationType == EvaluationType.MostConcurrentInfected) {
            return svir.evaluation.mostConcurrentInfected;
        }
        return 0;
    }

//    public double[] parameterGridSearch() {
//        double[] bestParameters = new double[3];
//        double minError = Double.MAX_VALUE;
//        SVIR bestModel = null;
//        for (double p0 = 0.0001; p0 > 0.00000000001; p0 /= 1.05)
//        {
//            for (double p1 = 0.0001; p1 > 0.00000000001; p1 /= 1.05)
//            {
//                for (double p2 = 1.0 / 50; p2 < 1.0 / 4.0; p2 *= 1.05)
//                {
//                    model = new SVIR(simulationLength);
//                    loadPopulationFromCsv();
////                    model.setPopulations(S[0], V[0], I[0], R[0]);
////                    model.setParameters(p0, p1, 0.0, 0.0, p2);
//                    model.simulate();
//                    int[][] results = model.getResults();
//                    double error = calculateMeanSquareError(results);
//                    if (error < minError)
//                    {
//                        minError = error;
//                        bestParameters = new double[] { p0, p1, p2 };
//                        bestModel = model;
//                        System.out.println("Error = " + minError +
//                                "\nnatural death rate = " + p0 +
//                                "\nsusceptible transmission rate = " + p1 +
//                                "\nrecoveryRate = " + p2 + "\n");
//                    }
//                }
//            }
//        }
//        model = bestModel;
//        return bestParameters;
//    }

//    private double scale(double p) {
//        return (maxParameter - minParameter) * p + minParameter;
//    }
//
//    private void loadPopulationFromCsv() {
//        if (baseS != null) {
//            for (int i = 0; i < S.length; ++i) {
//                S[i] = baseS[i];
//                V[i] = baseV[i];
//                I[i] = baseI[i];
//                R[i] = baseR[i];
//            }
//        } else {
//            BufferedReader reader;
//            try {
//                reader = new BufferedReader(new FileReader("populations.csv"));
//
//                // Read infected
//                var line = reader.readLine();
//                var infected = List.of(line.split(",")).subList(startIndex, startIndex + simulationLength);
//
//                // Read removed (recovered + dead)
//                var line1 = reader.readLine();
//                var line2 = reader.readLine();
//                var recovered = List.of(line1.split(",")).subList(startIndex, startIndex + simulationLength);
//                var dead = List.of(line2.split(",")).subList(startIndex, startIndex + simulationLength);
//
//                R = new int[recovered.size()];
//                baseR = new int[recovered.size()];
//                I = new int[infected.size()];
//                baseI = new int[infected.size()];
//                for (int i = 0; i < recovered.size(); ++i) {
//                    R[i] = Integer.parseInt(recovered.get(i)) + Integer.parseInt(dead.get(i));
//                    baseR[i] = R[i];
//                    I[i] = Integer.parseInt(infected.get(i)) - R[i];
//                    baseI[i] = I[i];
//                }
//
//                // Assume no vaccines are available
//                int n = R.length;
//                V = new int[n];
//                baseV = new int[n];
//
//                // Calculate susceptible (all - (removed + infected))
//                S = new int[n];
//                baseS = new int[n];
//                for (int i = 0; i < n; ++i) {
//                    S[i] = startingPopulation - (I[i] + R[i]);
//                    baseS[i] = S[i];
//                }
//                reader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public double calculateMeanSquareError(int[][] results)
//    {
//        double population = startingPopulation;
//        int n = S.length;
//        var errS = new double[n];
//        var errV = new double[n];
//        var errI = new double[n];
//        var errR = new double[n];
//        double error = 0.0;
//        for (int i = 0; i < n; ++i) {
//            errS[i] = (S[i] - results[0][i]) / population * (S[i] - results[0][i]) / population;
//            errV[i] = (V[i] - results[1][i]) / population * (V[i] - results[1][i]) / population;
//            errI[i] = (I[i] - results[2][i]) / population * (I[i] - results[2][i]) / population;
//            errR[i] = (R[i] - results[3][i]) / population * (R[i] - results[3][i]) / population;
//            error += errS[i] + errV[i] + errI[i] + errR[i];
//        }
//        return error;
//    }
//
//    public void saveRealDataToFile() {
//        var data = new int[][]{baseS, baseV, baseI, baseR};
//        try {
//            FileWriter writer = new FileWriter("real_data.csv");
//            for (int[] arr: data)
//            {
//                for (int i = 0; i < arr.length; ++i) {
//                    writer.write(String.valueOf(arr[i]));
//                    if (i != arr.length - 1) {
//                        writer.write(",");
//                    }
//                }
//                writer.write("\n");
//            }
//            writer.close();
//        } catch (Exception ignored) {
//
//        }
//    }
}

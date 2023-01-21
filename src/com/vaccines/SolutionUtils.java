package com.vaccines;

import com.vaccines.areas.Country;
import com.vaccines.areas.Powiat;
import com.vaccines.models.EpidemiologicalModel;
import com.vaccines.models.ModelType;
import org.moeaframework.core.Solution;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class SolutionUtils {

    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm");

    // File system solution utils
    static void printVoivodeshipVaccinationRate(ArrayList<ArrayList<Double>> solution) {
        EpidemiologicalModel model = new EpidemiologicalModel(Config.SIMULATION_LENGTH, ModelType.SVIR);
        model.setVaccineAvailability(solution);
        model.simulate();

        ArrayList<String> usedNames = new ArrayList<>();

        for (Powiat powiat : model.country.powiaty) {
            String name = powiat.voivodeship;
            if (!usedNames.contains(name)) {
                usedNames.add(name);

                double pop = 0, vaccPercentage = 0;
                for (Powiat p : model.country.powiaty) {
                    if (Objects.equals(p.voivodeship, name)) {
                        pop += p.getPopulationCount();
                        vaccPercentage += p.getPopulationCount() * p.vaccinationPercentage.get(p.vaccinationPercentage.size() - 1);
                    }
                }
                vaccPercentage /= pop;
                System.out.println("Woj. " + name + ": " + Math.round(vaccPercentage * 1000.0) / 1000.0);
            }
        }
    }

    public static void writeSolutionToFile(Solution solution, String fileName, int weeks, int subdivisionCount) {
        try {
            FileWriter writer = new FileWriter("solutions\\" + fileName);

            for (int i = 0; i < weeks; i++) {
                for (int j = 0; j < subdivisionCount; j++) {
                    if (j != 0)
                        writer.append(", ");
                    int varIndex = i * subdivisionCount + j;
                    writer.append(String.valueOf(solution.getVariable(varIndex)));
                }
                writer.append("\n");
            }
            writer.close();
        }
        catch (Exception ex) {
            System.out.println("Error during saving to file: " + ex.toString());
            System.out.print("Saved solution: ");
            for (int i = 0; i < solution.getNumberOfVariables(); ++i) {
                System.out.print(String.valueOf(solution.getVariable(i)) + ", ");
            }
        }
    }

    public static ArrayList<ArrayList<Double>> loadSolutionFromFile(String fileName) {
        ArrayList<ArrayList<Double>> solution = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {

                String[] values = line.replace(" ", "").split(",");
                double sum = 0;
                ArrayList<Double> divisions = new ArrayList<>();

                for (String value : values) {
                    double d = Double.parseDouble(value);
                    sum += d;
                    divisions.add(d);
                }

                for (int i = 0; i < divisions.size(); ++i) {
                    double value = divisions.get(i) * Config.MAX_WEEKLY_VACCINES / sum;
                    divisions.set(i, value);
                }

                solution.add(divisions);
            }
        }
        catch (Exception ex) {
            System.out.print("Error during loading solution from file: " + ex.toString());
            return null;
        }
        return solution;
    }


    // Generating metaheuristic strategies
    public static ArrayList<ArrayList<Double>> getVaccinationPlanBasedOnTotalPopulation(Country country) {
        var plan = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> weekPlan = new ArrayList<>();
        double totalPopulation = country.getTotalPopulation();
        for (Powiat powiat : country.powiaty) {
            double weight = powiat.getPopulationCount() / totalPopulation;
            weekPlan.add(weight * Config.MAX_WEEKLY_VACCINES);
        }

        for (int i = 0; i < Config.SIMULATION_LENGTH / 7; ++i) {
            plan.add(weekPlan);
        }
        return plan;
    }

    public static ArrayList<ArrayList<Double>> getVaccinationPlanBasedOnPopulationDensity(Country country) {
        var plan = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> weekPlan = new ArrayList<>();
        double sum = 0;
        for (Powiat powiat : country.powiaty) {
            sum += (powiat.getPopulationCount() / powiat.area);
        }

        for (Powiat powiat : country.powiaty) {
            double weight = powiat.getPopulationCount() / powiat.area;
            weekPlan.add(weight / sum * Config.MAX_WEEKLY_VACCINES);
        }

        for (int i = 0; i < Config.SIMULATION_LENGTH / 7; ++i) {
            plan.add(weekPlan);
        }
        return plan;
    }

    public static ArrayList<ArrayList<Double>> getVaccinationPlanBasedOnTotalInflow(Country country) {
        var plan = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> weekPlan = new ArrayList<>();
        double sum = 0;

        for (Powiat powiat : country.powiaty) {
            sum += powiat.getPopulation().getTotalInFlow();
        }

        for (Powiat powiat : country.powiaty) {
            double weight = powiat.getPopulation().getTotalInFlow();
            weekPlan.add(weight / sum * Config.MAX_WEEKLY_VACCINES);
        }

        for (int i = 0; i < Config.SIMULATION_LENGTH / 7; ++i) {
            plan.add(weekPlan);
        }
        return plan;
    }

    public static ArrayList<ArrayList<Double>> getVaccinationPlanBasedOnTotalOutflow(Country country) {
        var plan = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> weekPlan = new ArrayList<>();
        double sum = 0;

        for (Powiat powiat : country.powiaty) {
            sum += powiat.getPopulation().getTotalInFlow();
        }

        for (Powiat powiat : country.powiaty) {
            double weight = powiat.getPopulation().getTotalOutFlow();
            weekPlan.add(weight / sum * Config.MAX_WEEKLY_VACCINES);
        }

        for (int i = 0; i < Config.SIMULATION_LENGTH / 7; ++i) {
            plan.add(weekPlan);
        }
        return plan;
    }
}

package com.vaccines.areas;

import com.vaccines.evaluations.Evaluation;
import com.vaccines.models.ModelType;
import com.vaccines.populations.Population;
import com.vaccines.populations.SVEIRPopulation;
import com.vaccines.populations.SVIRPopulation;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Stream;

public class Country {
    public String name;
    public ArrayList<Powiat> powiaty = new ArrayList<>();
    public Country() { }

    public Country(String name) {
        this.name = name;
    }

    public Country(Country country) {
        name = country.name;
        for (Powiat powiat : country.powiaty) {
            powiaty.add(new Powiat(powiat));
        }
    }

    public Evaluation simulateStep(int[] vaccines) {
        Evaluation evaluation = new Evaluation();
        int vaccineIndex = 0;
        for (Powiat powiat : powiaty) {
            evaluation.add(powiat.getPopulation().update(vaccines[vaccineIndex++] / 7));
        }
        return evaluation;
    }

    public void applyChanges() {
        for (Powiat powiat : powiaty) {
            powiat.applyChanges();
        }
    }

    public Powiat getPowiatByCode(String code) {
        return powiaty.stream().filter(p -> Objects.equals(p.code, code)).findFirst().orElse(null);
    }

    public Powiat getPowiatByName(String name) {
        return powiaty.stream().filter(p -> Objects.equals(p.name, name)).findFirst().orElse(null);
    }

    public void loadPolishData(ModelType type) {
        loadPolishData(type, 0);
    }

    private double totalPopulation = -1;
    public double getTotalPopulation() {
        if (totalPopulation == -1) {
            totalPopulation = 0;
            for (Powiat powiat: powiaty) {
                totalPopulation += powiat.getPopulationCount();
            }
        }
        return totalPopulation;
    }

    public void loadPolishData(ModelType type, int minFlowVolume) {
        name = "Poland";

        // powiat data
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("data/powiaty_dane.csv"));
            csvReader.readLine(); // header
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(";");
                Powiat powiat = new Powiat(data, this);
                Population population;
                if (type == ModelType.SVIR)
                    population = new SVIRPopulation(Integer.parseInt(data[5]));
                else
                    population = new SVEIRPopulation(Integer.parseInt(data[5]));
                powiat.setPopulation(population);
                powiaty.add(powiat);
            }
            csvReader.close();
        } catch (Exception e) {
            System.out.println("Error reading powiat data. " + e.toString());
        }

        // flow data
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("data/przeplywy_powiaty.csv"));
            Stream<String> stream = Arrays.stream(csvReader.readLine().split(";")).skip(2)
                    .map(p -> p.replace("powiat ", ""));
            var powiatNames = stream.toArray(String[]::new); // header
            csvReader.readLine(); // TERYT codes
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(";");
                String targetPowiatName = data[0].replace("powiat ", "");
                Powiat targetPowiat = getPowiatByName(targetPowiatName);
                stream = Arrays.stream(data).skip(2);
                data = stream.toArray(String[]::new);
                for (int i = 0; i < data.length; ++i) {
                    String s = data[i];
                    if (!StringUtils.isEmpty(s)) {
                        int flowVolume = Integer.parseInt(s);
                        Powiat sourcePowiat = getPowiatByName(powiatNames[i]);
                        if (!Objects.equals(sourcePowiat.name, targetPowiatName)) {
                            sourcePowiat.getPopulation().outFlow.put(targetPowiat, (double) flowVolume);
                            targetPowiat.getPopulation().inFlow.put(sourcePowiat, (double) flowVolume);
                        } else {
                            sourcePowiat.getPopulation().selfMigration = flowVolume;
                        }
                    }
                }
            }
            csvReader.close();
        } catch (Exception e) {
            System.out.println("Error reading flow data. " + e.toString());
        }

        // infected data
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("data/zarazenia.csv"));
            csvReader.readLine(); // headers
            csvReader.readLine();
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(";");
                String code = data[7].substring(1); // skips 't'
                Powiat powiat = getPowiatByCode(code);
                double numberOfInfected = Integer.parseInt(data[2]);
                powiat.initializeInfected(numberOfInfected);
            }
            csvReader.close();
        } catch (Exception e) {
            System.out.println("Error reading infection data. " + e.toString());
        }
        System.out.println("Finished reading historical data.");
    }
}

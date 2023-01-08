package com.vaccines.areas;

import com.vaccines.populations.Population;
import com.vaccines.populations.SVEIRPopulation;
import com.vaccines.populations.SVIRPopulation;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class Powiat {
    public String name;
    public String code;
    public Country country;
    public String voivodeship;
    public PowiatType type;
    private Population population;
    public Position position;
    public double area;

    public Powiat(String[] data, Country country) throws ParseException {
        this.code = getProperCode(data[0]);
        this.voivodeship = data[1];
        this.name = data[2];
        this.type = PowiatType.parseType(data[4]);
        this.position = new Position(Integer.parseInt(data[12]), Integer.parseInt(data[13]));
        this.country = country;
        // to load double values with comma as the decimal separator
        this.area = NumberFormat.getInstance(Locale.FRANCE).parse(data[3]).doubleValue();
    }

    public Powiat(Powiat powiat) {
        this.code = powiat.code;
        this.name = powiat.name;
        this.type = powiat.type;
        this.position = powiat.position;
        this.country = powiat.country;
        this.voivodeship = powiat.voivodeship;
        this.area = powiat.area;

        if (powiat.population instanceof SVIRPopulation)
            this.population = new SVIRPopulation((SVIRPopulation) powiat.population);
        else
            this.population = new SVEIRPopulation((SVEIRPopulation) powiat.population);
    }

    public void initializeInfected(double numberOfInfected) {
        if (population instanceof SVIRPopulation pop) {
            pop.I.changePopulations(numberOfInfected);
            pop.S.changePopulations(-numberOfInfected);
        }
        else if (population instanceof SVEIRPopulation pop) {
            pop.I.changePopulations(numberOfInfected);
            pop.S.changePopulations(-numberOfInfected);
        }
    }

    public void applyChanges() {
        population.applyChanges();
    }

    public double getPopulationCount() {
        return population.getTotalPopulation();
    }

    public Population getPopulation() {
        return population;
    }

    public void setPopulation(Population population) {
        this.population = population;
        population.powiat = this;
    }

    public String getProperCode(String code) {
        while (code.length() < 4) {
            code = "0" + code;
        }
        return code;
    }
}

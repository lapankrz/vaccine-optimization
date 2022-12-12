package com.vaccines.areas;

import com.vaccines.models.ModelType;
import com.vaccines.populations.Population;
import com.vaccines.populations.SVEIRPopulation;
import com.vaccines.populations.SVIRPopulation;

public class Commune extends AdminDivision {
    public int id;

    public Commune(int id, String code, String name, Population population, County county) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.population = population;
        this.higherDivision = county;
        this.isLowestDivision = true;
    }

    public Commune(String[] data, County county, ModelType type) {
        this.higherDivision = county;
        this.id = Integer.parseInt(data[0]);
        this.code = getProperCode(data[1]);
        this.name = data[2];
        this.isLowestDivision = true;

        int students = Integer.parseInt(data[3]);
        int adults = Integer.parseInt(data[4]);
        int seniors = Integer.parseInt(data[5]);

        if (type == ModelType.SVIR) {
            population = new SVIRPopulation(this, students, adults, seniors);
        }
        else if (type == ModelType.SVEIR) {
            population = new SVEIRPopulation(students, adults, seniors);
        }
    }

    public Commune(Commune commune) {
        super(commune);
        id = commune.id;
    }

    public String getProperCode(String code) {
        while (code.length() < 7) {
            code = "0" + code;
        }
        return code;
    }
}

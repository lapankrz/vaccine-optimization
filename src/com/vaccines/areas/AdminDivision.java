package com.vaccines.areas;

import com.vaccines.evaluations.Evaluation;
import com.vaccines.populations.Population;
import com.vaccines.populations.SVEIRPopulation;
import com.vaccines.populations.SVIRPopulation;
import java.util.ArrayList;
import java.util.Objects;

public class AdminDivision {
    public String code;
    public String name;
    public AdminDivision higherDivision;
    public ArrayList<AdminDivision> lowerDivisions = new ArrayList<>();
    public boolean isTopDivision = false, isLowestDivision = false;
    public Population population;

    public AdminDivision() {}

    public AdminDivision(AdminDivision division) {
        code = division.code;
        name = division.name;
        higherDivision = division.higherDivision;
        isTopDivision = division.isTopDivision;
        isLowestDivision = division.isLowestDivision;
        if (division.population instanceof SVIRPopulation) {
            population = new SVIRPopulation((SVIRPopulation)division.population);
        }
        else {
            population = new SVEIRPopulation((SVEIRPopulation)division.population);
        }
    }



    public void applyChanges(int levelsDown) {
        if (levelsDown > 0)
        {
            for (AdminDivision division : lowerDivisions) {
                division.applyChanges(levelsDown - 1);
            }
        }
        else {
            population.applyChanges();
        }
    }

    public AdminDivision getLowerDivision(String code, int levelsDown) {
        if (levelsDown <= 0)
            return null;
        for (AdminDivision division : lowerDivisions) {
            if (levelsDown >= 2) {
                AdminDivision div = division.getLowerDivision(code, levelsDown - 1);
                if (div != null)
                    return div;
            }
            else {
                if (Objects.equals(division.code, code))
                    return  division;
            }
        }
        return null;
    }

    public void propagateDataFromLowerDivisions() {
        ArrayList<Population> populations = new ArrayList<>();
        for (AdminDivision division : lowerDivisions) {
            populations.add(division.population);
        }
        if (populations.size() > 0) {
             if (populations.get(0) instanceof SVIRPopulation) {
                 this.population = new SVIRPopulation(populations);
             }
             else { // SVEIR
//                 this.population = new SVEIRPopulation(populations);
             }
        }
    }

    public double getTotalStudents() {
        return population.getTotalStudents();
    }

    public double getTotalAdults() {
        return population.getTotalAdults();
    }

    public double getTotalSeniors() {
        return population.getTotalSeniors();
    }

    public double getTotalPopulation() {
        return population.getTotalPopulation();
    }
}

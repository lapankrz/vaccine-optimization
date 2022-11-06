package com.vaccines.areas;

import com.vaccines.populations.SVIRPopulation;

public class County extends AdminDivision {
    public CountyType type;
    public Position position;

    public County(String code, String name, CountyType type, Position position, Voivodeship voivodeship) {
        this.code = getProperCode(code);
        this.name = name;
        this.type = type;
        this.position = position;
        this.higherDivision = voivodeship;
    }

    public County(String[] data, Voivodeship voivodeship) {
        this.higherDivision = voivodeship;
        this.code = getProperCode(data[0]);
        this.name = data[2];
        this.type = CountyType.parseType(data[4]);
        this.position = new Position(Integer.parseInt(data[12]), Integer.parseInt(data[13]));
    }

    public County(County county) {
        super(county);
        type = county.type;
        position = county.position;
        for (AdminDivision division : county.lowerDivisions) {
            Commune commune = (Commune) division;
            lowerDivisions.add(new Commune(commune));
        }
    }

    public void initializeInfected(double casesPer10k) {
        for (AdminDivision division : lowerDivisions) {
            Commune c = (Commune)division;
            if (c.population instanceof SVIRPopulation pop) {
                int infectedStudents = (int)Math.round(pop.getTotalStudents() * casesPer10k / 10000.0);
                int infectedAdults = (int)Math.round(pop.getTotalAdults() * casesPer10k / 10000.0);
                int infectedSeniors = (int)Math.round(pop.getTotalSeniors() * casesPer10k / 10000.0);
                pop.I.changePopulations(infectedStudents, infectedAdults, infectedSeniors);
                pop.S.changePopulations(-infectedStudents, -infectedAdults, -infectedSeniors);
            }
            else {
                //TODO: implement SVEIRPopulation
            }
        }
    }

    public String getProperCode(String code) {
        while (code.length() < 4) {
            code = "0" + code;
        }
        return code;
    }
}

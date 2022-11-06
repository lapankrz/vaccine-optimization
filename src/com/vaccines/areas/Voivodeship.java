package com.vaccines.areas;

public class Voivodeship extends AdminDivision {

    public Voivodeship() { }

    public Voivodeship(String code, String name, Country country)
    {
        this.code = code;
        this.name = name;
        this.higherDivision = country;
    }

    public Voivodeship(Voivodeship voivodeship) {
        super(voivodeship);
        for (AdminDivision division : voivodeship.lowerDivisions) {
            County county = (County) division;
            lowerDivisions.add(new County(county));
        }
    }
}

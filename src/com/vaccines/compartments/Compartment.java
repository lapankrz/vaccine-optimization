package com.vaccines.compartments;

public class Compartment {
    public CompartmentType type;
    public double size;
    public double diff;

    public Compartment(CompartmentType type, double size) {
        this.type = type;
        this.size = size;
    }

    public Compartment(Compartment compartment) {
        type = compartment.type;
        size = compartment.size;
        diff = compartment.diff;
    }

    public void applyChanges() {
        size += diff;
        if (size < 0)
            size = 0;
        diff = 0;
    }

    public void changePopulations(double diff) {
        this.diff = diff;
        applyChanges();
    }

    public void addPopulation(Compartment c) {
        diff = c.size;
        applyChanges();
    }
}

package com.vaccines.compartments;

public class Compartment {
    public CompartmentType type;
    public double students, adults, seniors;
    public double studentsDiff, adultsDiff, seniorsDiff;

    public Compartment(CompartmentType type, int students, int adults, int seniors) {
        this.type = type;
        this.students = students;
        this.adults = adults;
        this.seniors = seniors;
    }

    public Compartment(Compartment compartment) {
        type = compartment.type;
        students = compartment.students;
        adults = compartment.adults;
        seniors = compartment.seniors;
        studentsDiff = compartment.studentsDiff;
        adultsDiff = compartment.adultsDiff;
        seniorsDiff = compartment.seniorsDiff;
    }

    public void applyChanges() {
        students += studentsDiff;
        if (students < 0)
            students = 0;
        adults += adultsDiff;
        if (adults < 0)
            adults = 0;
        seniors += seniorsDiff;
        if (seniors < 0)
            seniors = 0;
        studentsDiff = adultsDiff = seniorsDiff = 0;
    }

    public void changePopulations(double studentsDiff, double adultsDiff, double seniorsDiff) {
        students += studentsDiff;
        adults += adultsDiff;
        seniors += seniorsDiff;
    }

    public double getTotalPopulation() {
        return students + adults + seniors;
    }

    public void addPopulation(Compartment c) {
        students += c.students;
        adults += c.adults;
        seniors += c.seniors;
    }
}

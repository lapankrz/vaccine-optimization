package com.vaccines.populations;

public class Flow {
    public double students, adults, seniors;

    public Flow(double students, double adults, double seniors) {
        this.students = students;
        this.adults = adults;
        this.seniors = seniors;
    }

    public Flow(Flow flow1, Flow flow2) {
        this.students = flow1.students + flow2.students;
        this.adults = flow1.adults + flow2.adults;
        this.seniors = flow1.seniors + flow2.seniors;
    }

    public double getTotalFlow() {
        return students + adults + seniors;
    }
}

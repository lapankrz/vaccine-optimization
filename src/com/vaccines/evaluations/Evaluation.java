package com.vaccines.evaluations;

public class Evaluation {
    public double infectedSum;
    public double mostConcurrentInfected;
    public double deadSum;

    public Evaluation(int infectedSum, int mostConcurrentInfected, int deadSum) {
        this.infectedSum = infectedSum;
        this.mostConcurrentInfected = mostConcurrentInfected;
        this.deadSum = deadSum;
    }

    public Evaluation() { }

    public void add(Evaluation evaluation) {
        this.infectedSum += evaluation.infectedSum;
        this.mostConcurrentInfected += evaluation.mostConcurrentInfected;
        this.deadSum += evaluation.deadSum;
    }
}

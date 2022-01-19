package com.vaccines;

public class SVIRGroup extends Group {
    int S, V, I, R;
    double naturalDeathRate = 0.01 / 365;
    double susceptibleTransmissionRate = 0.011, vaccinatedTransmissionRate = 0.011;
    double vaccineImmunizationRate = 1.0 / 14;
    double recoveryRate = 1.0 / 14;

    public SVIRGroup(int S, int V, int I, int R) {
        this.S = S;
        this.V = V;
        this.I = I;
        this.R = R;
    }

    public void setParameters(double naturalDeathRate, double susceptibleTransmissionRate,
                              double vaccinatedTransmissionRate, double vaccineImmunizationRate, double recoveryRate) {
        this.naturalDeathRate = naturalDeathRate;
        this.susceptibleTransmissionRate = susceptibleTransmissionRate;
        this.vaccinatedTransmissionRate = vaccinatedTransmissionRate;
        this.vaccineImmunizationRate = vaccineImmunizationRate;
        this.recoveryRate = recoveryRate;
    }

    @Override
    public void Update(Group g) {

    }
}

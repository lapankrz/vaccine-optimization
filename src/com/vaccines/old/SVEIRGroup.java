//package com.vaccines.groups;
//
//import com.vaccines.areas.MixingArea;
//import com.vaccines.areas.SVEIRMixingArea;
//import com.vaccines.areas.SVIRMixingArea;
//import com.vaccines.compartments.Compartment;
//import com.vaccines.compartments.CompartmentType;
//
//public class SVEIRGroup extends Group {
//    public Compartment S, V, E, I, R;
//
//    public SVEIRGroup(int[][] compartments) {
//        S = new Compartment(CompartmentType.Susceptible, compartments[0][0], compartments[0][1], compartments[0][2]);
//        V = new Compartment(CompartmentType.Vaccinated,compartments[1][0], compartments[1][1], compartments[1][2]);
//        E = new Compartment(CompartmentType.Exposed,compartments[2][0], compartments[2][1], compartments[2][2]);
//        I = new Compartment(CompartmentType.Infected,compartments[3][0], compartments[3][1], compartments[3][2]);
//        R = new Compartment(CompartmentType.Recovered,compartments[4][0], compartments[4][1], compartments[4][2]);
//    }
//
//    @Override
//    public void update(Group g, int[] vaccines) {
//        SVEIRGroup group = (SVEIRGroup)g;
//        updateSusceptible(group, vaccines);
//        updateVaccinated(group, vaccines);
//        updateExposed(group, vaccines);
//        updateInfected(group);
//        updateRecovered();
//    }
//
//    private void updateSusceptible(SVEIRGroup g, int[] vaccines) {
//        for ( MixingArea ma : travelFlux.keySet() ) {
//            SVEIRMixingArea area = (SVEIRMixingArea)ma;
//            int pop = getTotalPopulation();
//            S.adults -= (int)(area.interactionRate * area.exposedTransmissionRate * E.adults * S.adults / pop
//                    + area.interactionRate * area.infectedTransmissionRate * I.adults * S.adults / pop
//                    + vaccines[1] - area.naturalDeathRate * S.adults);
//        }
//    }
//
//    private void updateVaccinated(SVEIRGroup g, int[] vaccines) {
//        for ( MixingArea ma : travelFlux.keySet() ) {
//            SVEIRMixingArea area = (SVEIRMixingArea)ma;
//        }
//    }
//
//    private void updateExposed(SVEIRGroup g, int[] vaccines) {
//        for ( MixingArea ma : travelFlux.keySet() ) {
//            SVEIRMixingArea area = (SVEIRMixingArea)ma;
//        }
//    }
//
//    private void updateInfected(SVEIRGroup g) {
//        for ( MixingArea ma : travelFlux.keySet() ) {
//            SVEIRMixingArea area = (SVEIRMixingArea)ma;
//        }
//    }
//
//    private void updateRecovered() {
//        for ( MixingArea ma : travelFlux.keySet() ) {
//            SVEIRMixingArea area = (SVEIRMixingArea)ma;
//        }
//    }
//
//    @Override
//    public int getTotalPopulation() {
//        return getTotalStudents() + getTotalAdults() + getTotalSeniors();
//    }
//
//    @Override
//    public int getTotalStudents() {
//        return S.students + V.students + I.students + R.students;
//    }
//
//    @Override
//    public int getTotalAdults() {
//        return S.adults + V.adults + I.adults + R.adults;
//    }
//
//    @Override
//    public int getTotalSeniors() {
//        return S.seniors + V.seniors + I.seniors + R.seniors;
//    }
//}

package com.vaccines;

import com.vaccines.areas.Country;
import com.vaccines.models.ModelType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;

public class Plotter {

    public static void plotEvaluations(double[] infections, double[] mostConcurrent) {
        String title = "Łączna liczba zakażeń w zależności od dnia symulacji";
        String xAxisLabel = "Dzień symulacji";
        String yAxisLabel = "Łączna liczba zakażeń";

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("Łączna liczba zakażeń");
        for (int i = 0; i < infections.length; ++i) {
            series1.add(i, infections[i]);
        }
        dataset.addSeries(series1);

        XYSeries series2 = new XYSeries("Pik zakażeń");
        for (int i = 0; i < mostConcurrent.length; ++i) {
            series2.add(i, mostConcurrent[i]);
        }
        dataset.addSeries(series2);

        double[] dailyInfections = new double[infections.length - 1];
        for (int i = 0; i < infections.length - 1; i++) {
            dailyInfections[i] = infections[i + 1] - infections[i];
        }
        XYSeries series3 = new XYSeries("Dzienna liczba zakażeń");
        for (int i = 0; i < dailyInfections.length; ++i) {
            series3.add(i, dailyInfections[i]);
        }
        dataset.addSeries(series3);

        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

        ChartFrame frame = new ChartFrame("Demo", chart);
        frame.pack();
        frame.setVisible(true);
    }

    public static void plotByTotalPop(ArrayList<ArrayList<Double>> solution) {

        String title = "Waga przydziału szczepionek w zależności od populacji";
        String xAxisLabel = "Populacja powiatu";
        String yAxisLabel = "Waga przydziału szczepionek";

        Country country = new Country();
        country.loadPolishData(ModelType.SVIR);
        var powiaty = country.powiaty;

        XYSeriesCollection dataset = new XYSeriesCollection();

//        for (int j = 13; j < solution.size(); ++j) {
//            XYSeries series = new XYSeries("Week " + (j + 1));
//            for (int i = 0; i < solution.get(0).size(); ++i) {
//                var weight = solution.get(j).get(i);
//                var county = counties.get(i);
//                series.add(county.population.getTotalOutFlow() / county.population.getTotalPopulation(), weight);
//            }
//            dataset.addSeries(series);
//            break;
//        }

        XYSeries series = new XYSeries("Sum across all weeks");
        int divCount = solution.get(0).size();
        int weeks = solution.size();
        for (int divIndex = 0; divIndex < divCount; ++divIndex) {
            double sum = 0;
            for (ArrayList<Double> week : solution) {
                sum += week.get(divIndex);
            }
            var county = powiaty.get(divIndex);
            double x = county.getPopulation().getTotalInFlow() / county.getPopulationCount();
            double totpop = county.getPopulationCount();
            double y = sum / weeks;
            series.add(x, y);
        }
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

        ChartFrame frame = new ChartFrame("Demo", chart);
        frame.pack();
        frame.setVisible(true);
    }

    public static void plotByPopDensity(String fileName) {

    }

    public static void plotByTotalInflow() {

    }

    public static void plotByTotalOutflow() {

    }
}

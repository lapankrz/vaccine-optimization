package com.vaccines;

import com.vaccines.areas.Country;
import com.vaccines.areas.Powiat;
import com.vaccines.areas.PowiatType;
import com.vaccines.models.EpidemiologicalModel;
import com.vaccines.models.ModelType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Objects;

public class Plotter {

    public static ArrayList<ArrayList<Double>> getVaccinationPlanFromFile(String folderName, int evaluationNumber) {
        try {
            File folder = new File("./solutions/" + folderName);
            File[] listOfFiles = folder.listFiles();

            if (listOfFiles == null) {
                return null;
            }

            String fileName = null;
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().contains("eval" + evaluationNumber + "_")) {
                    fileName = file.getName();
                    break;
                }
            }
            if (fileName == null) {
                return null;
            }

            ArrayList<ArrayList<Double>> variables = new ArrayList<>();
            try {
                BufferedReader csvReader = new BufferedReader(new FileReader("./solutions/" + folderName + "/" + fileName));
                String row;
                while ((row = csvReader.readLine()) != null) {
                    ArrayList<Double> week = new ArrayList<>();
                    String[] data = row.split(",");
                    for (var d : data) {
                        week.add(Double.parseDouble(d));
                    }
                    variables.add(week);
                }
                csvReader.close();
            } catch (Exception e) {
                System.out.println("Error reading variable data. " + e);
            }

            return variables;
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public static void plotVaccinationPercentageByWeek(EpidemiologicalModel model, boolean summed) {

        XYSeries series1 = new XYSeries("Powiaty ziemskie");
        XYSeries series2 = new XYSeries("Powiaty grodzkie");
        XYSeries series3 = new XYSeries("Powiaty grodzkie powyżej 300 tys. mieszkańców");

        double grodzkiePop = 0, ziemskiePop = 0, over300kPop = 0;
        for (Powiat powiat : model.country.powiaty) {
            double pop = powiat.getPopulationCount();
            if (powiat.type == PowiatType.Grodzki) {
                grodzkiePop += pop;
                if (pop > Config.biggestCitiesThreshold) {
                    over300kPop += pop;
                }
            }
            else { // ziemski
                ziemskiePop += pop;
            }
        }

        for (int i = 0; i < Config.SIMULATION_LENGTH; ++i) {
            double grodzkiePercentage = 0, ziemskiePercentage = 0, over300kPercentage = 0;
            for (Powiat powiat : model.country.powiaty) {
                double pop = powiat.getPopulationCount();
                double vaccPercentage = powiat.vaccinationPercentage.get(i);

                if (!summed && i > 0) {
                    vaccPercentage -= powiat.vaccinationPercentage.get(i-1);
                }

                if (powiat.type == PowiatType.Grodzki) {
                    grodzkiePercentage += vaccPercentage * pop / grodzkiePop;
                    if (pop > Config.biggestCitiesThreshold) {
                        over300kPercentage += vaccPercentage * pop / over300kPop;
                    }
                }
                else { // ziemski
                    ziemskiePercentage += vaccPercentage * pop / ziemskiePop;
                }
            }
            series1.add(i, 100 * ziemskiePercentage);
            series2.add(i, 100 * grodzkiePercentage);
            series3.add(i, 100 * over300kPercentage);
        }

        String title = "";
        String xAxisLabel = "Dzień symulacji";
        String yAxisLabel = "Dzienna zaszczepiona część populacji (%)";

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);

        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);
        ChartFrame frame = new ChartFrame("Demo", chart);
        frame.pack();
        frame.setVisible(true);
    }

    // objective = "mci" || "inf"
    public static void plotObjectiveByEvaluation(String[] folderNames, String[] seriesNames, String objective) {

        String title = "Łączna liczba zakażeń w zależności od numeru iteracji";
        if (Objects.equals(objective, "mci"))
            title = "Pik zakażeń w zależności od numeru iteracji";

        String xAxisLabel = "Numer iteracji";
        String yAxisLabel = "Liczba osób";

        XYSeriesCollection dataset = new XYSeriesCollection();
        int min = Integer.MAX_VALUE, max = 0;

        for (int i = 0; i < folderNames.length; ++i) {

            File folder = new File("./solutions/" + folderNames[i]);
            File[] listOfFiles = folder.listFiles();

            XYSeries series = new XYSeries(seriesNames[i]);

            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        String[] parts = file.getName().split("_");
                        int eval = Integer.parseInt(parts[1].replace("eval", ""));

                        int val;
                        if (Objects.equals(objective, "inf")) {
                            val = Integer.parseInt(parts[2].replace("inf", ""));
                        }
                        else {
                            val = Integer.parseInt(parts[3].replace("mci", ""));
                        }

                        min = Math.min(min, val);
                        max = Math.max(max, val);

                        series.add(eval, val);
                    }
                }
            }
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

        chart.getXYPlot().getRangeAxis().setRange(min - 100000, max + 100000);

        ChartFrame frame = new ChartFrame("", chart);
        frame.pack();
        frame.setVisible(true);
    }

    public static void plotBothObjectivesByEvaluation(String folderName) {
        String title = "Funkcje celu w zależności od numeru iteracji";
        String xAxisLabel = "Numer iteracji";
        String yAxisLabel = "Liczba osób";

        File folder = new File("./solutions/" + folderName);
        File[] listOfFiles = folder.listFiles();

        XYSeriesCollection dataset1 = new XYSeriesCollection();
        XYSeriesCollection dataset2 = new XYSeriesCollection();
        int min1 = Integer.MAX_VALUE, max1 = 0;
        int min2 = Integer.MAX_VALUE, max2 = 0;

        XYSeries series1 = new XYSeries("Łączna liczba zakażeń");
        XYSeries series2 = new XYSeries("Pik zakażeń");

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    String[] parts = file.getName().split("_");
                    int eval = Integer.parseInt(parts[1].replace("eval", ""));

                    int inf = Integer.parseInt(parts[2].replace("inf", ""));
                    min1 = Math.min(min1, inf);
                    max1 = Math.max(max1, inf);
                    series1.add(eval, inf);

                    int mci = Integer.parseInt(parts[3].replace("mci", ""));
                    min2 = Math.min(min2, mci);
                    max2= Math.max(max2, mci);
                    series2.add(eval, mci);
                }
            }
        }
        dataset1.addSeries(series1);
        dataset2.addSeries(series2);

        JFreeChart chart1 = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset1, PlotOrientation.VERTICAL, true, true, false);
        JFreeChart chart2 = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset2, PlotOrientation.VERTICAL, true, true, false);

        chart1.getXYPlot().getRangeAxis().setRange(min1 - 100000, max1 + 100000);
        chart2.getXYPlot().getRangeAxis().setRange(min2 - 100000, max2 + 100000);

        XYPlot plot = (XYPlot) chart2.getPlot();
        plot.getRenderer().setSeriesPaint(0, Color.blue);

        JFrame frame = new JFrame();
        frame.setLayout( new FlowLayout() );
        frame.getContentPane().add(new ChartPanel(chart1));
        frame.getContentPane().add(new ChartPanel(chart2));
        frame.pack();
        frame.setVisible(true);
    }

    // objective = "mci" || "inf"
    public static void plotObjectiveByEvaluation(String folderName, String objective) {

        String title = "Funkcje celu w zależności od numeru iteracji";
        String xAxisLabel = "Numer iteracji";
        String yAxisLabel = "Liczba osób";

        File folder = new File("./solutions/" + folderName);
        File[] listOfFiles = folder.listFiles();

        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries series = new XYSeries("Łączna liczba zakażeń");
        if (Objects.equals(objective, "mci"))
            series = new XYSeries("Pik zakażeń");

        int min = Integer.MAX_VALUE, max = 0;

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    String[] parts = file.getName().split("_");
                    int eval = Integer.parseInt(parts[1].replace("eval", ""));

                    int val;
                    if (Objects.equals(objective, "inf")) {
                        val = Integer.parseInt(parts[2].replace("inf", ""));
                    }
                    else {
                        val = Integer.parseInt(parts[3].replace("mci", ""));
                    }

                    min = Math.min(min, val);
                    max = Math.max(max, val);

                    series.add(eval, val);
                }
            }
        }

        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

        chart.getPlot().setBackgroundPaint( Color.WHITE );
        chart.getXYPlot().getRangeAxis().setRange(min - 100000, max + 100000);

        ChartFrame frame = new ChartFrame("", chart);
        frame.pack();
        frame.setVisible(true);
    }

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

    public static void plotEvalForContactRateValues(EpidemiologicalModel model) {
        EpidemiologicalModel m1 = new EpidemiologicalModel(model);
        EpidemiologicalModel m2 = new EpidemiologicalModel(model);

        Config.contactRate = 0.514;
        m1.simulate();

        Config.contactRate = 0.3;
        m2.simulate();

        String title = "Łączna liczba zakażeń w zależności od dnia symulacji";
        String xAxisLabel = "Dzień symulacji";
        String yAxisLabel = "Łączna liczba zakażeń";

        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries series1 = new XYSeries("contactRate = 0.514");
        for (int i = 0; i < m1.infections.length; ++i) {
            series1.add(i, m1.infections[i]);
        }
        dataset.addSeries(series1);

        XYSeries series2 = new XYSeries("contactRate = 0.3");
        for (int i = 0; i < m2.infections.length; ++i) {
            series2.add(i, m2.infections[i]);
        }
        dataset.addSeries(series2);

        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);
        ChartFrame frame = new ChartFrame("Demo", chart);
        frame.pack();
        frame.setVisible(true);
    }

    public static void plotByTotalPop(ArrayList<ArrayList<Double>> solution) {

        String title = "Waga przydziału szczepionek w zależności od populacji";
        String xAxisLabel = "Populacja powiatu";
        String yAxisLabel = "Średni tygodniowy przydział szczepionek";

        Country country = new Country();
        country.loadPolishData(ModelType.SVIR);
        var powiaty = country.powiaty;

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeriesCollection mavg_dataset = new XYSeriesCollection();

        XYSeries series = new XYSeries("Średni tygodniowy przydział");
        XYSeries mavg_series = new XYSeries("Średni tygodniowy przydział");
        int divCount = solution.get(0).size();
        int weeks = solution.size();
        for (int divIndex = 0; divIndex < divCount; ++divIndex) {
            double sum = 0;
            for (ArrayList<Double> week : solution) {
                sum += week.get(divIndex);
            }
            var county = powiaty.get(divIndex);
            double totpop = county.getPopulationCount();
            double y = sum / weeks;

            series.add(totpop, y);
            if (totpop < 300000) {
                mavg_series.add(totpop, y);
            }
        }
        dataset.addSeries(series);
        mavg_dataset.addSeries(mavg_series);

        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

        final XYDataset dataset2 = MovingAverage.createMovingAverage(mavg_dataset, " - średnia krocząca", 50000, 0);
        final XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDataset(1, dataset2);
        var renderer = new StandardXYItemRenderer();
        renderer.setSeriesStroke(
            0,
            new BasicStroke(
                    2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1.0f, new float[] {1.0f, 1.0f}, 0.0f
            ));
        plot.setRenderer(1, renderer);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        ChartFrame frame = new ChartFrame("Demo", chart);
        frame.pack();
        frame.setVisible(true);
    }

    public static void plotByPopDensity(ArrayList<ArrayList<Double>> solution) {
        String title = "Waga przydziału szczepionek w zależności od populacji";
        String xAxisLabel = "Gęstość zaludnienia powiatu";
        String yAxisLabel = "Średni tygodniowy przydział szczepionek";

        Country country = new Country();
        country.loadPolishData(ModelType.SVIR);
        var powiaty = country.powiaty;

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeriesCollection mavg_dataset = new XYSeriesCollection();

        XYSeries series = new XYSeries("Średni tygodniowy przydział");
        XYSeries mavg_series = new XYSeries("Średni tygodniowy przydział");
        int divCount = solution.get(0).size();
        int weeks = solution.size();
        for (int divIndex = 0; divIndex < divCount; ++divIndex) {
            double sum = 0;
            for (ArrayList<Double> week : solution) {
                sum += week.get(divIndex);
            }
            var county = powiaty.get(divIndex);
            double totpop = county.getPopulationCount();
            double x = totpop / county.area;
            double y = sum / weeks;

            series.add(x, y);
            if (totpop < 300000) {
                mavg_series.add(x, y);
            }
        }
        dataset.addSeries(series);
        mavg_dataset.addSeries(mavg_series);

        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

        final XYDataset dataset2 = MovingAverage.createMovingAverage(mavg_dataset, " - średnia krocząca", 500, 0);
        final XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDataset(1, dataset2);
        var renderer = new StandardXYItemRenderer();
        renderer.setSeriesStroke(
                0,
                new BasicStroke(
                        2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1.0f, new float[] {1.0f, 1.0f}, 0.0f
                ));
        plot.setRenderer(1, renderer);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        ChartFrame frame = new ChartFrame("Demo", chart);
        frame.pack();
        frame.setVisible(true);
    }

    public static void plotByTotalInflow(ArrayList<ArrayList<Double>> solution) {
        String title = "Waga przydziału szczepionek w zależności od populacji";
        String xAxisLabel = "Suma migracji wejściowej w stosunku do całości populacji";
        String yAxisLabel = "Średni tygodniowy przydział szczepionek";

        Country country = new Country();
        country.loadPolishData(ModelType.SVIR);
        var powiaty = country.powiaty;

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeriesCollection mavg_dataset = new XYSeriesCollection();

        XYSeries series = new XYSeries("Średni tygodniowy przydział");
        XYSeries mavg_series = new XYSeries("Średni tygodniowy przydział");
        int divCount = solution.get(0).size();
        int weeks = solution.size();
        for (int divIndex = 0; divIndex < divCount; ++divIndex) {
            double sum = 0;
            for (ArrayList<Double> week : solution) {
                sum += week.get(divIndex);
            }
            var county = powiaty.get(divIndex);
            double totpop = county.getPopulationCount();
            double x = county.getPopulation().getTotalInFlow() / totpop;
            double y = sum / weeks;

            series.add(x, y);
            if (totpop < 300000) {
                mavg_series.add(x, y);
            }
        }
        dataset.addSeries(series);
        mavg_dataset.addSeries(mavg_series);

        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

        final XYDataset dataset2 = MovingAverage.createMovingAverage(mavg_dataset, " - średnia krocząca", 0.05, 0);
        final XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDataset(1, dataset2);
        var renderer = new StandardXYItemRenderer();
        renderer.setSeriesStroke(
                0,
                new BasicStroke(
                        2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1.0f, new float[] {1.0f, 1.0f}, 0.0f
                ));
        plot.setRenderer(1, renderer);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        ChartFrame frame = new ChartFrame("Demo", chart);
        frame.pack();
        frame.setVisible(true);
    }

    public static void plotByTotalOutflow(ArrayList<ArrayList<Double>> solution) {
        String title = "Waga przydziału szczepionek w zależności od populacji";
        String xAxisLabel = "Suma migracji wyjściowej w stosunku do całości populacji";
        String yAxisLabel = "Średni tygodniowy przydział szczepionek";

        Country country = new Country();
        country.loadPolishData(ModelType.SVIR);
        var powiaty = country.powiaty;

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeriesCollection mavg_dataset = new XYSeriesCollection();

        XYSeries series = new XYSeries("Średni tygodniowy przydział");
        XYSeries mavg_series = new XYSeries("Średni tygodniowy przydział");
        int divCount = solution.get(0).size();
        int weeks = solution.size();
        for (int divIndex = 0; divIndex < divCount; ++divIndex) {
            double sum = 0;
            for (ArrayList<Double> week : solution) {
                sum += week.get(divIndex);
            }
            var county = powiaty.get(divIndex);
            double totpop = county.getPopulationCount();
            double x = county.getPopulation().getTotalOutFlow() / totpop;
            double y = sum / weeks;

            series.add(x, y);
            if (x < 0.15) {
                mavg_series.add(x, y);
            }
        }
        dataset.addSeries(series);
        mavg_dataset.addSeries(mavg_series);

        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

        final XYDataset dataset2 = MovingAverage.createMovingAverage(mavg_dataset, " - średnia krocząca", 0.03, 0);
        final XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDataset(1, dataset2);
        var renderer = new StandardXYItemRenderer();
        renderer.setSeriesStroke(
                0,
                new BasicStroke(
                        2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1.0f, new float[] {1.0f, 1.0f}, 0.0f
                ));
        plot.setRenderer(1, renderer);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        ChartFrame frame = new ChartFrame("Demo", chart);
        frame.pack();
        frame.setVisible(true);
    }

    public static void plotByMigrationBalance(ArrayList<ArrayList<Double>> solution) {
        String title = "Waga przydziału szczepionek w zależności od populacji";
        String xAxisLabel = "Saldo migracji";
        String yAxisLabel = "Średni tygodniowy przydział szczepionek";

        Country country = new Country();
        country.loadPolishData(ModelType.SVIR);
        var powiaty = country.powiaty;

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeriesCollection mavg_dataset = new XYSeriesCollection();

        XYSeries series = new XYSeries("Średni tygodniowy przydział");
        XYSeries mavg_series = new XYSeries("Średni tygodniowy przydział");
        int divCount = solution.get(0).size();
        int weeks = solution.size();
        for (int divIndex = 0; divIndex < divCount; ++divIndex) {
            double sum = 0;
            for (ArrayList<Double> week : solution) {
                sum += week.get(divIndex);
            }
            var county = powiaty.get(divIndex);
            double totpop = county.getPopulationCount();
            double x = (county.getPopulation().getTotalInFlow() - county.getPopulation().getTotalOutFlow()) / totpop;
            double y = sum / weeks;

            series.add(x, y);
            if (x < 0.15 && x > -0.1) {
                mavg_series.add(x, y);
            }
        }
        dataset.addSeries(series);
        mavg_dataset.addSeries(mavg_series);

        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

        final XYDataset dataset2 = MovingAverage.createMovingAverage(mavg_dataset, " - średnia krocząca", 0.05, 0);
        final XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDataset(1, dataset2);
        var renderer = new StandardXYItemRenderer();
        renderer.setSeriesStroke(
                0,
                new BasicStroke(
                        2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1.0f, new float[] {1.0f, 1.0f}, 0.0f
                ));
        plot.setRenderer(1, renderer);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        ChartFrame frame = new ChartFrame("Demo", chart);
        frame.pack();
        frame.setVisible(true);
    }
}

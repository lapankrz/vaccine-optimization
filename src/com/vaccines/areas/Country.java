package com.vaccines.areas;

import com.vaccines.evaluations.Evaluation;
import com.vaccines.models.ModelType;
import com.vaccines.populations.Flow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Country extends AdminDivision {

    public Country() {
        this.isTopDivision = true;
    }

    public Country(String name) {
        this.name = name;
        this.isTopDivision = true;
    }

    public Country(Country country) {
        super(country);
        for (AdminDivision division : country.lowerDivisions) {
            Voivodeship voivodeship = (Voivodeship) division;
            lowerDivisions.add(new Voivodeship(voivodeship));
        }
    }

    public Evaluation simulateStep(int[] vaccines, int levelsDown) {
        Evaluation evaluation = new Evaluation();
        int vaccineIndex = 0;

        if (levelsDown == 0)
        {
            evaluation = population.update(vaccines[0] / 7);
        }
        else
        {
            for (AdminDivision voivodeship : lowerDivisions)
            {
                if (levelsDown == 1)
                {
                    evaluation.add(voivodeship.population.update(vaccines[vaccineIndex++] / 7));
                }
                else
                {
                    for (AdminDivision county : voivodeship.lowerDivisions)
                    {
                        if (levelsDown == 2)
                        {
                            evaluation.add(county.population.update(vaccines[vaccineIndex++] / 7));
                        }
                        else
                        {
                            for (AdminDivision commune : county.lowerDivisions)
                            {
                                evaluation.add(commune.population.update(vaccines[vaccineIndex++] / 7));
                            }
                        }
                    }
                }
            }
        }

        return evaluation;
    }

    public Voivodeship getVoivodeship(String code) {
        return (Voivodeship) getLowerDivision(code, 1);
    }

    public County getCounty(String code) {
        return (County) getLowerDivision(code, 2);
    }

    public Commune getCommune(String code) {
        return (Commune) getLowerDivision(code, 3);
    }

    public void loadPolishData(ModelType type) {
        loadPolishData(type, 0);
    }

    public ArrayList<AdminDivision> getAllDivisionsOnLevel(int administrativeLevel) {
        ArrayList<AdminDivision> divisions = new ArrayList<>();
        if (administrativeLevel == 0) {
            divisions.add(this);
        }
        else {
            for (AdminDivision voivodeship : this.lowerDivisions) {
                if (administrativeLevel == 1) { // voivodeship count
                    divisions.add(voivodeship);
                } else {
                    for (AdminDivision county : voivodeship.lowerDivisions) {
                        if (administrativeLevel == 2) { // county count
                            divisions.add(county);
                        }
                        else {
                            divisions.addAll(county.lowerDivisions);
                        }
                    }
                }
            }
        }
        return divisions;
    }

    public void loadPolishData(ModelType type, int minFlowVolume) {
        name = "Poland";

        // voivodeship data
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("data/wojewodztwa_id.csv"));
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(";");
                Voivodeship voivodeship = new Voivodeship(data[0], data[1], this);
                lowerDivisions.add(voivodeship);
            }
            csvReader.close();
        } catch (Exception e) {
            System.out.println("Voivodeship data file not found");
        }

        // county data
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("data/powiaty_dane.csv"));
            csvReader.readLine(); // header
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(";");
                Voivodeship v = (Voivodeship) lowerDivisions.stream().filter(el -> Objects.equals(el.name, data[1])).toList().get(0);
                County county = new County(data, v);
                v.lowerDivisions.add(county);
            }
            csvReader.close();
        } catch (Exception e) {
            System.out.println("County data file not found");
        }

        // commune data
        for (AdminDivision division : lowerDivisions) {
            Voivodeship v = (Voivodeship) division;
            try {
                BufferedReader csvReader = new BufferedReader(new FileReader("data/gminy_dane.csv"));
                csvReader.readLine(); // header
                String row;
                while ((row = csvReader.readLine()) != null) {
                    String[] data = row.split(";");
                    Object[] counties = v.lowerDivisions.stream().filter(
                            c -> {
                                String code = data[1];
                                while (code.length() < 7) {
                                    code = "0" + code;
                                }
                                return code.startsWith(c.code);
                            }
                    ).toArray();
                    if (counties.length > 0) {
                        County county = (County) counties[0];
                        Commune commune = new Commune(data, county, type);
                        county.lowerDivisions.add(commune);
                    }
                }
                csvReader.close();
            } catch (Exception e) {
                System.out.println("County data file not found");
            }
        }

        // flow data for adults
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("data/przeplywy_gminy_pracujacy.csv"));
            csvReader.readLine(); // header
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(";");
                Commune from = getCommune(data[0]);
                Commune to = getCommune(data[1]);
                if (from != null && to != null) {
                    int volume = Integer.parseInt(data[2]);
                    if (volume > 0 && volume >= minFlowVolume) {
                        Flow flow = new Flow(0, volume, 0);
                        from.population.outFlow.put(to, flow);
                        to.population.inFlow.put(from, flow);
                    }
                }
            }
            csvReader.close();
        } catch (Exception e) {
            System.out.println("County data file not found");
        }

        // flow data for students
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("data/przeplywy_gminy_uczniowie.csv"));
            csvReader.readLine(); // header
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(";");
                String fromCode = data[2];
                while (fromCode.length() < 7) {
                    fromCode = "0" + fromCode;
                }
                String toCode = data[0];
                while (toCode.length() < 7) {
                    toCode = "0" + toCode;
                }
                Commune from = getCommune(fromCode);
                Commune to = getCommune(toCode);
                if (from != null && to != null) {
                    int volume = Integer.parseInt(data[5]);
                    if (volume > 0 && volume >= minFlowVolume) {
                        Flow flow = new Flow(volume, 0, 0);
                        HashMap<AdminDivision, Flow> fromMap = from.population.outFlow;
                        HashMap<AdminDivision, Flow> toMap = to.population.inFlow;
                        if (fromMap.containsKey(to)) { // update existing flow
                            Flow adultFlow = fromMap.get(to);
                            flow.adults = adultFlow.adults;
                            fromMap.remove(to);
                            toMap.remove(from);
                        }
                        fromMap.put(to, flow);
                        toMap.put(from, flow);
                    }
                }
            }
            csvReader.close();
        } catch (Exception e) {
            System.out.println("County data file not found");
        }

        // infected data
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("data/zarazenia.csv"));
            csvReader.readLine(); // headers
            csvReader.readLine();
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(";");
                String code = data[7].substring(1); // skips 't'
                County county = getCounty(code);
                double casesPer10k = Double.parseDouble(data[3]);
                county.initializeInfected(casesPer10k);
            }
            csvReader.close();
        } catch (Exception e) {
            System.out.println("Infected data file not found");
        }

        for (AdminDivision voivodeship : lowerDivisions) {
            for (AdminDivision county : voivodeship.lowerDivisions) {
                for (AdminDivision commune : county.lowerDivisions) {
                    double studentFlow = commune.population.getTotalStudents() - commune.population.getTotalStudentOutFlow();
                    double adultFlow = commune.population.getTotalAdults() - commune.population.getTotalAdultOutFlow();
                    double seniorFlow = commune.population.getTotalSeniors() - commune.population.getTotalSeniorOutFlow();
                    Flow flow = new Flow(studentFlow, adultFlow, seniorFlow);

                    if (commune.population.inFlow.containsKey(commune)) {
                        commune.population.inFlow.replace(commune, flow);
                        commune.population.outFlow.replace(commune, flow);
                    } else {
                        commune.population.inFlow.put(commune, flow);
                        commune.population.outFlow.put(commune, flow);
                    }
                }
            }
        }

        // aggregate data from lower subdivisions
        for (AdminDivision voivodeship : lowerDivisions) {
            for (AdminDivision county : voivodeship.lowerDivisions) {
                county.propagateDataFromLowerDivisions();
            }
        }
        for (AdminDivision voivodeship : lowerDivisions) {
            voivodeship.propagateDataFromLowerDivisions();
        }
        propagateDataFromLowerDivisions();
    }
}

package com.ericmguimaraes.gaso.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by adrianodias on 4/29/17.
 */

public class Consumption implements Serializable {

    private int verylow = 0;
    private int low = 0;
    private int average = 0;
    private int high = 0;
    private int veryhigh = 0;

    public void incrementVeryLow() {
        verylow++;
    }
    public void incrementLow() {
        low++;
    }
    public void incrementAverage() {
        average++;
    }
    public void incrementHigh() {
        high++;
    }
    public void incrementVeryHigh() {
        veryhigh++;
    }

    public int getVerylow() {
        return verylow;
    }

    public int getLow() {
        return low;
    }

    public int getAverage() {
        return average;
    }

    public int getHigh() {
        return high;
    }

    public int getVeryhigh() {
        return veryhigh;
    }

    public void incrementComsuption(String str) {
        if (str.equals("muito_baixo")) {
            incrementVeryLow();
        } else if (str.equals("baixo")) {
            incrementLow();
        } else if (str.equals("medio")) {
            incrementAverage();
        } else if (str.equals("alto")) {
            incrementHigh();
        } else if (str.equals("muito_alto")) {
            incrementVeryHigh();
        }
    }

    public int getComsumptionValue(String consumption) {
        switch (consumption) {
            case "muito_baixo":
                return verylow;
            case "baixo":
                return low;
            case "medio":
                return average;
            case "alto":
                return high;
            case "muito_alto":
                return veryhigh;
            default:
                return 0;
        }
    }

//    public int[] getValuesOfComsumptions() {
//        return new int[] {verylow, low, average, high, veryhigh};
//    }
//    public String[] getNamesOfComsumptions() {
//        return new String[] {"muito_baixo", "baixo", "medio", "alto", "muito_alto"};
//    }

    public List<Integer> getValuesOfComsumptions() {
        List<Integer> c = new ArrayList<>();
        c.add(verylow);
        c.add(low);
        c.add(average);
        c.add(high);
        c.add(veryhigh);
        return c;
    }

    public List<String> getNamesOfComsumptions() {
        List<String> c = new ArrayList<>();
        c.add("muito_baixo");
        c.add("baixo");
        c.add("medio");
        c.add("alto");
        c.add("muito_alto");
        return c;
    }

    public String mostFrequentConsumptionName() {
        int max = 0;
        int indexOfMax = 0;
        List<Integer> comsumptions = getValuesOfComsumptions();
        for (Integer comsumption : comsumptions) {
            if (comsumption > max) {
                max = comsumption;
                indexOfMax = comsumptions.indexOf(comsumption);
            }
        }

        return getNamesOfComsumptions().get(indexOfMax);
    }

}

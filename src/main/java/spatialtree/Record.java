package main.java.spatialtree;

import java.io.Serializable;
import java.util.ArrayList;


public class Record implements Serializable {
    private long id; // The unique id of the record

    private String name;
    private ArrayList<Double> coordinates; // ArrayList with the coordinates of the Record's point



    Record(long id, String name, ArrayList<Double> coordinates) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
    }

    long getId() {
        return id;
    }


    double getCoordinate(int dimension)
    {
        return coordinates.get(dimension);
    }

    @Override
    public String toString() {
        StringBuilder recordToString = new StringBuilder(id + "," + name + "," + coordinates.get(0));
        for(int i = 1; i < coordinates.size(); i++)
            recordToString.append(",").append(coordinates.get(i));
        return String.valueOf(recordToString);
    }
}
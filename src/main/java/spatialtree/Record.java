package main.java.spatialtree;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a spatial record.
 */
public class Record implements Serializable {
    private long id; // The unique id of the record

    private String name;
    private ArrayList<Double> coordinates; // ArrayList with the coordinates of the Record's point



    Record(long id, String name, ArrayList<Double> coordinates) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
    }

    public long getId() {
        return id;
    }


    public double getCoordinate(int dimension)
    {
        return coordinates.get(dimension);
    }

    /**
     * Provides a string representation of the record, for display purposes.
     *
     * @return A string that represents the record, including its ID, name, and coordinates.
     */
    @Override
    public String toString() {
        StringBuilder recordToString = new StringBuilder(id + "," + name + "," + coordinates.get(0));
        for(int i = 1; i < coordinates.size(); i++)
            recordToString.append(",").append(coordinates.get(i));
        return String.valueOf(recordToString);
    }
}

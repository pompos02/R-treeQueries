package main.java.spatialtree;

import java.util.ArrayList;

public class SpatialDataEntry {
    private long id; // Unique identifier for the entry
    private String name; // Human-readable name of the entry
    private ArrayList<Double> coordinates; // Array to store multi-dimensional coordinates

    /**
     * Constructs a new SpatialDataEntry with given id, name, and coordinates.
     * @param id The unique identifier for the spatial data entry.
     * @param name The name of the location or point of interest.
     * @param coordinates The coordinates of the entry in a multi-dimensional space.
     */
    public SpatialDataEntry(long  id, String name, ArrayList<Double> coordinates) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Double> getCoordinates() {
        return coordinates;
    }

    // Method to get the coordinate for a specific dimension
    public Double getCoordinate(int dimension) {
        return coordinates.get(dimension);
    }


}

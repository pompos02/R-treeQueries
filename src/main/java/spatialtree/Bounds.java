package main.java.spatialtree;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
/**
 * Represents the bounds of an interval in a single dimension.
 */
public class Bounds implements Serializable {
    private double lower; // Representing the lower value of the interval
    private double upper; // Representing the upper value of the interval

    /**
     * Constructor for Bounds.
     *
     * @param lower The lower bound of the interval.
     * @param upper The upper bound of the interval.
     * @throws IllegalArgumentException If the lower bound is greater than the upper bound.
     */

    public Bounds(double lower, double upper) {
        if (lower <= upper)
        {
            this.lower = lower;
            this.upper = upper;
        }
        else
            throw new IllegalArgumentException( "The lower value of the bounds cannot be bigger than the upper");
    }

    /**
     * Gets the lower bound of the interval.
     *
     * @return The lower bound.
     */
    public double getLower() {
        return lower;
    }

    /**
     * Gets the upper bound of the interval.
     *
     * @return The upper bound.
     */
    double getUpper() {
        return upper;
    }

    /**
     * Finds the minimum bounds needed to encompass a set of entries in all dimensions.
     * This method is used when you want to create a bounding box that encloses multiple entries.
     *
     * @param entries An ArrayList of Entry objects to encompass within the bounds.
     * @return An ArrayList of Bounds objects representing the minimum bounds.
     */

    static ArrayList<Bounds> findMinimumBounds(ArrayList<Entry> entries) {
        ArrayList<Bounds> minimumBounds = new ArrayList<>();
        // For each dimension finds the minimum interval needed for the entries to fit
        for (int d = 0; d < helper.dataDimensions; d++)
        {
            Entry lowerEntry = Collections.min(entries, new EntryComparator.EntryBoundComparator(entries,d,false));
            Entry upperEntry = Collections.max(entries, new EntryComparator.EntryBoundComparator(entries,d,true));
            minimumBounds.add(new Bounds(lowerEntry.getBoundingBox().getBounds().get(d).getLower(),upperEntry.getBoundingBox().getBounds().get(d).getUpper()));
        }
        return minimumBounds;
    }
    /**
     * Finds the minimum bounds needed to encompass two bounding boxes.
     * This is useful for merging bounding boxes when updating or inserting entries in the Tree.
     *
     * @param boundingBoxA The first bounding box to merge.
     * @param boundingBoxB The second bounding box to merge.
     * @return An ArrayList of Bounds objects representing the minimum bounds that enclose both bounding boxes.
     */
    
    static ArrayList<Bounds> findMinimumBounds(BoundingBox boundingBoxA, BoundingBox boundingBoxB) {
        ArrayList<Bounds> minimumBounds = new ArrayList<>();
        // For each dimension finds the minimum interval needed for the entries to fit
        for (int d = 0; d < helper.dataDimensions; d++)
        {
            double lower = Math.min(boundingBoxA.getBounds().get(d).getLower(), boundingBoxB.getBounds().get(d).getLower());
            double upper = Math.max(boundingBoxA.getBounds().get(d).getUpper(), boundingBoxB.getBounds().get(d).getUpper());
            minimumBounds.add(new Bounds(lower,upper));
        }
        return minimumBounds;
    }
}

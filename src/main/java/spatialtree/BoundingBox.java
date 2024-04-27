package main.java.spatialtree;

import java.io.Serializable;
import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 * Represents a bounding box in the n-dimensional space.
 */
public class BoundingBox implements Serializable {
    private ArrayList<Bounds> bounds; // The bounds for each dimension
    private Double area; // Area of the BoundingBox
    private Double margin; // Perimeter of the BoundingBox
    private ArrayList<Double> center; // Represents tHe coordinates of the center point of the bounding box

    static final int dataDimensions=helper.dataDimensions;

    /**
     * Constructor for the BoundingBox which initializes the bounds and calculates the area, margin, and center.
     *
     * @param bounds ArrayList of Bounds defining the bounding box in each dimension.
     */

    public BoundingBox(ArrayList<Bounds> bounds) {
        this.bounds = bounds;
        this.area = calculateArea();
        this.margin = calculateMargin();
        this.center = getCenter();
    }

    /**
     * Getter for bounds.
     *
     * @return ArrayList of Bounds defining the bounding box.
     */
    ArrayList<Bounds> getBounds() {
        return bounds;
    }
    /**
     * Getter for the area of the bounding box.
     *
     * @return double representing the area.
     */
    public double getArea() {
        // If area is not yet initialized, find the area
        if (area == null)
            area = calculateArea();
        return area;
    }

    /**
     * Getter for the margin of the bounding box.
     *
     * @return double representing the margin.
     */
    public double getMargin() {
        // If margin is not yet initialized, find the margin
        if (margin == null)
            margin = calculateMargin();

        return margin;
    }

    /**
     * Checks whether a point's radius overlaps with this bounding box.
     * DON'T DELETE!!!
     * @param point The point to check.
     * @param radius The radius around the point.
     * @return boolean indicating whether there is an overlap.
     */
    boolean checkOverLapWithPoint(ArrayList<Double> point, double radius){
        // If the minimum distance from the point is less or equal the point's radius then the bounding box is in the range
        return findMinDistanceFromPoint(point) <= radius;
    }

    /**
     * Calculates the minimum distance between this bounding box and a given point.
     * This is essential for nearest neighbor queries.
     *
     * @param point The point to calculate distance from.
     * @return double representing the minimum distance.
     */
    public double findMinDistanceFromPoint(ArrayList<Double> point){
        double minDistance = 0;
        // For every dimension find the minimum distance
        double rd;
        for (int d = 0; d < dataDimensions; d++)
        {
            if(getBounds().get(d).getLower() > point.get(d))
                rd = getBounds().get(d).getLower();
            else if (getBounds().get(d).getUpper() < point.get(d))
                rd = getBounds().get(d).getUpper();
            else
                rd = point.get(d);

            minDistance += Math.pow(point.get(d) - rd,2);
        }
        return sqrt(minDistance);
    }

    /**
     * Calculates the center of this bounding box.
     *
     * @return ArrayList of Double representing the center coordinates.
     */

    private ArrayList<Double> getCenter() {
        // If center is not yet initialized, find the center and return it
        if (center == null)
        {
            center = new ArrayList<>();

            for (int d = 0; d < dataDimensions; d++)
                center.add((bounds.get(d).getUpper()+bounds.get(d).getLower())/2);
        }
        return center;
    }
    /**
     * Calculates the margin (perimeter) of this bounding box.
     *
     * @return double representing the margin.
     */

    private double calculateMargin() {
        double sum = 0;
        for (int d = 0; d <dataDimensions; d++)
            sum += abs(bounds.get(d).getUpper() - bounds.get(d).getLower());
        return sum;
    }

    /**
     * Calculates the area (volume in multi-dimensions) of this bounding box.
     *
     * @return double representing the area.
     */

    private double calculateArea() {
        double productOfEdges = 1;
        for (int d = 0; d < dataDimensions; d++)
            productOfEdges = productOfEdges * (bounds.get(d).getUpper() - bounds.get(d).getLower());
        return abs(productOfEdges);
    }

    /**
     * Checks if two bounding boxes overlap.
     *
     * @param boundingBoxA First bounding box.
     * @param boundingBoxB Second bounding box.
     * @return boolean indicating whether the bounding boxes overlap.
     */

    public static boolean checkOverlap(BoundingBox boundingBoxA, BoundingBox boundingBoxB) {
        // For every dimension find the intersection point
        //  return false if they do not overlap
        for (int d = 0; d < dataDimensions; d++)
        {
            double overlapD = Math.min(boundingBoxA.getBounds().get(d).getUpper(), boundingBoxB.getBounds().get(d).getUpper())
                    - Math.max(boundingBoxA.getBounds().get(d).getLower(), boundingBoxB.getBounds().get(d).getLower());

            if (overlapD < 0)
                return false;
        }
        return true;
    }

    /**
     * Calculates the overlap value between two bounding boxes.
     *
     * @param boundingBoxA First bounding box.
     * @param boundingBoxB Second bounding box.
     * @return double representing the overlap value.
     */

    static double calculateOverlapValue(BoundingBox boundingBoxA, BoundingBox boundingBoxB) {
        double overlapValue = 1;
        // For every dimension find the intersection point
        for (int d = 0; d < dataDimensions; d++)
        {
            double overlapD = Math.min(boundingBoxA.getBounds().get(d).getUpper(), boundingBoxB.getBounds().get(d).getUpper())
                    - Math.max(boundingBoxA.getBounds().get(d).getLower(), boundingBoxB.getBounds().get(d).getLower());

            if (overlapD < 0)
                return 0; // No overlap, return 0
            else
                overlapValue = overlapD*overlapValue;
        }
        return overlapValue;
    }

    /**
     * Calculates the Euclidean distance between the centers of two bounding boxes.
     *
     * @param boundingBoxA First bounding box.
     * @param boundingBoxB Second bounding box.
     * @return double representing the distance.
     */
    static double findDistanceBetweenBoundingBoxes(BoundingBox boundingBoxA, BoundingBox boundingBoxB) {
        double distance = 0;
        // For every dimension find the intersection point
        for (int d = 0; d < dataDimensions; d++)
        {
            distance += Math.pow(boundingBoxA.getCenter().get(d) - boundingBoxB.getCenter().get(d),2);
        }
        return sqrt(distance);
    }
}

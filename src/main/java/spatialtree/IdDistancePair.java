package main.java.spatialtree;
/**
 * Class used to associate a record (LeafEntry) with its distance from a specific item.
 * Essential for K-NN
 */
public class IdDistancePair {
    private LeafEntry record; // The id of the record
    private double distanceFromItem; // The distance from an item

    public IdDistancePair(LeafEntry record, double distanceFromItem) {
        this.record = record;
        this.distanceFromItem = distanceFromItem;
    }

    public LeafEntry getRecord(){ return this.record;}

    public double getDistanceFromItem() {
        return distanceFromItem;
    }
}

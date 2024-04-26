package main.java.spatialtree;

// Class which is used to hold the record it's distance from a specific item
public class IdDistancePair {
    private LeafEntry record; // The id of the record
    private double distanceFromItem; // The distance from an item

    public IdDistancePair(LeafEntry record, double distanceFromItem) {
        this.record = record;
        this.distanceFromItem = distanceFromItem;
    }

    public LeafEntry getRecord(){ return this.record;}
    public long getRecordId() {
        return this.record.getRecordId();
    }


    public double getDistanceFromItem() {
        return distanceFromItem;
    }
}

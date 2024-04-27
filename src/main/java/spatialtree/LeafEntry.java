package main.java.spatialtree;

import java.util.ArrayList;

/**
 * Represents a leaf node entry in an R*-tree.
 * A LeafEntry extends the generic Entry class by including specific metadata
 * such as the record ID and the datafile block ID
 * where the actual spatial record is stored.
 */
public class LeafEntry extends Entry {
    private long recordId;
    private int dataFileBlockId; // The id of the block which the record is saved in the datafile

    public LeafEntry(long recordId, int dataFileBlockId, ArrayList<Bounds> recordBounds) {
        super(new BoundingBox(recordBounds));
        this.recordId = recordId;
        this.dataFileBlockId = dataFileBlockId;
    }

    public long getRecordId() {
        return recordId;
    }

    int getDataFileBlockId() {
        return dataFileBlockId;
    }

    /**
     * Retrieves the Record by iterating the block from the datafile using the block ID.
     * This is used to print the actual records for testing purposes
     * @return The Record if found, null otherwise.
     */
    public Record findRecord(){
        ArrayList<Record> records = helper.readDataFile(this.getDataFileBlockId());
        if (records != null && !records.isEmpty()) {

            for (Record record : records) {
                if(record.getId() == this.recordId ){
                    return record;
                }
            }
        } else {
            System.out.println("No records found in Block " + this.getDataFileBlockId() + ".");
        }
        return null;

    }

    /**
     * Searches for the Record across all blocks in the data file.
     * This method does not rely on the block ID and is used in the
     * BulkLoaded implementation, as we can't hold the
     * blockID in the BulkLoad
     *This is used to print the actual records for testing purposes
     * @return The Record if found, null otherwise.
     */
    public Record findRecordWithoutBlockId(){
        ArrayList<Record> records = new ArrayList<>();
        for (int blockId = 1; blockId <= helper.getTotalBlocksInDatafile(); blockId++) {
            records=helper.readDataFile(blockId);
            for (Record record : records) {
                if(record.getId() == this.recordId ){
                    return record;
                }
            }
        }




        if (records != null && !records.isEmpty()) {

            for (Record record : records) {
                if(record.getId() == this.recordId ){
                    return record;
                }
            }
        } else {
            System.out.println("No records found in Block " + this.getDataFileBlockId() + ".");
        }
        return null;

    }
    @Override
    public boolean isLeaf() {
        return true; // This is a leaf node
    }


    /**
     * Compares this LeafEntry to another object for equality based on record ID.
     *
     * @param obj The object to compare with this LeafEntry.
     * @return true if the other object is a LeafEntry with the same record ID.
     */
    public boolean equals(Object obj) {
        if ((obj instanceof LeafEntry && this.recordId == ((LeafEntry) obj).getRecordId())) {
            return true;
        }
        return false;
    }
}

package main.java.spatialtree;

import java.util.ArrayList;

// Represents the entries on the bottom of the RStarTree
// Extends the Entry Class where it's BoundingBox
// is the bounding box of the spatial object (the record) indexed
// also holds the recordId of the record and a pointer of the block which the record is saved in the datafile
public class LeafEntry extends Entry {
    private long recordId;
    private int dataFileBlockId; // The id of the block which the record is saved in the datafile

    LeafEntry(long recordId, int dataFileBlockId, ArrayList<Bounds> recordBounds) {
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
}

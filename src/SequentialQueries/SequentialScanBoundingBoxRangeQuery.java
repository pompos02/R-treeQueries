package SequentialQueries;

import main.java.spatialtree.Record;
import main.java.spatialtree.*;

import java.util.ArrayList;

/**
 * Implements a sequential scan range query to find records within a specified bounding box.
 * This class extends SequentialScanQuery to perform non-indexed searches on spatial data.
 */
public class SequentialScanBoundingBoxRangeQuery extends SequentialScanQuery{
    private ArrayList<LeafEntry> qualifyingRecordIds; // Record ids used for queries
    private BoundingBox searchBoundingBox; // Bounding box used for range queries

    public SequentialScanBoundingBoxRangeQuery(BoundingBox searchBoundingBox) {
        this.searchBoundingBox = searchBoundingBox;
    }
    /**
     * Executes the bounding box range query by performing a sequential scan of all records.
     *
     * @return A list of LeafEntry objects representing the records that fall within the specified bounding box.
     */
    @Override
    public ArrayList<LeafEntry> getQueryRecords() {
        qualifyingRecordIds = new ArrayList<>();
        search();
        return qualifyingRecordIds;
    }

    /**
     * Scans through all blocks of data sequentially, checking each record against the bounding box to determine if it qualifies.
     */
    private void search(){
        int blockId = 1;
        while(blockId < helper.getTotalBlocksInDatafile())
        {
            ArrayList<Record> recordsInBlock;
            recordsInBlock = helper.readDataFile(blockId);
            ArrayList<LeafEntry> entries = new ArrayList<>();

            if (recordsInBlock != null)
            {
                for (Record record : recordsInBlock)
                {
                    ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
                    // Since we have to do with points as records we set low and upper to be same
                    for (int d = 0; d < helper.getDataDimensions(); d++)
                        boundsForEachDimension.add(new Bounds(record.getCoordinate(d), record.getCoordinate(d)));

                    entries.add(new LeafEntry(record.getId(), blockId, boundsForEachDimension));
                }

                for(Entry entry : entries)
                {
                    if(BoundingBox.checkOverlap(entry.getBoundingBox(), searchBoundingBox)){
                        LeafEntry leafEntry = (LeafEntry) entry;
                        qualifyingRecordIds.add(leafEntry);
                    }
                }
            }
            else
                throw new IllegalStateException("Could not read records properly from the datafile");
            blockId++;
        }
    }
}

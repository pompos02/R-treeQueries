package SequentialQueries;


import main.java.spatialtree.*;
import main.java.spatialtree.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Performs a k-nearest neighbour query sequentially on spatial data without using an index structure.
 * This class scans all records to find the k closest points to a specified search point.
 */
public class SequentialNearestNeighboursQuery extends SequentialScanQuery {
    private ArrayList<Double> searchPoint;
    private int k;
    private PriorityQueue<IdDistancePair> nearestNeighbours;


    public SequentialNearestNeighboursQuery(ArrayList<Double> searchPoint, int k) {
        if (k < 0)
            throw new IllegalArgumentException("Parameter 'k' for the nearest neighbours must be a positive integer.");
        this.searchPoint = searchPoint;
        this.k = k;
        this.nearestNeighbours = new PriorityQueue<>(k, new Comparator<IdDistancePair>() {
            @Override
            // This comparator ensures that the PriorityQueue acts as a max-heap based on the distance from the item.
            public int compare(IdDistancePair recordDistancePairA, IdDistancePair recordDistancePairB) {
                return Double.compare(recordDistancePairB.getDistanceFromItem(),recordDistancePairA.getDistanceFromItem()); // In order to make a MAX heap
            }
        });
    }

    /**
     * Executes the query to find the k-nearest neighbours.
     *
     * @return A list of LeafEntry objects representing the k closest records to the specified point.
     */
    @Override
    public ArrayList<LeafEntry> getQueryRecords() {
        ArrayList<LeafEntry> qualifyingRecordIds = new ArrayList<>();
        findNeighbours();
        while (nearestNeighbours.size() != 0)
        {
            IdDistancePair recordDistancePair = nearestNeighbours.poll();
            qualifyingRecordIds.add(recordDistancePair.getRecord());
        }
        Collections.reverse(qualifyingRecordIds); // In order to return closest neighbours first instead of farthest
        return qualifyingRecordIds;
    }

    /**
     * Scans all records sequentially to find the nearest neighbours to the specified search point.
     */
    private void findNeighbours(){
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
                int i = 0;
                while(i < entries.size()){
                    double distanceFromPoint = entries.get(i).getBoundingBox().findMinDistanceFromPoint(searchPoint);
                    if(nearestNeighbours.size() == k){
                        if(distanceFromPoint < nearestNeighbours.peek().getDistanceFromItem()){
                            nearestNeighbours.poll();
                            nearestNeighbours.add(new IdDistancePair(entries.get(i), distanceFromPoint));
                        }
                    }else{
                        nearestNeighbours.add(new IdDistancePair(entries.get(i), distanceFromPoint));
                    }

                    i++;
                }
            }
            else
                throw new IllegalStateException("Could not read records properly from the datafile");
            blockId++;
        }
    }
}


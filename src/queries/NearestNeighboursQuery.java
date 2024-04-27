package queries;

import main.java.spatialtree.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

/**
 * Executes a k-nearest neighbours query using an R*-tree to find the k closest records to a specified point.
 */
public class NearestNeighboursQuery extends Query {
    private ArrayList<Double> searchPoint; // The coordinates of point used for radius queries
    private double searchPointRadius; // The reference radius that is used as a bound
    private int k; // The number of nearest neighbours to be found
    private PriorityQueue<IdDistancePair> nearestNeighbours; // Using a max heap for the nearest neighbours


    public NearestNeighboursQuery(ArrayList<Double> searchPoint, int k) {
        if (k < 0)
            throw new IllegalArgumentException("Parameter 'k' for the nearest neighbours must be a positive integer.");
        this.searchPoint = searchPoint;
        this.k = k;
        this.searchPointRadius = Double.MAX_VALUE;
        this.nearestNeighbours = new PriorityQueue<>(k, (recordDistancePairA, recordDistancePairB) -> {
            return Double.compare(recordDistancePairB.getDistanceFromItem(), recordDistancePairA.getDistanceFromItem()); // In order to make a MAX heap
        });
    }
    /**
     * Retrieves the nearest neighbour records found in the R*-tree starting from a given node.
     *
     * @param node The root node ( usually ) of the R*-tree from which the search starts.
     * @return A list of LeafEntry objects representing the nearest neighbours.
     */
    @Override
    public ArrayList<LeafEntry> getQueryRecords(Node node) {
        ArrayList<LeafEntry> qualifyingRecords = new ArrayList<>();
        findNeighbours(node);
        while (nearestNeighbours.size() != 0)
        {
            IdDistancePair recordDistancePair = nearestNeighbours.poll();
            qualifyingRecords.add(recordDistancePair.getRecord());
        }
        Collections.reverse(qualifyingRecords); // In order to return closest neighbours first instead of farthest
        return qualifyingRecords;
    }

    /**
     * Recursively searches the R*-tree to find and prioritize the nearest neighbours.
     * This method employs a branch-and-bound approach that limits exploration based on
     * the current search radius.
     *
     * @param node The current node in the R*-tree being explored.
     */
    private void findNeighbours(Node node) {
        node.getEntries().sort(new EntryComparator.EntryDistanceFromPointComparator(node.getEntries(),searchPoint));
        int i = 0;
        if (node.getLevel() != RStarTree.getLeafLevel()) {
            while (i < node.getEntries().size() && (nearestNeighbours.size() < k || node.getEntries().get(i).getBoundingBox().findMinDistanceFromPoint(searchPoint) <= searchPointRadius))
            {
                findNeighbours(helper.readIndexFileBlock(node.getEntries().get(i).getChildNodeBlockId()));
                i++;
            }
        }
        else {
            while (i < node.getEntries().size() && (nearestNeighbours.size() < k || node.getEntries().get(i).getBoundingBox().findMinDistanceFromPoint(searchPoint) <= searchPointRadius))
            {
                if (nearestNeighbours.size() >= k)
                    nearestNeighbours.poll();
                LeafEntry leafEntry = (LeafEntry) node.getEntries().get(i);
                double minDistance = leafEntry.getBoundingBox().findMinDistanceFromPoint(searchPoint);
                nearestNeighbours.add(new IdDistancePair(leafEntry, minDistance));
                searchPointRadius = nearestNeighbours.peek().getDistanceFromItem();
                i++;
            }

        }
    }
}

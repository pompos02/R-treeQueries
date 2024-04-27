package queries;

import main.java.spatialtree.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Implements a simple skyline query that identifies entries within a specified
 * bounding box that are not dominated by any other entry.
 */

public class SkylineQuery extends Query {
    private BoundingBox searchBoundingBox; // BoundingBox for the region to consider in the skyline

    private static final int ROOT_NODE_BLOCK_ID = 1;

    public SkylineQuery(BoundingBox searchBoundingBox) {
        this.searchBoundingBox = searchBoundingBox;
    }
    /**
     * Executes the skyline query starting from the root of the R*-tree.
     *
     * @param node The root node of the R*-tree from which the search will start.
     * @return A list of LeafEntry objects that make up the skyline within the specified bounding box.
     */
    @Override
    public ArrayList<LeafEntry> getQueryRecords(Node node) {
        ArrayList<LeafEntry> potentialSkyline = new ArrayList<>();
        collectPotentialSkyline(helper.readIndexFileBlock(ROOT_NODE_BLOCK_ID), potentialSkyline);
        return filterSkyline(potentialSkyline);
    }
    /**
     * Recursively collects potential skyline candidates from nodes that overlap with the search bounding box.
     *
     * @param node The current node being examined.
     * @param potentialSkyline A list to accumulate potential skyline entries.
     */
    private void collectPotentialSkyline(Node node, List<LeafEntry> potentialSkyline) {
        if (node.getLevel() != RStarTree.getLeafLevel()) {
            for (Entry entry : node.getEntries()) {
                if (BoundingBox.checkOverlap(entry.getBoundingBox(), this.searchBoundingBox)) {
                    collectPotentialSkyline(helper.readIndexFileBlock(entry.getChildNodeBlockId()), potentialSkyline);
                }
            }
        } else {
            for (Entry entry : node.getEntries()) {
                if (entry instanceof LeafEntry && BoundingBox.checkOverlap(entry.getBoundingBox(), this.searchBoundingBox)) {
                    potentialSkyline.add((LeafEntry) entry);
                }
            }
        }
    }
    /**
     * Filters the collected entries to determine which ones are part of the skyline.
     *
     * @param candidates The list of entries that are potential candidates for the skyline.
     * @return A list of entries that constitute the skyline, having no entries that dominate them.
     */

    private ArrayList<LeafEntry> filterSkyline(List<LeafEntry> candidates) {
        ArrayList<LeafEntry> skyline = new ArrayList<>();
        for (LeafEntry candidate : candidates) {
            boolean isDominated = false;
            for (LeafEntry other : candidates) {
                if (dominates(other, candidate)) {
                    isDominated = true;
                    break;
                }
            }
            if (!isDominated) {
                skyline.add(candidate);
            }
        }
        return skyline;
    }

    /**
     * Determines whether one entry dominates another based on their bounding boxes.
     *
     * @param a The first entry to compare.
     * @param b The second entry to compare.
     * @return true if entry 'a' dominates entry 'b', false otherwise.
     */
    private boolean dominates(LeafEntry a, LeafEntry b) {
        ArrayList<Bounds> boundsA = a.getBoundingBoxArray();
        ArrayList<Bounds> boundsB = b.getBoundingBoxArray();

        boolean betterInAll = true; // 'a' must be better or equal in all dimensions to dominate 'b'
        boolean betterInAtLeastOne = false; // 'a' must be strictly better in at least one dimension to dominate 'b'

        for (int d = 0; d < boundsA.size(); d++) {
            if (boundsA.get(d).getLower() > boundsB.get(d).getLower()) {
                betterInAll = false; // Found a dimension where 'a' is not better or equal to 'b'
            }
            if (boundsA.get(d).getLower() < boundsB.get(d).getLower()) {
                betterInAtLeastOne = true; // Found a dimension where 'a' is strictly better than 'b'
            }
        }

        return betterInAll && betterInAtLeastOne; // 'a' dominates 'b' if it is better in at least one dimension and not worse in any
    }
}

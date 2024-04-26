package queries;

import main.java.spatialtree.LeafEntry;
import main.java.spatialtree.Node;
import main.java.spatialtree.*;
import java.util.ArrayList;
import java.util.List;


// A very Simple SkyLineQuery just to prove the logic

public class SkylineQuery extends Query {
    private BoundingBox searchBoundingBox; // BoundingBox for the region to consider in the skyline

    private static final int ROOT_NODE_BLOCK_ID = 1;

    public SkylineQuery(BoundingBox searchBoundingBox) {
        this.searchBoundingBox = searchBoundingBox;
    }
    @Override
    public ArrayList<LeafEntry> getQueryRecords(Node node) {
        ArrayList<LeafEntry> potentialSkyline = new ArrayList<>();
        collectPotentialSkyline(helper.readIndexFileBlock(ROOT_NODE_BLOCK_ID), potentialSkyline);
        return filterSkyline(potentialSkyline);
    }

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

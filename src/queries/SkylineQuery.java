package queries;

import main.java.spatialtree.LeafEntry;
import main.java.spatialtree.Node;
import main.java.spatialtree.*;
import java.util.ArrayList;
import java.util.List;

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
        // Check if 'a' dominates 'b'. For simplicity, assuming two dimensions: cost and distance
        boolean betterInAll = a.getBoundingBoxArray().get(0).getLower() <= b.getBoundingBoxArray().get(0).getLower() &&
                a.getBoundingBoxArray().get(1).getLower() <= b.getBoundingBoxArray().get(1).getLower();
        boolean betterInAtLeastOne = a.getBoundingBoxArray().get(0).getLower() < b.getBoundingBoxArray().get(0).getLower() ||
                a.getBoundingBoxArray().get(1).getLower() < b.getBoundingBoxArray().get(1).getLower();
        return betterInAll && betterInAtLeastOne;
    }
}

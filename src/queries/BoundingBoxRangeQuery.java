package queries;

import main.java.spatialtree.*;

import java.util.ArrayList;

/**
 * Implements a range query using a bounding box on an R*-tree to find all records within the specified area.
 * This class is a type of Query that specifically handles searching within spatial bounds.
 */
public class BoundingBoxRangeQuery extends Query {
    private ArrayList<LeafEntry> qualifyingRecord; // Record ids used for queries
    private BoundingBox searchBoundingBox; // BoundingBox used for range queries

    public BoundingBoxRangeQuery(BoundingBox searchBoundingBox) {
        this.searchBoundingBox = searchBoundingBox;
    }
    /**
     * Executes the query on a given node of the R*-tree and returns all records (leaf entries) within the bounding box.
     *
     * @param node The starting node for the query, typically the root of the R*-tree.
     * @return A list of LeafEntry objects representing the records found within the bounding box.
     */
    @Override
    public ArrayList<LeafEntry> getQueryRecords(Node node){
        qualifyingRecord = new ArrayList<>();
        search(node);
        return qualifyingRecord;
    }
    /**
     * Recursively searches through the R*-tree nodes to find and collect all records that overlap with the bounding box.
     * This method differentiates between leaf and non-leaf nodes to apply the correct checking logic.
     *
     * @param node The node currently being searched.
     */
    private void search(Node node){
        // [Search subtrees]
        // If T is not a leaf check each entry E to determine whether E.R
        //overlaps searchBoundingBox.
        if (node.getLevel() != RStarTree.getLeafLevel())
            for (Entry entry: node.getEntries())
            {
                // For all overlapping entries, invoke Search on the tree whose root is
                // pointed to by E.childPTR.
                if (BoundingBox.checkOverlap(entry.getBoundingBox(),searchBoundingBox))
                    search( helper.readIndexFileBlock(entry.getChildNodeBlockId()));
            }

            // [Search leaf node]
            // If T is a leaf, check all entries E to determine whether E.r overlaps S.
            // If so, E is a qualifying record
        else
            for (Entry entry: node.getEntries())
            {
                // For all overlapping entries, invoke Search on the tree whose root is
                // pointed to by E.childPTR.
                if (BoundingBox.checkOverlap(entry.getBoundingBox(),searchBoundingBox))
                {
                    LeafEntry leafEntry = (LeafEntry) entry;
                    qualifyingRecord.add(leafEntry);
                }
            }
    }
}

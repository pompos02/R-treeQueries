package main.java.spatialtree;

import queries.BoundingBoxRangeQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static main.java.spatialtree.helper.RecordSorterX;

public class BulkLoadingRStarTree {

    private int totalLevels; // The total levels of the tree, increasing the size starting of the root, the root (top level) will always have the highest level
    private boolean[] levelsInserted; // Used for information on  which levels have already called overFlowTreatment on a data insertion procedure
    private static final int ROOT_NODE_BLOCK_ID = 1; // Root node will always have 1 as it's ID, in order to identify which block has the root Node
    private static final int LEAF_LEVEL = 1; // Constant leaf level 1, since we are increasing the level from the root, the root (top level) will always have the highest level
    private static final int CHOOSE_SUBTREE_P_ENTRIES = 32;
    private static final int REINSERT_P_ENTRIES = (int) (0.30 * Node.getMaxEntries()); // Setting p to 30% of max entries

    // RStarTree constructor
    // If insertRecordsFromDataFile parameter is true then makes a new root node since we are resetting the tree and inserting the records from the datafile
    public BulkLoadingRStarTree(boolean insertRecordsFromDataFile) {
        this.totalLevels = helper.getTotalLevelsOfTreeIndex(); // Initialise the total levels from the FileHelper class, in case there is an already existing indexFile
        ArrayList<Record> allRecords = new ArrayList<>();
        if (insertRecordsFromDataFile)
        {
            for (int i = 1; i< helper.getTotalBlocksInDatafile(); i++)
            {
                ArrayList<Record> records = helper.readDataFile(i);
                allRecords.addAll(records);

            }
            RecordSorterX(allRecords);

            List<Node> nodes= createLeafNodes(allRecords,-1);
            for(Node node : nodes){
                helper.updateIndexFileBlock(node,LEAF_LEVEL);
            }
            Node root = bulkLoadTree(nodes);  // Build the entire tree
            helper.updateIndexFileBlock(root, ++totalLevels);  // Store the root node

        }
    }
    private List<Node> createLeafNodes(ArrayList<Record> records, int dataFileBlockId) {// the dataFileBlockId is not going to be used
        List<Node> nodes = new ArrayList<>();
        ArrayList<Entry> entries = new ArrayList<>();

        for (Record record : records) {
            // Create bounds for each dimension of the record
            // Assuming record.getCoordinate(index) returns the coordinate for a given dimension
            ArrayList<Bounds> recordBounds = new ArrayList<>();
            for (int d = 0; d < helper.getDataDimensions(); d++) {
                double coordinate = record.getCoordinate(d);
                recordBounds.add(new Bounds(coordinate, coordinate));
            }

            if (entries.size() == Node.getMaxEntries()) {
                nodes.add(new Node(LEAF_LEVEL, new ArrayList<>(entries)));
                entries.clear();
            }
            entries.add(new LeafEntry(record.getId(), dataFileBlockId, recordBounds));
        }

        if (!entries.isEmpty()) {
            nodes.add(new Node(LEAF_LEVEL, entries));
        }

        return nodes;
    }
    private Node bulkLoadTree(List<Node> leafNodes) {
        int currentLevel = LEAF_LEVEL;
        List<Node> currentLevelNodes = new ArrayList<>(leafNodes);

        while (currentLevelNodes.size() > 1) {  // Continue until only one node remains, the root
            List<Node> nextLevelNodes = constructHigherLevelNodes(currentLevelNodes, currentLevel);
            for (Node node : nextLevelNodes) {
                helper.updateIndexFileBlock(node, currentLevel + 1);  // Update the index file with the new node
            }
            currentLevel++;
            currentLevelNodes = nextLevelNodes;
        }

        return currentLevelNodes.get(0);  // The single remaining node is the root
    }

    private List<Node> constructHigherLevelNodes(List<Node> lowerLevelNodes, int currentLevel) {
        List<Node> higherLevelNodes = new ArrayList<>();
        ArrayList<Entry> entries = new ArrayList<>();

        for (Node node : lowerLevelNodes) {
            ArrayList<Bounds> nodeBounds = node.calculateBoundingBoxForNode(node.getEntries());
            BoundingBox bb = new BoundingBox(nodeBounds);
            Entry entry = new Entry(bb , node.getBlockId());  // Create a new Entry for the node
            if (entries.size() == Node.getMaxEntries()) {
                higherLevelNodes.add(new Node(currentLevel + 1, new ArrayList<>(entries)));
                entries.clear();
            }
            entries.add(entry);
        }

        if (!entries.isEmpty()) {
            higherLevelNodes.add(new Node(currentLevel + 1, entries));
        }

        return higherLevelNodes;
    }



    public Node getRoot() {
        return helper.readIndexFileBlock(ROOT_NODE_BLOCK_ID);
    }

    static int getRootNodeBlockId() {
        return ROOT_NODE_BLOCK_ID;
    }

    public static int getLeafLevel() {
        return LEAF_LEVEL;
    }






    public ArrayList<LeafEntry> getDataInBoundingBox(BoundingBox searchBoundingBox){
        BoundingBoxRangeQuery query = new BoundingBoxRangeQuery(searchBoundingBox);
        return query.getQueryRecordIds(helper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }


    public int getTotalLevels() {
        return totalLevels;
    }
}

package main.java.spatialtree;

import queries.BoundingBoxRangeQuery;
import queries.NearestNeighboursQuery;
import queries.SkylineQuery;

import java.util.ArrayList;
import java.util.List;

import static main.java.spatialtree.helper.RecordSorterX;

public class BulkLoadingRStarTree {

    private int totalLevels; // The total levels of the tree, increasing the size starting of the root, the root (top level) will always have the highest level
    private boolean[] levelsInserted; // Used for information on  which levels have already called overFlowTreatment on a data insertion procedure
    private static  int ROOT_NODE_BLOCK_ID ; // Root node will always have 1 as it's ID, in order to identify which block has the root Node
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
            Node root = bulkLoadTree(nodes);  // Build the entire tree
            root.setBlockId(helper.getTotalBlocksInIndexFile());
            helper.writeNewIndexFileBlock(root);
            ROOT_NODE_BLOCK_ID=helper.getTotalBlocksInIndexFile()-1; // -1 because we incremented it in writeNewIndexFileBlock

        }
    }
    private List<Node> createLeafNodes(ArrayList<Record> records, int dataFileBlockId) {
        List<Node> nodes = new ArrayList<>();
        ArrayList<Entry> entries = new ArrayList<>();
        for (Record record : records) {
            ArrayList<Bounds> recordBounds = new ArrayList<>();
            for (int d = 0; d < helper.getDataDimensions(); d++) {
                double coordinate = record.getCoordinate(d);
                recordBounds.add(new Bounds(coordinate, coordinate));
            }

            entries.add(new LeafEntry(record.getId(), dataFileBlockId, recordBounds));
            if (entries.size() == Node.getMaxEntries()) {
                Node node = new Node(LEAF_LEVEL, new ArrayList<>(entries));
                node.setBlockId(helper.getTotalBlocksInIndexFile());
                helper.writeNewIndexFileBlock(node);
                nodes.add(node);
                entries.clear();  // Clearing the list to free up memory
            }
        }

        if (!entries.isEmpty()) {
            Node node = new Node(LEAF_LEVEL, entries);
            node.setBlockId(helper.getTotalBlocksInIndexFile());
            helper.writeNewIndexFileBlock(node);
            nodes.add(node);
        }
        return nodes;
    }

    private Node bulkLoadTree(List<Node> leafNodes) {
        int currentLevel = LEAF_LEVEL;
        List<Node> currentLevelNodes = new ArrayList<>(leafNodes);
        while (currentLevelNodes.size() > 1) {  // Continue until only one node remains, the root
            List<Node> nextLevelNodes = constructHigherLevelNodes(currentLevelNodes, currentLevel);
            for (Node node : nextLevelNodes) {
                node.setBlockId(helper.getTotalBlocksInIndexFile());
                helper.writeNewIndexFileBlock(node);
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
                helper.updateLevelsOfTreeInIndexFile();
                Node newNode = new Node(helper.getTotalLevelsOfTreeIndex(), new ArrayList<>(entries));

                newNode.setBlockId(helper.getTotalBlocksInIndexFile());  // Get a new block ID for the node

                helper.writeNewIndexFileBlock(newNode);
                higherLevelNodes.add(newNode);
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
        return query.getQueryRecords(helper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }


    public int getTotalLevels() {
        return totalLevels;
    }

    public boolean deleteRecord(Entry targetEntry) {
        Node node = findLeafNode(targetEntry, getRoot());
        if (node == null){
            return false; // Entry not found
        }

        // Attempt to remove the entry
        boolean removed = node.getEntries().removeIf(e -> e instanceof LeafEntry && e.equals(targetEntry));

        if (!removed) return false; // Entry was not in the found node

        //TODO
        // Handle potential underflow
        if (node.getEntries().size() < Node.getMaxEntries() * 0.3) {
            System.out.println("Underflow");
            //handleUnderflow(node);
        }

        // Node is updated, reflect changes in storage if necessary
        helper.updateIndexFileBlock(node, totalLevels);
        return true; // Entry successfully deleted
    }

    private Node findLeafNode(Entry targetEntry, Node node) {
        if (node.getLevel() == LEAF_LEVEL) {
            return node.contains(targetEntry) ? node : null;
        } else {
            for (Entry e : node.getEntries()) {
                if (BoundingBox.checkOverlap(e.getBoundingBox(), targetEntry.getBoundingBox())) {
                    Node result = findLeafNode(targetEntry, helper.readIndexFileBlock(e.getChildNodeBlockId()));
                    if (result != null) return result;
                }
            }
        }
        return null;
    }

    public ArrayList<LeafEntry> getNearestNeighbours(ArrayList<Double> searchPoint, int k){
        NearestNeighboursQuery query = new NearestNeighboursQuery(searchPoint,k);
        return query.getQueryRecords(helper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }

    public ArrayList<LeafEntry> getSkyline(BoundingBox searchBoundingBox) {
        SkylineQuery query = new SkylineQuery(searchBoundingBox);
        return query.getQueryRecords(helper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }
}

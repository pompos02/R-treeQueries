package main.java.spatialtree;

import queries.BoundingBoxRangeQuery;
import queries.NearestNeighboursQuery;
import queries.SkylineQuery;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents an R*-tree for spatial data indexing.
 */
public class RStarTree {

    private static int totalLevels; // The total levels of the tree, increasing the size starting of the root, the root (top level) will always have the highest level
    private boolean[] levelsInserted; // Used for information on  which levels have already called overFlowTreatment on a data insertion procedure
    private static final int ROOT_NODE_BLOCK_ID = 1; // Root node will always have 1 as it's ID, in order to identify which block has the root Node
    private static final int LEAF_LEVEL = 1; // Constant leaf level 1, since we are increasing the level from the root, the root (top level) will always have the highest level
    private static final int CHOOSE_SUBTREE_P_ENTRIES = 35;
    private static final int REINSERT_P_ENTRIES = (int) (0.30 * Node.getMaxEntries()); // Setting p to 30% of max entries
    /**
     * Constructs an R*-tree, optionally initializing it by inserting records from a data file.
     * @param insertRecordsFromDataFile If true, initializes the tree with records from a data file.
     */
    public RStarTree(boolean insertRecordsFromDataFile) {
        this.totalLevels = helper.getTotalLevelsOfTreeIndex(); // Initialise the total levels from the FileHelper class, in case there is an already existing indexFile
        int counter=0;
        if (insertRecordsFromDataFile)
        {
            helper.writeNewIndexFileBlock(new Node(1)); // Initialising the root node
            // Adding the data of datafile in the RStarTree (to the indexFile)
            for (int i = 1; i< helper.getTotalBlocksInDatafile(); i++)
            {
                ArrayList<Record> records = helper.readDataFile(i);
                counter+=records.size();
                if (records != null)
                {
                    for (Record record : records) {
                        insertRecord(record, i);
                    }
                }
                else
                    throw new IllegalStateException("Could not read records properly from the datafile");
            }System.out.println("SIZE OF RECORDS: " + counter);
        }

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


    /**
     * Inserts a record into the tree at the leaf level.
     *
     * @param record The record to insert.
     * @param datafileBlockId The block ID of the data file where the record is stored.
     */
    private void insertRecord(Record record, int datafileBlockId) {
        ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
        // Since we have to do with points as records we set low and upper to be same
        for (int d = 0; d < helper.getDataDimensions(); d++)
            boundsForEachDimension.add(new Bounds(record.getCoordinate(d),record.getCoordinate(d)));

        levelsInserted = new boolean[totalLevels];
        insert(null, null, new LeafEntry(record.getId(), datafileBlockId, boundsForEachDimension), LEAF_LEVEL); // Inserting on leaf level since it's a new record
    }

    /**
     * Retrieves all leaf entries that fall within a specified bounding box.
     * Bounding box range query
     *
     * @param searchBoundingBox The bounding box to query against.
     * @return A list of leaf entries within the bounding box.
     */
    public ArrayList<LeafEntry> getDataInBoundingBox(BoundingBox searchBoundingBox){
        BoundingBoxRangeQuery query = new BoundingBoxRangeQuery(searchBoundingBox);
        return query.getQueryRecords(helper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }
    /**
     * Retrieves all leaf entries that form the skyline within a specified bounding box.
     * Skyline query
     *
     * @param searchBoundingBox The bounding box to query against.
     * @return A list of leaf entries that are part of the skyline.
     */
    public ArrayList<LeafEntry> getSkyline(BoundingBox searchBoundingBox) {
        SkylineQuery query = new SkylineQuery(searchBoundingBox);
        return query.getQueryRecords(helper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }
    /**
     * Retrieves the nearest neighbors to a specified point.
     * K-NN query
     *
     * @param searchPoint The point to find neighbors near.
     * @param k The number of nearest neighbors to retrieve.
     * @return A list of leaf entries representing the nearest neighbors.
     */
    public ArrayList<LeafEntry> getNearestNeighbours(ArrayList<Double> searchPoint, int k){
        NearestNeighboursQuery query = new NearestNeighboursQuery(searchPoint,k);
        return query.getQueryRecords(helper.readIndexFileBlock(ROOT_NODE_BLOCK_ID));
    }
    /**
     * Recursively inserts a data entry into the tree, possibly causing a split of nodes.
     *
     * @param parentNode The parent node of the current recursion level.
     * @param parentEntry The entry in the parent node that points to the current node.
     * @param dataEntry The data entry to insert.
     * @param levelToAdd The tree level at which to add the data entry.
     * @return The entry resulting from a split, if any, or null if no split occurred.
     */
    private Entry insert(Node parentNode, Entry parentEntry,  Entry dataEntry, int levelToAdd) {

        Node childNode;
        long idToRead;

        if(parentEntry == null)
            idToRead = ROOT_NODE_BLOCK_ID;

        else
        {
            // Updating-Adjusting the bounding box of the Entry that points to the Updated Node
            parentEntry.adjustBBToFitEntry(dataEntry);
            helper.updateIndexFileBlock(parentNode,totalLevels);
            idToRead = parentEntry.getChildNodeBlockId();
        }

        childNode = helper.readIndexFileBlock(idToRead);
        if (childNode == null)
            throw new IllegalStateException("The Node-block read from file is null");

        // CS2: If we're at a leaf (or the level we wanted to insert the dataEntry), then use that level
        // I2: If N has less than M items, accommodate E in N
        if (childNode.getLevel() == levelToAdd)
        {
            childNode.insertEntry(dataEntry);
            helper.updateIndexFileBlock(childNode,totalLevels);
        }

        else {
            // I1: Invoke ChooseSubtree. with the level as a parameter,
            // to find an appropriate node N, m which to place the
            // new leaf E

            // Recurse to get the node that the new data entry will fit better
            Entry bestEntry = chooseSubTree(childNode, dataEntry.getBoundingBox(), levelToAdd);
            // Receiving a new Entry if the recursion caused the next level's Node to split
            Entry newEntry = insert(childNode, bestEntry, dataEntry, levelToAdd);

            childNode = helper.readIndexFileBlock(idToRead);
            if (childNode == null)
                throw new IllegalStateException("The Node-block read from file is null");

            // If split was called on children, the new entry that the split caused gets joined to the list of items at this level
            if (newEntry != null)
            {
                childNode.insertEntry(newEntry);
                helper.updateIndexFileBlock(childNode,totalLevels);
            }

            // Else no split was called on children, returning null upwards
            else
            {
                helper.updateIndexFileBlock(childNode,totalLevels);
                return null;
            }
        }

        // If N has M+1 items. invoke OverflowTreatment with the
        // level of N as a parameter [for reinsertion or split]
        if (childNode.getEntries().size() > Node.getMaxEntries())
        {
            // I3: If OverflowTreatment was called and a split was
            // performed, propagate OverflowTreatment upwards
            // if necessary
            return overFlowTreatment(parentNode,parentEntry,childNode);
        }

        return null;
    }

    /**
     * Selects the best subtree to accommodate a new entry based on the minimal enlargement and overlap criteria.
     *
     * @param node The current node being considered.
     * @param boundingBoxToAdd The bounding box of the entry to insert.
     * @param levelToAdd The level at which to add the entry.
     * @return The best entry within the node to follow the path for insertion.
     */
    private Entry chooseSubTree(Node node, BoundingBox boundingBoxToAdd, int levelToAdd) {

        Entry bestEntry;

        // If the child pointers in N point to leaves
        if (node.getLevel() == levelToAdd+1)
        {
            // Alternative for large node sizes, determine the nearly minimum overlap cost
            if (Node.getMaxEntries() > (CHOOSE_SUBTREE_P_ENTRIES *2)/3  && node.getEntries().size() > CHOOSE_SUBTREE_P_ENTRIES)
            {
                // Sorting the entries in the node in increasing order of
                // their area enlargement needed to include the new data rectangle
                ArrayList<EntryAreaEnlargementPair> entryAreaEnlargementPairs = new ArrayList<>();
                for (Entry entry: node.getEntries())
                {
                    BoundingBox newBoundingBoxA = new BoundingBox(Bounds.findMinimumBounds(entry.getBoundingBox(),boundingBoxToAdd));
                    double areaEnlargementA = newBoundingBoxA.getArea() - entry.getBoundingBox().getArea();
                    entryAreaEnlargementPairs.add(new EntryAreaEnlargementPair(entry,areaEnlargementA));
                }
                entryAreaEnlargementPairs.sort(EntryAreaEnlargementPair::compareTo);
                // Let sortedByEnlargementEntries be the group of the sorted entries
                ArrayList<Entry> sortedByEnlargementEntries = new ArrayList<>();
                for (EntryAreaEnlargementPair pair: entryAreaEnlargementPairs)
                    sortedByEnlargementEntries.add(pair.getEntry());

                // From the items in sortedByEnlargementEntries, let A be the group of the first p entries,
                // considering all items in the node, choosing the entry whose rectangle needs least overlap enlargement
                //ArrayList<Entry> pFirstEntries = (ArrayList<Entry>)sortedByEnlargementEntries.subList(0, CHOOSE_SUBTREE_P_ENTRIES);
                bestEntry = Collections.min(sortedByEnlargementEntries.subList(0, CHOOSE_SUBTREE_P_ENTRIES), new EntryComparator.EntryOverlapEnlargementComparator(sortedByEnlargementEntries.subList(0, CHOOSE_SUBTREE_P_ENTRIES),boundingBoxToAdd,node.getEntries()));

                return bestEntry;
            }

            // Choose the entry in the node whose rectangle needs least overlap enlargement to include the new data rectangle
            // Resolve ties by choosing the entry whose rectangle needs least area enlargement,
            // then the entry with the rectangle of smallest area
            bestEntry = Collections.min(node.getEntries(), new EntryComparator.EntryOverlapEnlargementComparator(node.getEntries(),boundingBoxToAdd,node.getEntries()));
            return bestEntry;
        }

        // If the child pointers in N do not point to leaves: determine the minimum area cost],
        // choose the leaf in N whose rectangle needs least area enlargement to include the new data
        // rectangle. Resolve ties by choosing the leaf with the rectangle of smallest area
        ArrayList<EntryAreaEnlargementPair> entryAreaEnlargementPairs = new ArrayList<>();
        for (Entry entry: node.getEntries())
        {
            BoundingBox newBoundingBoxA = new BoundingBox(Bounds.findMinimumBounds(entry.getBoundingBox(),boundingBoxToAdd));
            double areaEnlargementA = newBoundingBoxA.getArea() - entry.getBoundingBox().getArea();
            entryAreaEnlargementPairs.add(new EntryAreaEnlargementPair(entry,areaEnlargementA));
        }

        bestEntry = Collections.min(entryAreaEnlargementPairs,EntryAreaEnlargementPair::compareTo).getEntry();
        return bestEntry;
    }

    /**
     * Handles node overflows by either reinserting entries or splitting the node.
     *
     * @param parentNode The parent node of the overflowing node.
     * @param parentEntry The entry pointing to the overflowing node.
     * @param childNode The node that is overflowing.
     * @return An entry that results from handling the overflow, which might require propagation upwards.
     */
    private Entry overFlowTreatment(Node parentNode, Entry parentEntry, Node childNode) {
        //System.out.println("OVERFLOW TREATMENT CALLED");
        // If the level is not the root level and this is the first call of OverflowTreatment
        // in the given level during the insertion of one data rectangle, then reinsert
        if (childNode.getBlockId() != ROOT_NODE_BLOCK_ID && !levelsInserted[childNode.getLevel()-1])
        {
            levelsInserted[childNode.getLevel()-1] = true; // Mark level as already inserted
            reInsert(parentNode,parentEntry,childNode);
            return null;
        }


        // Else invoke Split
        ArrayList<Node> splitNodes = childNode.splitNode(); // The two nodes occurring after the split
        if (splitNodes.size() != 2)
            throw new IllegalStateException("The resulting Nodes after a split cannot be more or less than two");
        childNode.setEntries(splitNodes.get(0).getEntries()); // Adjusting the previous Node with the new entries
        Node splitNode = splitNodes.get(1); // The new Node that occurred from the split

        // Updating the file with the new changes of the split nodes
        if (childNode.getBlockId() != ROOT_NODE_BLOCK_ID)
        {
            helper.updateIndexFileBlock(childNode,totalLevels);
            splitNode.setBlockId(helper.getTotalBlocksInIndexFile());
            helper.writeNewIndexFileBlock(splitNode);

            // Propagate the overflow treatment upwards, to fit the entry on the caller's level Node
            parentEntry.adjustBBToFitEntries(childNode.getEntries()); // Adjusting the bounding box of the Entry that points to the updated Node
            helper.updateIndexFileBlock(parentNode,totalLevels); // Write changes to file
            return new Entry(splitNode);
        }

        // Else if OverflowTreatment caused a split of the root, create a new root

        // Creating two Node-blocks for the split
        childNode.setBlockId(helper.getTotalBlocksInIndexFile());
        helper.writeNewIndexFileBlock(childNode);
        splitNode.setBlockId(helper.getTotalBlocksInIndexFile());
        helper.writeNewIndexFileBlock(splitNode);

        // Updating the root Node-block with the new root Node
        ArrayList<Entry> newRootEntries = new ArrayList<>();
        newRootEntries.add(new Entry(childNode));
        newRootEntries.add(new Entry(splitNode));
        Node newRoot = new Node(++totalLevels,newRootEntries);
        newRoot.setBlockId(ROOT_NODE_BLOCK_ID);
        helper.updateIndexFileBlock(newRoot,totalLevels);
        return null;
    }

    /**
     * Reinserts entries from a node to handle overflows.
     *
     * @param parentNode The parent node.
     * @param parentEntry The entry in the parent node that points to the current node.
     * @param childNode The node from which entries are reinserted.
     */
    private void reInsert(Node parentNode, Entry parentEntry, Node childNode) {

        if(childNode.getEntries().size() != Node.getMaxEntries() + 1)
            throw new IllegalStateException("Cannot throw reinsert for node with total entries fewer than M+1");

        // RI1 For all M+l items of a node N, compute the distance between the centers of their rectangles
        // and the center of the bounding rectangle of N

        // RI2: Sort the items in INCREASING order (since then we use close reinsert)
        // of their distances computed in RI1
        childNode.getEntries().sort(new EntryComparator.EntryDistanceFromCenterComparator(childNode.getEntries(),parentEntry.getBoundingBox()));
        ArrayList<Entry> removedEntries = new ArrayList<>(childNode.getEntries().subList(childNode.getEntries().size()-REINSERT_P_ENTRIES,childNode.getEntries().size()));

        // RI3: Remove the last p items from N (since then we use close reinsert) and adjust the bounding rectangle of N
        for(int i = 0; i < REINSERT_P_ENTRIES; i++)
            childNode.getEntries().remove(childNode.getEntries().size()-1);

        // Updating bounding box of node and to the parent entry
        parentEntry.adjustBBToFitEntries(childNode.getEntries());
        helper.updateIndexFileBlock(parentNode,totalLevels);
        helper.updateIndexFileBlock(childNode,totalLevels);

        // RI4: In the sort, defined in RI2, starting with the minimum distance (= close reinsert),
        // invoke Insert to reinsert the items
        if(removedEntries.size() != REINSERT_P_ENTRIES)
            throw new IllegalStateException("Entries queued for reinsert have different size than the ones that were removed");

        for (Entry entry : removedEntries)
            insert(null,null,entry,childNode.getLevel());
    }

    /**
     * Attempts to delete a record from the tree.
     *
     * @param targetEntry The entry to delete.
     * @return true if the entry was successfully deleted, false otherwise.
     */
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

    /**
     * Finds the leaf node that contains the specified entry.
     *
     * @param targetEntry The entry to find.
     * @param node The node to start the search from.
     * @return The leaf node containing the entry, or null if not found.
     */
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





}

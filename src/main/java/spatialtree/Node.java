package main.java.spatialtree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
/**
 * Represents a node in an R*-tree, which may contain either further nodes or leaf entries.
 */
    public class Node implements Serializable {
        private static final int MAX_ENTRIES = helper.calculateMaxEntriesInNode(); // The maximum entries that a Node can fit based on the file parameters
        private static final int MIN_ENTRIES = (int)(0.4 * MAX_ENTRIES); // Setting m to 40%
        private int level; // The level of the tree that this Node is located
        private long blockId; // The unique ID of the file block that this Node refers to
        private ArrayList<Entry> entries; // The ArrayList with the Entries of the Node
    /**
     * Constructor for root node initialization.
     *
     * @param level The level at which the node exists within the tree.
     */
    Node(int level) {
        this.level = level;
        this.entries = new ArrayList<>();
        this.blockId = RStarTree.getRootNodeBlockId();
    }
    /**
     * General constructor for node with predefined entries.
     *
     * @param level The tree level of the node.
     * @param entries List of entries for the node.
     */
    Node(int level, ArrayList<Entry> entries) {
        this.level = level;
        this.entries = entries;
    }

    void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    void setEntries(ArrayList<Entry> entries) {
        this.entries = entries;
    }

    static int getMaxEntries() {
        return MAX_ENTRIES;
    }

    long getBlockId() {
        return blockId;
    }

    public int getLevel() {
        return level;
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }
    /**
     * Adds an entry to the node.
     *
     * @param entry The entry to be added.
     */
    void insertEntry(Entry entry)
    {
        entries.add(entry);
    }

    /**
     * Splits the node into two when. This method determines the split strategy
     *
     * @return A list containing two new nodes as a result of the split.
     */
    ArrayList<Node> splitNode() {
        ArrayList<Distribution> splitAxisDistributions = chooseSplitAxis();
        return chooseSplitIndex(splitAxisDistributions);
    }

    public ArrayList<Bounds> calculateBoundingBoxForNode(ArrayList<Entry> entries) {
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("Entries list cannot be empty");
        }

        // Get the dimensionality from the first entry
        int dimensions = helper.getDataDimensions();
        ArrayList<Bounds> combinedBounds = new ArrayList<>(Collections.nCopies(dimensions, null));

        // Initialize combinedBounds with extreme values to be reduced in the loops
        for (int i = 0; i < dimensions; i++) {
            combinedBounds.set(i, new Bounds(-Double.MAX_VALUE, Double.MAX_VALUE));
        }

        // Calculate minimum and maximum for each dimension across all entries
        for (Entry entry : entries) {

            ArrayList<Bounds> entryBounds = entry.getBoundingBoxArray();
            for (int d = 0; d < dimensions; d++) {
                Bounds currentBounds = entryBounds.get(d);
                Bounds existingBounds = combinedBounds.get(d);
                double newLower = Math.min(existingBounds.getLower(), currentBounds.getLower());
                double newUpper = Math.max(existingBounds.getUpper(), currentBounds.getUpper());
                combinedBounds.set(d, new Bounds(newLower, newUpper));
            }
        }

        return combinedBounds;
    }


    /**
     * Helper method to choose the best split axis by evaluating the distribution of entries.
     *
     * @return A list of potential distributions along the chosen split axis.
     */
    private ArrayList<Distribution> chooseSplitAxis() {

        ArrayList<Distribution> splitAxisDistributions = new ArrayList<>(); // for the different distributions
        double splitAxisMarginsSum = Double.MAX_VALUE;
        for (int d = 0; d < helper.dataDimensions; d++)
        {
            ArrayList<Entry> entriesSortedByUpper = new ArrayList<>();
            ArrayList<Entry> entriesSortedByLower = new ArrayList<>();

            for (Entry entry : entries)
            {
                entriesSortedByLower.add(entry);
                entriesSortedByUpper.add(entry);
            }

            entriesSortedByLower.sort(new EntryComparator.EntryBoundComparator(entriesSortedByLower,d,false));
            entriesSortedByUpper.sort(new EntryComparator.EntryBoundComparator(entriesSortedByUpper,d,true));

            ArrayList<ArrayList<Entry>> sortedEntries = new ArrayList<>();
            sortedEntries.add(entriesSortedByLower);
            sortedEntries.add(entriesSortedByUpper);

            double sumOfMargins = 0;
            ArrayList<Distribution>  distributions = new ArrayList<>();
            // Determining distributions
            for (ArrayList<Entry> sortedEntryList: sortedEntries)
            {
                for (int k = 1; k <= MAX_ENTRIES - 2* MIN_ENTRIES +2; k++)
                {
                    ArrayList<Entry> firstGroup = new ArrayList<>();
                    ArrayList<Entry> secondGroup = new ArrayList<>();
                    // The first group contains the first (m-l)+k entries, the second group contains the remaining entries
                    for (int j = 0; j < (MIN_ENTRIES -1)+k; j++)
                        firstGroup.add(sortedEntryList.get(j));
                    for (int j = (MIN_ENTRIES -1)+k; j < entries.size(); j++)
                        secondGroup.add(sortedEntryList.get(j));

                    BoundingBox bbFirstGroup = new BoundingBox(Bounds.findMinimumBounds(firstGroup));
                    BoundingBox bbSecondGroup = new BoundingBox(Bounds.findMinimumBounds(secondGroup));

                    Distribution distribution = new Distribution(new DistributionGroup(firstGroup,bbFirstGroup), new DistributionGroup(secondGroup,bbSecondGroup));
                    distributions.add(distribution);
                    sumOfMargins += bbFirstGroup.getMargin() + bbSecondGroup.getMargin();
                }

                // Choose the axis with the minimum sum as split axis
                if (splitAxisMarginsSum > sumOfMargins)
                {
                    // bestSplitAxis = d;
                    splitAxisMarginsSum = sumOfMargins;
                    splitAxisDistributions = distributions;
                }
            }
        }
        return splitAxisDistributions;
    }

    /**
     * Choose the best split index from the available distributions and return the resulting nodes.
     *
     * @param splitAxisDistributions The list of potential distributions to consider for splitting.
     * @return A list of two new nodes resulting from the split.
     */
    private ArrayList<Node> chooseSplitIndex(ArrayList<Distribution> splitAxisDistributions) {

        if (splitAxisDistributions.size() == 0)
            throw new IllegalArgumentException("Wrong distributions group size. Given 0");

        double minOverlapValue = Double.MAX_VALUE;
        double minAreaValue = Double.MAX_VALUE;
        int bestDistributionIndex = 0;
        // Along the chosen split axis, choose the
        // distribution with the minimum overlap value
        for (int i = 0; i < splitAxisDistributions.size(); i++)
        {
            DistributionGroup distributionFirstGroup = splitAxisDistributions.get(i).getFirstGroup();
            DistributionGroup distributionSecondGroup = splitAxisDistributions.get(i).getSecondGroup();

            double overlap = BoundingBox.calculateOverlapValue(distributionFirstGroup.getBoundingBox(), distributionSecondGroup.getBoundingBox());
            if(minOverlapValue > overlap)
            {
                minOverlapValue = overlap;
                minAreaValue = distributionFirstGroup.getBoundingBox().getArea() + distributionSecondGroup.getBoundingBox().getArea();
                bestDistributionIndex = i;
            }
            // Resolve ties by choosing the distribution with minimum area-value
            else if (minOverlapValue == overlap)
            {
                double area = distributionFirstGroup.getBoundingBox().getArea() + distributionSecondGroup.getBoundingBox().getArea() ;
                if(minAreaValue > area)
                {
                    minAreaValue = area;
                    bestDistributionIndex = i;
                }
            }
        }
        ArrayList<Node> splitNodes = new ArrayList<>();
        DistributionGroup firstGroup = splitAxisDistributions.get(bestDistributionIndex).getFirstGroup();
        DistributionGroup secondGroup = splitAxisDistributions.get(bestDistributionIndex).getSecondGroup();
        splitNodes.add(new Node(level,firstGroup.getEntries()));
        splitNodes.add(new Node(level,secondGroup.getEntries()));
        return splitNodes;
    }

    /**
     * Checks if this node contains an entry with a bounding box that overlaps with the target entry.
     *
     * @param targetEntry The entry to check against the entries in this node.
     * @return true if there is an overlap, false otherwise.
     */
    public boolean contains(Entry targetEntry) {
        for (Entry entry : entries) {
            if (BoundingBox.checkOverlap(entry.getBoundingBox(), targetEntry.getBoundingBox())) {
                return true;
            }
        }
        return false;
    }

}

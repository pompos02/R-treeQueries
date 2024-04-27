package queries;

import main.java.spatialtree.LeafEntry;
import main.java.spatialtree.Node;

import java.util.ArrayList;

// Class use for queries execution with the use of the RStarTree
abstract class Query {
    // Returns the ids of the query's records
    abstract ArrayList<LeafEntry> getQueryRecords(Node node);
}

package queries;

import main.java.spatialtree.LeafEntry;
import main.java.spatialtree.Node;

import java.util.ArrayList;
/**
 * Abstract class serving as the foundation for executing various types of queries using an RStarTree.
 */
abstract class Query {
    // Returns the ids of the query's records
    abstract ArrayList<LeafEntry> getQueryRecords(Node node);

}

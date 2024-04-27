package SequentialQueries;

import main.java.spatialtree.LeafEntry;

import java.util.ArrayList;

/**
 * Abstract base class for sequential scan queries.
 */
abstract public class SequentialScanQuery {

    abstract ArrayList<LeafEntry> getQueryRecords();

}

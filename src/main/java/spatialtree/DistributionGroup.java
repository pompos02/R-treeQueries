package main.java.spatialtree;

import java.util.ArrayList;

 /**
// * Class representing a set of entries and their collective bounding box.
// * This is primarily used during the process of node splitting in an R*-tree to form distribution groups.
// * Each distribution group is a candidate for becoming a separate node after the split.
// */

class DistributionGroup {
     // The list of entries in this distribution group
     private ArrayList<Entry> entries;
     // The bounding box that encloses all the entries in this group.
     // It's the minimum bounding rectangle that contains all the entries.
    private BoundingBox boundingBox;

     /**
      * Constructor for the DistributionGroup class.
      *
      * @param entries      An ArrayList of Entry objects that this group contains.
      * @param boundingBox  The BoundingBox that bounds all the entries in this group.
      */
    DistributionGroup(ArrayList<Entry> entries, BoundingBox boundingBox) {
        this.entries = entries;
        this.boundingBox = boundingBox;
    }

     /**
      * Gets the entries in this distribution group.
      *
      * @return An ArrayList containing the entries.
      */
    ArrayList<Entry> getEntries() {
        return entries;
    }

     /**
      * Gets the bounding box for this distribution group.
      *
      * @return The bounding box enclosing the entries.
      */
    BoundingBox getBoundingBox() {
        return boundingBox;
    }
}

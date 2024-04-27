package main.java.spatialtree;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents an entry in an R*-tree, which contains a bounding box and a reference to a child node.
 * The bounding box covers all the child node's entries' bounding boxes.
 */
public class Entry implements Serializable {
    private BoundingBox boundingBox; // The closed bounded intervals describing the extent of the object along each dimension
    private Long childNodeBlockId; // The address (block ID) of a lower Node (child) in the RStarTree
    /**
     * Constructs an Entry that refers to a child node and sets up its bounding box.
     *
     * @param childNode The child node this entry will refer to.
     */
    Entry(Node childNode) {
        this.childNodeBlockId = childNode.getBlockId();
        adjustBBToFitEntries(childNode.getEntries()); // Adjusting the BoundingBox of the Entry to fit the objects of the childNode
    }
    /**
     * Constructs an Entry with a specified bounding box.
     *
     * @param boundingBox The bounding box of the entry.
     */
    Entry(BoundingBox boundingBox)
    {
        this.boundingBox = boundingBox;
    }
    /**
     * Constructs an Entry with a bounding box and a child node block ID.
     *
     * @param bb The bounding box of the entry.
     * @param blockId The block ID of the child node.
     */
    public Entry(BoundingBox bb, long blockId) {
        this.boundingBox=bb;
        this.childNodeBlockId=blockId;
    }

    void setChildNodeBlockId(Long childNodeBlockId) {
        this.childNodeBlockId = childNodeBlockId;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public ArrayList<Bounds> getBoundingBoxArray() {
        return boundingBox.getBounds();
    }

    public Long getChildNodeBlockId() {
        return childNodeBlockId;
    }
    /**
     * Adjusts the bounding box to fit the given entries.
     * the new bounding box consists of the new minimum bounds created
     * @param entries The entries to be enclosed by the bounding box.
     */
    void adjustBBToFitEntries(ArrayList<Entry> entries){
        boundingBox = new BoundingBox(Bounds.findMinimumBounds(entries));
    }
    /**
     * Adjusts the bounding box to include another entry's bounding box.
     *
     * @param entryToInclude The entry to be included in the bounding box.
     */
    void adjustBBToFitEntry(Entry entryToInclude){
        boundingBox = new BoundingBox(Bounds.findMinimumBounds(boundingBox,entryToInclude.getBoundingBox()));
    }

    public boolean isLeaf() {
        return false; // Default implementation for non-leaf nodes
    }

    // Overridden equals method to compare entries. This default implementation always returns false.
    // TODO check this!
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return false;
    }

}

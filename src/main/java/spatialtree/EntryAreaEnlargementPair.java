package main.java.spatialtree;
/**
 * Class used to pair an Entry object with its area enlargement metric.
 * This pairing is used for comparing and sorting entries based on how much they
 * would enlarge a bounding box if they were to be inserted into it.
 */
class   EntryAreaEnlargementPair implements Comparable {
    private Entry entry; // The Entry object
    private double areaEnlargement; // It's area enlargement assigned
    /**
     * Constructor for the EntryAreaEnlargementPair class.
     *
     * @param entry The Entry object.
     * @param areaEnlargement The area enlargement value associated with the entry.
     */
    EntryAreaEnlargementPair(Entry entry, double areaEnlargement){
        this.entry = entry;
        this.areaEnlargement = areaEnlargement;
    }

    Entry getEntry() {
        return entry;
    }

    private double getAreaEnlargement() {
        return areaEnlargement;
    }
    /**
     * Compares this EntryAreaEnlargementPair with another to determine sorting order based on area enlargement.
     * If two entries have the same area enlargement,
     * the entry with the smaller bounding box area is considered 'less' than the other.
     * @param obj The other EntryAreaEnlargementPair object to be compared with this one.
     * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Object obj) {
        EntryAreaEnlargementPair pairB = (EntryAreaEnlargementPair)obj;
        // Resolve ties by choosing the entry with the rectangle of the smallest area
        if (this.getAreaEnlargement() == pairB.getAreaEnlargement())
            return Double.compare(this.getEntry().getBoundingBox().getArea(),pairB.getEntry().getBoundingBox().getArea());
        else
            return Double.compare(this.getAreaEnlargement(),pairB.getAreaEnlargement());
    }
}

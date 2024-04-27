package main.java.spatialtree;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
/**
 * A class containing multiple comparators for sorting or comparing Entry objects based on various criteria.
 * These are used in the context of managing entries in the R*-tree.
 */
public class EntryComparator {
    /**
     * A comparator that compares two entries based on the values of their bounding box's bounds.
     */
    static class EntryBoundComparator implements Comparator<Entry>
    {
        // Hash-map  used for mapping the comparison value of the Entries during the compare method
        // Key of the hash-map is the given Entry
        // Value of the hash-map is the given Entry's bound (either upper or lower)
        private HashMap<Entry,Double> entryComparisonMap;

        /**
         * Constructs the comparator by mapping each entry to its bound value for comparison.
         *
         * @param entriesToCompare A list of entries to compare.
         * @param dimension        The dimension along which to compare the entries.
         * @param compareByUpper   Whether to compare by the upper bound (true) or lower bound (false).
         */
        EntryBoundComparator(List<Entry> entriesToCompare, int dimension, boolean compareByUpper)
        {
            // Initialising hash-map
            this.entryComparisonMap = new HashMap<>();
            // If the comparison is based on the upper bound
            if (compareByUpper)
            {
                for (Entry entry : entriesToCompare)
                    entryComparisonMap.put(entry,entry.getBoundingBox().getBounds().get(dimension).getUpper());
            }
            // else if the comparison is based on the lower bound
            else
            {
                for (Entry entry : entriesToCompare)
                    entryComparisonMap.put(entry,entry.getBoundingBox().getBounds().get(dimension).getLower());
            }
        }

        @Override
        public int compare(Entry entryA, Entry entryB)
        {
            return Double.compare(entryComparisonMap.get(entryA),entryComparisonMap.get(entryB));
        }
    }
    /**
     * A comparator that compares entries based on how much the area of their bounding box
     * would be enlarged by including another bounding box.
     */
    static class EntryAreaEnlargementComparator implements Comparator<Entry>
    {
        // Hash-map used for mapping the comparison value of the Entries during the compare method
        // First value of the ArrayList is the area of the bounding box
        // Second value of the ArrayList is the area enlargement of the specific Entry
        private HashMap<Entry,ArrayList<Double>> entryComparisonMap;

        EntryAreaEnlargementComparator(List<Entry> entriesToCompare, BoundingBox boundingBoxToAdd)
        {
            // Initialising Hash-map
            this.entryComparisonMap = new HashMap<>();
            for (Entry entry : entriesToCompare)
            {
                BoundingBox entryNewBB = new BoundingBox(Bounds.findMinimumBounds(entry.getBoundingBox(),boundingBoxToAdd));
                ArrayList<Double> values = new ArrayList<>();
                values.add(entry.getBoundingBox().getArea()); // First value of the ArrayList is the area of the bounding box
                double areaEnlargement = entryNewBB.getArea() - entry.getBoundingBox().getArea();
                if (areaEnlargement < 0)
                    throw new IllegalStateException("The enlargement cannot be a negative number");
                values.add(areaEnlargement); // Second value of the ArrayList is the area enlargement of the specific Entry
                entryComparisonMap.put(entry,values);
            }

        }

        @Override
        public int compare(Entry entryA, Entry entryB) {
            double areaEnlargementA = entryComparisonMap.get(entryA).get(1);
            double areaEnlargementB = entryComparisonMap.get(entryB).get(1);
            // Resolve ties by choosing the entry with the rectangle of smallest area
            if (areaEnlargementA == areaEnlargementB)
                return Double.compare(entryComparisonMap.get(entryA).get(0),entryComparisonMap.get(entryB).get(0));
            else
                return Double.compare(areaEnlargementA,areaEnlargementB);
        }
    }
    /**
     * Comparator for comparing entries based on the
     * increase in overlap that would result from adding a new bounding box to each entry's bounding box.
     */
    static class EntryOverlapEnlargementComparator implements Comparator<Entry>
    {
        private BoundingBox boundingBoxToAdd; // The bounding box to add
        private ArrayList<Entry> nodeEntries; // All the entries of the Node

        // Hash-map used for mapping the comparison value of the Entries during the compare method
        // Key of the hash-map is the given Entry
        // Value of the hash-map is the given Entry's overlap Enlargement
        private HashMap<Entry,Double> entryComparisonMap;
        EntryOverlapEnlargementComparator(List<Entry> entriesToCompare, BoundingBox boundingBoxToAdd, ArrayList<Entry> nodeEntries)
        {
            this.boundingBoxToAdd = boundingBoxToAdd;
            this.nodeEntries = nodeEntries;

            // Initialising Hash-map
            this.entryComparisonMap = new HashMap<>();
            for (Entry entry : entriesToCompare)
            {
                double overlapEntry = calculateEntryOverlapValue(entry, entry.getBoundingBox());
                Entry newEntry = new Entry(new BoundingBox(Bounds.findMinimumBounds(entry.getBoundingBox(),boundingBoxToAdd))); // The entry's bounding box after it includes the new bounding box
                double overlapNewEntry = calculateEntryOverlapValue(entry, newEntry.getBoundingBox()); // Using the previous entry signature in order to check for equality
                double overlapEnlargementEntry = overlapNewEntry - overlapEntry ;

                if (overlapEnlargementEntry < 0)
                    throw new IllegalStateException("The enlargement cannot be a negative number");

                entryComparisonMap.put(entry,overlapEnlargementEntry);
            }
        }

        @Override
        public int compare(Entry entryA, Entry entryB) {
            double overlapEnlargementEntryA = entryComparisonMap.get(entryA);
            double overlapEnlargementEntryB = entryComparisonMap.get(entryB);
            // Resolve ties by choosing the entry whose rectangle needs least area enlargement, then
            // the entry with the rectangle of smallest area (which is included in the EntryAreaEnlargementComparator)
            if (overlapEnlargementEntryA == overlapEnlargementEntryB)
            {   ArrayList<Entry> entriesToCompare = new ArrayList<>();
                entriesToCompare.add(entryA);
                entriesToCompare.add(entryB);
                return new EntryAreaEnlargementComparator(entriesToCompare,boundingBoxToAdd).compare(entryA,entryB);
            }
            else
                return Double.compare(overlapEnlargementEntryA,overlapEnlargementEntryB);
        }

        // Calculates and returns the overlap value of the given entry with the other node entries
        double calculateEntryOverlapValue(Entry entry, BoundingBox boundingBox){
            double sum = 0;
            for (Entry nodeEntry : nodeEntries)
            {
                if (nodeEntry != entry)
                    sum += BoundingBox.calculateOverlapValue(boundingBox,nodeEntry.getBoundingBox());
            }
            return sum;
        }
    }
    /**
     * Comparator for comparing entries by the distance from the center of a specified bounding box.
     */
    static class EntryDistanceFromCenterComparator implements Comparator<Entry>
    {
        // Hash-map  used for mapping the comparison value of the Entries during the compare method
        // Key of the hash-map is the given Entry
        // Value of the hash-map is the given Entry's BoundingBox distance from the given BoundingBox
        private HashMap<Entry,Double> entryComparisonMap;


        EntryDistanceFromCenterComparator(List<Entry>entriesToCompare, BoundingBox boundingBox) {
            // Initialising Hash-map
            this.entryComparisonMap = new HashMap<>();

            for (Entry entry : entriesToCompare)
                entryComparisonMap.put(entry,BoundingBox.findDistanceBetweenBoundingBoxes(entry.getBoundingBox(),boundingBox));
        }
        @Override
        public int compare(Entry entryA, Entry entryB)
        {
            return Double.compare(entryComparisonMap.get(entryA),entryComparisonMap.get(entryB));
        }
    }
    /**
     * Comparator for comparing entries based on their distance from a specific point.
     */
    public static class EntryDistanceFromPointComparator implements Comparator<Entry>
    {
        // Hash-map  used for mapping the comparison value of the Entries during the compare method
        // Key of the hash-map is the given Entry
        // Value of the hash-map is the given Entry's BoundingBox distance from the given point
        private HashMap<Entry,Double> entryComparisonMap;

        public EntryDistanceFromPointComparator(List<Entry> entriesToCompare, ArrayList<Double> point) {
            // Initialising Hash-map
            this.entryComparisonMap = new HashMap<>();

            for (Entry entry : entriesToCompare)
                entryComparisonMap.put(entry,entry.getBoundingBox().findMinDistanceFromPoint(point));
        }
        @Override
        public int compare(Entry entryA, Entry entryB)
        {
            return Double.compare(entryComparisonMap.get(entryA),entryComparisonMap.get(entryB));
        }
    }
}

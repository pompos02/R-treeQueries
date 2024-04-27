package main.java.spatialtree;

/**
 * Class used to represent the distribution of a set of entries into two groups.
 * Essential for Node splitting
 */
class Distribution {
    private DistributionGroup firstGroup; // The first distribution group
    private DistributionGroup secondGroup; // The second distribution group

    /**
     * Constructor for the Distribution class.
     * Initializes a Distribution object with two groups.
     *
     * @param firstGroup The first distribution group.
     * @param secondGroup The second distribution group.
     */
    Distribution(DistributionGroup firstGroup, DistributionGroup secondGroup) {
        this.firstGroup = firstGroup;
        this.secondGroup = secondGroup;
    }

    /**
     * Gets the first group of the distribution.
     *
     * @return The first distribution group.
     */
    DistributionGroup getFirstGroup() {
        return firstGroup;
    }

    /**
     * Gets the second group of the distribution.
     *
     * @return The second distribution group.
     */
    DistributionGroup getSecondGroup() {
        return secondGroup;
    }
}

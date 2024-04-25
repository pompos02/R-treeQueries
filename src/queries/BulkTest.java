package queries;

import main.java.spatialtree.BoundingBox;
import main.java.spatialtree.Bounds;
import main.java.spatialtree.BulkLoadingRStarTree;
import main.java.spatialtree.LeafEntry;

import java.util.ArrayList;

public class BulkTest {

    public static void main(String[] args) {
        // Test initialization
        System.out.println("Starting the test...");

        // Initialize the tree with the flag to simulate loading from a data file
        BulkLoadingRStarTree tree = new BulkLoadingRStarTree(true);

        // Test if the tree root is correctly initialized
        if (tree.getRoot() != null) {
            System.out.println("Test passed: Root is initialized.");
        } else {
            System.out.println("Test failed: Root is not initialized.");
        }

        // Test the number of levels in the tree
        // Assuming we know the expected number of levels after bulk loading the given data
        int expectedLevels = 3; // This value should be set based on expected test data
        if (tree.getTotalLevels() == expectedLevels) {
            System.out.println("Test passed: Correct number of levels in the tree.");
        } else {
            System.out.println("Test failed: Incorrect number of levels. Expected: " + expectedLevels + ", but was: " + tree.getTotalLevels());
        }

        // Perform a simple range query test

        ArrayList<Bounds> searchBoundingBox = new ArrayList<>();
        searchBoundingBox.add(new Bounds(0, 1));
        searchBoundingBox.add(new Bounds(0, 1));
        ArrayList<LeafEntry> foundEntries = tree.getDataInBoundingBox(new BoundingBox(searchBoundingBox));
        // Check if the correct number of entries is found
        int expectedEntriesCount = 2; // This should match the expected result from the test data
        if (foundEntries.size() == expectedEntriesCount) {
            System.out.println("Test passed: Correct number of entries found in bounding box.");
        } else {
            System.out.println("Test failed: Incorrect number of entries found. Expected: " + expectedEntriesCount + ", but was: " + foundEntries.size());
        }

        System.out.println("Test completed.");
    }
}

package Tests;

import main.java.spatialtree.*;
import main.java.spatialtree.Record;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SkyLine {

    public static void main(String[] args) throws IOException {


        System.out.println("Initializing files:");
        List<Record> records = DataFileManagerNoName.loadDataFromFile("map.osm");
        helper.CreateDataFile(records,2, true);
        helper.CreateIndexFile(2,false);
        System.out.println("creating R*-tree");
        BulkLoadingRStarTree rStarTree = new BulkLoadingRStarTree(true);

        ArrayList<Bounds> queryBounds = new ArrayList<>();
        queryBounds.add(new Bounds(34.7018620-0.1 , 34.7018620+0.1));
        queryBounds.add(new Bounds(33.0449947 - 0.1, 33.0449947 + 0.1));
        System.out.print("Starting SkyLine query: ");
        long startRangeQueryTime = System.nanoTime();
        ArrayList<LeafEntry> queryRecords = rStarTree.getSkyline(new BoundingBox(queryBounds));
        long stopRangeQueryTime = System.nanoTime();
        System.out.print("range query Done ");

        System.out.println("Entires found in the given region: " + queryRecords.size());
        System.out.println("Total levels of the tree: " + helper.getTotalLevelsOfTreeIndex());
        System.out.println("writing them to outputSkyLineQuery.csv ");
        try (FileWriter csvWriter = new FileWriter("outputSkyLineQuery.csv")) {
            // Write the CSV header
            csvWriter.append("ID,Name,Latitude,Longitude \n");

            // Loop through records and write each to the file
            int counter=0;
            for (LeafEntry leafRecord : queryRecords) {
                counter++;
                // Assuming findRecord() returns a comma-separated string "id,name,lat,lon"
                csvWriter.append(counter + ". " + leafRecord.findRecordWithoutBlockId().toString());
                csvWriter.append("\n");  // New line after each record
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
        System.out.println("Time taken: " + (double) (stopRangeQueryTime - startRangeQueryTime) / 1_000_000_000.0 + " seconds");


    }
}

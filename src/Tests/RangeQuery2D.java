package Tests;
import main.java.spatialtree.*;
import main.java.spatialtree.Record;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;

public class RangeQuery2D {
    public static void main(String[] args) throws IOException {

        // ------------------------------------------------------------------------
        // Range Query Data
        ArrayList<Double> rStarRangeQueryTimes = new ArrayList<>();
        ArrayList<Double> seqScanRangeQueryTimes = new ArrayList<>();
        ArrayList<Double> areaOfRectangles = new ArrayList<>();
        ArrayList<Integer> rangeQueryRecords = new ArrayList<>();

        // ------------------------------------------------------------------------





        List<Record> records = DataFileManagerWithName.loadDataFromFile("map.osm");
        System.out.println("creating datafile: ");
        helper.CreateDataFile(records,2, true);
        System.out.println("DONE");
        System.out.println("creating index file: ");
        helper.CreateIndexFile(2,false);
        System.out.println("DONE");
        System.out.println("creating r*-tree");
        RStarTree rStarTree = new RStarTree(true);
        System.out.println("DONE");
        ArrayList<Bounds> queryBounds = new ArrayList<>();
        queryBounds.add(new Bounds(34.7018620-0.5 , 34.7018620+0.5));
        queryBounds.add(new Bounds(33.0449947 - 0.67, 33.0449947 + 0.67));


        System.out.print("Starting range query: ");
        long startRangeQueryTime = System.nanoTime();
        ArrayList<LeafEntry> queryRecords = rStarTree.getDataInBoundingBox(new BoundingBox(queryBounds));
        long stopRangeQueryTime = System.nanoTime();
        System.out.print("range query Done ");
        System.out.println("Entires found in the given region: " + queryRecords.size());
        System.out.println("writing them to output2DRangeQuery.csv ");
        try (FileWriter csvWriter = new FileWriter("output2DRangeQuery.csv")) {
            // Write the CSV header
            csvWriter.append("ID,Name,Latitude,Longitude \n");

            // Loop through records and write each to the file
            int counter=0;
            for (LeafEntry leafRecord : queryRecords) {
                counter++;
                // Assuming findRecord() returns a comma-separated string "id,name,lat,lon"
                csvWriter.append(counter + ". " + leafRecord.findRecord().toString());
                csvWriter.append("\n");  // New line after each record
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
        System.out.println(queryRecords.size());
        System.out.println("Time taken: " + (double) (stopRangeQueryTime - startRangeQueryTime) / 1_000_000_000.0 + " seconds");


    }

}

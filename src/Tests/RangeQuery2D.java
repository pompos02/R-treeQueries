package Tests;
import main.java.spatialtree.*;
import main.java.spatialtree.Record;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;

public class RangeQuery2D {
    public static void main(String[] args) throws IOException {


        System.out.println("Initializing files:");
        List<Record> records = DataFileManagerNoName.loadDataFromFile("malta.osm");
        helper.CreateDataFile(records,2, true);
        helper.CreateIndexFile(2,false);
        System.out.println("creating R*-tree");
        RStarTree rStarTree = new RStarTree(true);

        //QUERY
        ArrayList<Bounds> queryBounds = new ArrayList<>();
        // 7111836589,,31.72438,28.42733
        //all of malta: 14.2932,14.6000,    36.0224,35.7700
        //center 14.4343,14.4511,35.8779,35.8922
        double off=0.1;
        queryBounds.add(new Bounds(35.9-off , 35.9+off));
        queryBounds.add(new Bounds(14.4-off , 14.4+off));
        System.out.print("Starting range query: ");
        long startRangeQueryTime = System.nanoTime();
        ArrayList<LeafEntry> queryRecords = rStarTree.getDataInBoundingBox(new BoundingBox(queryBounds));
        long stopRangeQueryTime = System.nanoTime();
        System.out.print("range query Done ");




        System.out.println("Entires found in the given region: " + queryRecords.size());
        System.out.println("Total levels of the tree: " + helper.getTotalLevelsOfTreeIndex());
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
        System.out.println("Time taken: " + (double) (stopRangeQueryTime - startRangeQueryTime) / 1_000_000_000.0 + " seconds");


    }

}

package Tests;

import main.java.spatialtree.Record;
import main.java.spatialtree.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecordDeletion {
    public static void main(String[] args) throws IOException {
        System.out.println("Initializing the files: ");
        List<Record> records = DataFileManagerNoName.loadDataFromFile("malta.osm");
        helper.CreateDataFile(records,2, true);
        helper.CreateIndexFile(2,false);

        System.out.println("creating R*-tree");
        RStarTree rStarTree = new RStarTree(true);


        //Query the Entry you want to delete
        //205. 60170093,Μέσα Γειτονιά,34.701862,33.0449947 for map.osm
        ArrayList<Bounds> queryBoundsForDeletion = new ArrayList<>();
        queryBoundsForDeletion.add(new Bounds(34.7018620, 34.7018620));
        queryBoundsForDeletion.add(new Bounds(33.0449947, 33.0449947));
        ArrayList<LeafEntry> queryRecordsForDeletion = rStarTree.getDataInBoundingBox(new BoundingBox(queryBoundsForDeletion));

        try{
            System.out.println("Deleting Record with ID:  " + queryRecordsForDeletion.get(0).getRecordId());
            LeafEntry EntryForDeletion = queryRecordsForDeletion.get(0);
            rStarTree.deleteRecord(EntryForDeletion);
            System.out.println("Deleted ");
        } catch (Exception e) {
            throw new IllegalStateException("Entry not found!");
        }

        // Query to check if the entry was deleted successfully ( you should find the RecordID in the .csv file )
        ArrayList<Bounds> queryBounds = new ArrayList<>();
        queryBounds.add(new Bounds(34.7018620-0.1 , 34.7018620+0.1));
        queryBounds.add(new Bounds(33.0449947 - 0.1, 33.0449947 + 0.1));
        System.out.print("Starting range query: ");
        long startRangeQueryTime = System.nanoTime();
        ArrayList<LeafEntry> queryRecords = rStarTree.getDataInBoundingBox(new BoundingBox(queryBounds));
        long stopRangeQueryTime = System.nanoTime();
        System.out.print("range query Done ");




        System.out.println("Entires found in the given region: " + queryRecords.size());
        System.out.println("Total levels of the tree: " + helper.getTotalLevelsOfTreeIndex());
        System.out.println("writing them to output2DRangeQuery.csv ");
        try (FileWriter csvWriter = new FileWriter("output2DRangeQueryWithDeletion.csv")) {
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

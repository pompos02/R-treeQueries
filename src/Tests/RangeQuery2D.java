package Tests;

import SequentialQueries.SequentialScanBoundingBoxRangeQuery;
import main.java.spatialtree.Record;
import main.java.spatialtree.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RangeQuery2D {


    static RStarTree rStarTreeMaker(boolean reconstruct) throws IOException {
        if(reconstruct){
            System.out.println("Initializing files:");
            List<Record> records = DataFileManagerNoName.loadDataFromFile("../malta.osm");
            helper.CreateDataFile(records,2, true);
            helper.CreateIndexFile(2,true);
            System.out.println("creating R*-tree");
            RStarTree rStarTree = new RStarTree(true);
            return rStarTree;
        }
        else{
            List<Record> EmptyRecords = new ArrayList<>();
            helper.CreateDataFile(EmptyRecords,2, false);
            helper.CreateIndexFile(2,false);
            System.out.println("creating R*-tree");
            RStarTree rStarTree = new RStarTree(false);
            return rStarTree;
        }
    }
    public static void main(String[] args) throws IOException {

        boolean reconstruct = false;

        RStarTree rStarTree= rStarTreeMaker(reconstruct);


        //QUERY
        ArrayList<Bounds> queryBounds = new ArrayList<>();
        // 7111836589,,31.72438,28.42733
        //all of malta: 14.2932,14.6000,    36.0224,35.7700
        //center 14.4343,14.4511,35.8779,35.8922
        double off=0.000035 * 1000;
        queryBounds.add(new Bounds(35.9-off , 35.9+off));
        queryBounds.add(new Bounds(14.4-off , 14.4+off));
        //queryBounds.add(new Bounds(34.7018620-off , 34.7018620+off));
        //queryBounds.add(new Bounds(33.0449947-off , 33.0449947+off));



        System.out.println("Starting R*-Tree range query: ");
        long startRangeQueryTime = System.nanoTime();
        ArrayList<LeafEntry> queryRecords = rStarTree.getDataInBoundingBox(new BoundingBox(queryBounds));
        long stopRangeQueryTime = System.nanoTime();
        System.out.println("Records found in the given region: " + queryRecords.size());
        System.out.println("Time taken: " + (double) (stopRangeQueryTime - startRangeQueryTime) / 1_000_000_000.0 + " seconds");


        System.out.println("---------------------------------------------------------------");

        // Sequential Scan - Range Query
        System.out.println("Starting RangeQuery With Sequential scan : ");
        SequentialScanBoundingBoxRangeQuery sequentialScanBoundingBoxRangeQuery = new SequentialScanBoundingBoxRangeQuery(new BoundingBox(queryBounds));
        long startSequentialRangeQueryTime = System.nanoTime();
        ArrayList<LeafEntry> SequentialQueryRecords=sequentialScanBoundingBoxRangeQuery.getQueryRecords();
        long stopSequentialRangeQueryTime = System.nanoTime();
        System.out.println("Records found in the given region: " + SequentialQueryRecords.size());
        System.out.println("Time taken Sequential scan:  " + (double) (stopSequentialRangeQueryTime - startSequentialRangeQueryTime) / 1_000_000_000.0 + " seconds");



        boolean write=true;
        if(write){
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
        }




    }

}

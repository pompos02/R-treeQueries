package Tests;

import SequentialQueries.SequentialNearestNeighboursQuery;
import SequentialQueries.SequentialScanBoundingBoxRangeQuery;
import main.java.spatialtree.Record;
import main.java.spatialtree.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KNN {
    static RStarTree rStarTreeMaker(boolean reconstruct) throws IOException {
        if(reconstruct){
            System.out.println("Initializing files:");
            List<Record> records = DataFileManagerNoName.loadDataFromFile("malta.osm");
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

        ArrayList<Double> centerPoint = new ArrayList<>(); // ArrayList with the coordinates of an approximate center point
        centerPoint.add(14.4); // Coordinate of second dimension
        centerPoint.add(35.9); // Coordinate of first dimension

        boolean reconstruct = false;

        RStarTree rStarTree= rStarTreeMaker(reconstruct);


        //QUERY
        ArrayList<Bounds> queryBounds = new ArrayList<>();
        queryBounds.add(new Bounds(centerPoint.get(0) , centerPoint.get(0)));
        queryBounds.add(new Bounds(centerPoint.get(1), centerPoint.get(1)));

        int k=100;
        System.out.print("Starting R*-Tree K-NN query: ");
        long startKNNTime = System.nanoTime();
        ArrayList<LeafEntry> queryRecords = rStarTree.getNearestNeighbours(centerPoint, k);
        long stopKNNTime = System.nanoTime();
        System.out.println("Records found in the given region: " + queryRecords.size());
        System.out.println("Time taken: " + (double) (stopKNNTime - startKNNTime) / 1_000_000_000.0 + " seconds");

        System.out.println("---------------------------------------------------------------");

        // Sequential Scan - Range Query
        System.out.println("Starting K-NN With Sequential scan : ");
        SequentialNearestNeighboursQuery sequentialScanNearestNeighboursQuery = new SequentialNearestNeighboursQuery(centerPoint,k);
        long startSequentialRangeQueryTime = System.nanoTime();
        ArrayList<LeafEntry> SequentialQueryRecords=sequentialScanNearestNeighboursQuery.getQueryRecords();
        long stopSequentialRangeQueryTime = System.nanoTime();
        System.out.println("Records found in the given region: " + SequentialQueryRecords.size());
        System.out.println("Time taken Sequential scan:  " + (double) (stopSequentialRangeQueryTime - startSequentialRangeQueryTime) / 1_000_000_000.0 + " seconds");

        System.out.println("Entires found in the given region: " + queryRecords.size());
        System.out.println("writing them to outputKNNQuery.csv ");
        Boolean write=true;
        if(write){
            try (FileWriter csvWriter = new FileWriter("outputKNNQuery.csv")) {
                // Write the CSV header
                csvWriter.append("ID,Name,Latitude,Longitude \n");

                // Loop through records and write each to the file
                int counter=0;
                for (LeafEntry leafRecord : SequentialQueryRecords) {
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

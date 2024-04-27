package Tests;

import main.java.spatialtree.Record;
import main.java.spatialtree.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KNNBulk {
    public static void main(String[] args) throws IOException {

        ArrayList<Double> centerPoint = new ArrayList<>(); // ArrayList with the coordinates of an approximate center point
        centerPoint.add(33.0449947); // Coordinate of second dimension
        centerPoint.add(34.701862); // Coordinate of first dimension
        //205. 60170093,Μέσα Γειτονιά,34.701862,33.0449947 for map.osm
        System.out.println("Initializing files:");
        List<Record> records = DataFileManagerNoName.loadDataFromFile("map.osm");
        helper.CreateDataFile(records,2, true);
        helper.CreateIndexFile(2,false);
        System.out.println("creating R*-tree");
        BulkLoadingRStarTree rStarTree = new BulkLoadingRStarTree(true);

        //QUERY
        ArrayList<Bounds> queryBounds = new ArrayList<>();
        queryBounds.add(new Bounds(centerPoint.get(0) , centerPoint.get(0)));
        queryBounds.add(new Bounds(centerPoint.get(1), centerPoint.get(1)));

        int k=4;
        System.out.print("Starting KNN query: ");
        long startKNNTime = System.nanoTime();
        ArrayList<LeafEntry> queryRecords = rStarTree.getNearestNeighbours(centerPoint, k);
        long stopKNNTime = System.nanoTime();
        System.out.print("range query Done ");




        System.out.println("Entires found in the given region: " + queryRecords.size());
        System.out.println("writing them to outputKNNBulkQuery.csv ");
        try (FileWriter csvWriter = new FileWriter("outputKNNBulkQuery.csv")) {
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
        System.out.println("Time taken: " + (double) (stopKNNTime - startKNNTime) / 1_000_000_000.0 + " seconds");


    }
}

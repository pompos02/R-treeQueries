package OutputQueries;

import SequentialQueries.SequentialScanBoundingBoxRangeQuery;
import main.java.spatialtree.*;
import main.java.spatialtree.Record;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Run2DQueries {
    static RStarTree rStarTreeMaker(boolean reconstruct) throws IOException {
        if(reconstruct){
            System.out.println("Initializing files:");
            List<Record> records = DataFileManagerWithName.loadDataFromFile("malta.osm");
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
            System.out.println("R*-tree Constructed");
            return rStarTree;
        }
    }

    public static void main(String[] args) throws IOException {

        boolean reconstruct = false;
        RStarTree rStarTree= rStarTreeMaker(reconstruct);
        // CenterPoint
        ArrayList<Double> centerPoint = new ArrayList<>(); // ArrayList with the coordinates of an approximate center point
        centerPoint.add(35.9); // Coordinate of second dimension
        centerPoint.add(14.4); // Coordinate of first dimension
        double rangeIncrement = 0.0000025;
        // ------------------------------------------------------------------------
        // Range Query Data
        ArrayList<Double> rStarRangeQueryTimes = new ArrayList<>();
        ArrayList<Double> seqScanRangeQueryTimes = new ArrayList<>();
        ArrayList<Integer> rangeQueryRecords = new ArrayList<>();

        // KNN Query Data
        ArrayList<Double> knnRStarTimes = new ArrayList<>();
        ArrayList<Double> knnSeqScanTimes = new ArrayList<>();

        int i=0;

        while(i<10000){

            if(i%100 == 0){
                //Range Query

                ArrayList<Bounds> queryBounds = new ArrayList<>();
                queryBounds.add(new Bounds(centerPoint.get(0) - i*rangeIncrement, centerPoint.get(0) + i*rangeIncrement));
                queryBounds.add(new Bounds(centerPoint.get(1) - i*rangeIncrement, centerPoint.get(1) + i*rangeIncrement));

                long startRangeQueryTime = System.nanoTime();
                rangeQueryRecords.add(rStarTree.getDataInBoundingBox(new BoundingBox(queryBounds)).size());
                long stopRangeQueryTime = System.nanoTime();
                rStarRangeQueryTimes.add((double) (stopRangeQueryTime - startRangeQueryTime) / 1000000);
                // Sequential Scan - Range Query
                SequentialScanBoundingBoxRangeQuery sequentialScanBoundingBoxRangeQuery = new SequentialScanBoundingBoxRangeQuery(new BoundingBox(queryBounds));
                long startSequentialRangeQueryTime = System.nanoTime();
                sequentialScanBoundingBoxRangeQuery.getQueryRecords();
                long stopSequentialRangeQueryTime = System.nanoTime();
                seqScanRangeQueryTimes.add((double) (stopSequentialRangeQueryTime - startSequentialRangeQueryTime) / 1000000);
                System.out.println("i: " + i);
            }
            i++;
        }
        System.out.println("Writing on file");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("rangeQueryResults.csv"))) {
            String tagString = "Returned Records" +
                    ',' +
                    "R* Time(ms)" +
                    ',' +
                    "Sequential Scan Time(ms)" +
                    '\n';
            writer.write(tagString);

            // Range Query File creation
            int j = 0;
            while(j < rStarRangeQueryTimes.size()){
                writer.write(String.format(  rangeQueryRecords.get(j) +"," +rStarRangeQueryTimes.get(j)+ "," + seqScanRangeQueryTimes.get(j) + "\n"));
                j++;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }




    }
}

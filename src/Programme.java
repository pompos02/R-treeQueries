import main.java.spatialtree.*;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;

import static OutputQueries.Run2DQueries.rStarTreeMaker;

public class Programme {

    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);
        String selection;
        System.out.println("Welcome! Wait until we create the R*-Tree from the existing IndexFile based on malta.osm");
        System.out.println("--------------------------------------------------------------------------------");
        RStarTree rStarTree = rStarTreeMaker(false);

        do {
            System.out.println("Select the query you want:");
            System.out.println("1)Search for Records that overlap with a given bounding box\n2)K-NN\n3)Skyline\n0)To Exit");
            selection = scan.nextLine().trim().toLowerCase();
            System.out.println();

            switch (selection) {
                case "1":
                    System.out.println("Range Query selected");
                    System.out.println("example x bounds: 35.8 35.9");
                    System.out.println("example y bounds: 14.4 14.44");
                    ArrayList<Bounds> queryBounds1 = new ArrayList<>();
                    for (int i = 0; i < helper.getDataDimensions(); i++) {
                        while (true) {
                            int dim = i + 1;
                            System.out.print("Give the bounds for dimension " + dim + ": ");
                            double lowerBound = scan.nextDouble();
                            double upperBound = scan.nextDouble();
                            System.out.println();
                            if (lowerBound <= upperBound) {
                                queryBounds1.add(new Bounds(lowerBound, upperBound));
                                break;
                            } else {
                                System.out.println("The lower value of the bounds cannot be bigger than the upper");
                            }
                        }
                    }
                    scan.nextLine(); // consume the leftover newline

                    long startRangeQueryTime = System.nanoTime();
                    ArrayList<LeafEntry> queryRecords1 = rStarTree.getDataInBoundingBox(new BoundingBox(queryBounds1));
                    long stopRangeQueryTime = System.nanoTime();
                    System.out.println("Records found in the given region: " + queryRecords1.size());
                    System.out.println("Time taken: " + (double) (stopRangeQueryTime - startRangeQueryTime) / 1_000_000_000.0 + " seconds");
                    System.out.println("Do you want to write the results to a file? y/n");
                    String write1 = scan.nextLine().trim().toLowerCase();
                    System.out.println();
                    if (write1.equals("y")) {
                        System.out.println("writing results to output2DRangeQuery.csv ");
                        try (FileWriter csvWriter = new FileWriter("output2DRangeQuery.csv")) {
                            // Write the CSV header
                            csvWriter.append("ID,Name,Latitude,Longitude \n");

                            // Loop through records and write each to the file
                            int counter = 0;
                            for (LeafEntry leafRecord : queryRecords1) {
                                counter++;
                                // Assuming findRecord() returns a comma-separated string "id,name,lat,lon"
                                csvWriter.append(counter + ". " + leafRecord.findRecord().toString());
                                csvWriter.append("\n");  // New line after each record
                            }
                        } catch (IOException e) {
                            System.err.println("Error writing to CSV file: " + e.getMessage());
                        }
                    } else if (write1.equals("n")) {
                        System.out.println("Output file didn't get created");
                    } else {
                        System.out.println("Wrong input!");
                    }
                    System.out.println();
                    break;
                case "2":
                    System.out.println("K-NN query selected");
                    System.out.println("Give the center coordinates of each dimension");
                    System.out.println("Input Example\ndimension 1: 35.9\ndimension 2: 14.4");
                    ArrayList<Double> centerPoint = new ArrayList<>(); // The point's center
                    for (int i = 0; i < helper.getDataDimensions(); i++) {
                        int dim = i + 1;
                        System.out.print("Give the coordinate of reference point's center in dimension " + dim + ": ");
                        double coordinate = scan.nextDouble();
                        System.out.println();
                        centerPoint.add(coordinate);
                    }
                    scan.nextLine(); // consume the leftover newline

                    int k;
                    System.out.print("Give the value of k (the number of nearest neighbours to get): ");
                    k = scan.nextInt();
                    System.out.println();
                    scan.nextLine(); // consume the leftover newline

                    long startKNNTime = System.nanoTime();
                    ArrayList<LeafEntry> queryRecords2 = rStarTree.getNearestNeighbours(centerPoint, k);
                    long stopKNNTime = System.nanoTime();
                    System.out.println("Records found: " + queryRecords2.size());
                    System.out.println("Time taken: " + (double) (stopKNNTime - startKNNTime) / 1_000_000_000.0 + " seconds");
                    System.out.println("Do you want to write the results to a file? y/n");
                    String write2 = scan.nextLine().trim().toLowerCase();
                    System.out.println();
                    if (write2.equals("y")) {
                        System.out.println("writing results to outputKNNQuery.csv ");
                        try (FileWriter csvWriter = new FileWriter("outputKNNQuery.csv")) {
                            // Write the CSV header
                            csvWriter.append("ID,Name,Latitude,Longitude \n");

                            // Loop through records and write each to the file
                            int counter = 0;
                            for (LeafEntry leafRecord : queryRecords2) {
                                counter++;
                                // Assuming findRecord() returns a comma-separated string "id,name,lat,lon"
                                csvWriter.append(counter + ". " + leafRecord.findRecord().toString());
                                csvWriter.append("\n");  // New line after each record
                            }
                        } catch (IOException e) {
                            System.err.println("Error writing to CSV file: " + e.getMessage());
                        }
                    } else if (write2.equals("n")) {
                        System.out.println("Output file didn't get created");
                    } else {
                        System.out.println("Wrong input!");
                    }
                    System.out.println();
                    break;
                case "3":
                    System.out.println("Skyline query selected");

                    System.out.println("example x bounds: 30 40");
                    System.out.println("example y bounds: 10 20");
                    ArrayList<Bounds> queryBounds3 = new ArrayList<>();
                    for (int i = 0; i < helper.getDataDimensions(); i++) {
                        while (true) {
                            int dim = i + 1;
                            System.out.print("Give the bounds for dimension " + dim + ": ");
                            double lowerBound = scan.nextDouble();
                            double upperBound = scan.nextDouble();
                            System.out.println();
                            if (lowerBound <= upperBound) {
                                queryBounds3.add(new Bounds(lowerBound, upperBound));
                                break;
                            } else {
                                System.out.println("The lower value of the bounds cannot be bigger than the upper");
                            }
                        }
                    }
                    scan.nextLine(); // consume the leftover newline
                    long startSkylineQueryTime = System.nanoTime();
                    ArrayList<LeafEntry> queryRecords3 = rStarTree.getSkyline(new BoundingBox(queryBounds3));
                    long stopSkylineQueryTime = System.nanoTime();
                    System.out.println("Entries found in the given region: " + queryRecords3.size());
                    System.out.println("Time taken: " + (double) (stopSkylineQueryTime - startSkylineQueryTime) / 1_000_000_000.0 + " seconds");
                    System.out.println("Do you want to write the results to a file? y/n");
                    String write3 = scan.nextLine().trim().toLowerCase();
                    System.out.println();
                    if (write3.equals("y")) {
                        System.out.println("writing results to outputSkyLineQuery.csv ");
                        try (FileWriter csvWriter = new FileWriter("outputSkyLineQuery.csv")) {
                            // Write the CSV header
                            csvWriter.append("ID,Name,Latitude,Longitude \n");

                            // Loop through records and write each to the file
                            int counter = 0;
                            for (LeafEntry leafRecord : queryRecords3) {
                                counter++;
                                // Assuming findRecord() returns a comma-separated string "id,name,lat,lon"
                                csvWriter.append(counter + ". " + leafRecord.findRecord().toString());
                                csvWriter.append("\n");  // New line after each record
                            }
                        } catch (IOException e) {
                            System.err.println("Error writing to CSV file: " + e.getMessage());
                        }
                    } else if (write3.equals("n")) {
                        System.out.println("Output file didn't get created");
                    } else {
                        System.out.println("Wrong input!");
                    }
                    System.out.println();
                    break;
                case "0":
                    System.out.println("Exiting program.");
                    break;
                default:
                    System.out.println("Invalid selection, please try again.");
                    break;
            }
        } while (!selection.equals("0"));
    }
}

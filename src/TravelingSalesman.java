import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.io.*;
import java.time.chrono.MinguoChronology;
import java.util.Arrays;

public class TravelingSalesman {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    /* define constants */
    static long MAXVALUE =  2000000000;
    static long MINVALUE = -2000000000;
    static int numberOfTrials = 100;
    static int MAXINPUTSIZE  = (int) Math.pow(2,10);
    static int MININPUTSIZE  =  1;
    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time

    static String ResultsFolderPath = "/home/karson/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;


    public static void main(String[] args) {

        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        runFullExperiment("Brute-Exp1-ThrowAway.txt");
        runFullExperiment("Brute-Exp2.txt");
        runFullExperiment("Brute-Exp3.txt");
    }

    static void runFullExperiment(String resultsFileName){

        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch(Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file "+ResultsFolderPath+resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();
        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for(int inputSize=MININPUTSIZE;inputSize<=MAXINPUTSIZE; inputSize*=2) {
            // progress message...
            System.out.println("Running test for input size "+inputSize+" ... ");


            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of randomly spaced integers in ascending sorted order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            // but we will randomly generate the search key for each trial
            System.out.print("    Generating test data...");

            //generate random cost matrix, max cost size of 20
            int matrix[][] = costMatrix(inputSize,inputSize);

            System.out.println("...done.");
            System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();


            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the tirals
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // generate a random key to search in the range of a the min/max numbers in the list
                //long testSearchKey = (long) (0 + Math.random() * (testList[testList.length-1]));
                /* force garbage collection before each trial run so it is not included in the time */
                // System.gc();

                //TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */

                int brute[] = bruteForce(matrix,inputSize);


                // batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double)numberOfTrials; // calculate the average time per trial in this batch

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f \n",inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");
        }
    }

    //generate random cost matrix
    public static int[][] costMatrix(int a, int b){
        int M[][] = new int[a][b];

        for(int i = 0; i < a; i++){
            for(int j = 0; j<b; j++){
                if(a==b)
                    M[i][j]=0;
                else {
                    M[i][j] = (int) (Math.random() * 20);
                    M[j][i] = M[i][j];
                }
            }
        }

        return M;
    }

    public static int[] bruteForce(int M[][],int size){
        int[] V = new int[size];

        //fill V with large garbage values
        for(int i = 0; i < size;i++)
            V[i] = 99;

        //find optimul route
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(M[i][j] < V[i]){
                    V[i] = M[i][j];
                }
            }
        }

        return V;
    }
}
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

    //size of matrix's x and y
    static int n = 7;
    //cost matrix
    static int matrix[][] = {
        {0,2,5,3,7,5,3},
        {2,0,5,3,2,1,6},
        {5,5,0,3,8,1,2},
        {3,3,3,0,7,4,2},
        {7,2,8,7,0,2,3},
        {5,1,1,4,2,0,2},
        {2,6,2,2,3,2,0}};
    static int VISITED_ALL = (1<<n) -1;
    static int dp[][] = new int[n][n];

    public static void main(String[] args) {

        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        runFullExperiment("BruteTSP-Exp1-ThrowAway.txt");
        runFullExperiment("BruteTSP-Exp2.txt");
        runFullExperiment("BruteTSP-Exp3.txt");
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
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trials

        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();
        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for(int inputSize=MININPUTSIZE;inputSize<=MAXINPUTSIZE; inputSize*=2) {
            // progress message...
            System.out.println("Running test for input size "+inputSize+" ... ");

            //generate a random matrix
            //int randomMatrix[][] = generateRandomCostMatrix(n,n);

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of randomly spaced integers in ascending sorted order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            // but we will randomly generate the search key for each trial
            System.out.print("    Generating test data...");

            //generate random cost matrix, max cost size of 20
            //int matrix[][] = costMatrix(inputSize,inputSize);

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

                int brute = TspBrute(matrix, 0);
                //int dynamic = TspDynamic(1,0);
                //int greedy[] = TspGreedy(matrix, n);


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
    public static int[][] generateRandomCostMatrix(int a, int b){
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

    public static int TspBrute(int graph[][], int s)
    {
        // store all vertex apart from source vertex
        int[] vertex = new int[n+1];
        for (int i = 0; i < n; i++)
            if (i != s)
                vertex[i]=i;

        // store minimum weight Hamiltonian Cycle.
        int min_path = MAXINPUTSIZE;
        do {

            // store current Path weight(cost)
            int current_pathweight = 0;

            // compute current path weight
            int k = s;
            for (int i = 0; i < vertex.length; i++) {
                current_pathweight += graph[k][vertex[i]];
                k = vertex[i];
            }
            current_pathweight += graph[k][s];

            // update minimum
            min_path = Math.min(min_path, current_pathweight);

            vertex[n] = vertex[0];
        } while (vertex[0] != vertex[n]);

        return min_path;
    }

    public static int TspDynamic(int mask,int pos){

        if(mask==VISITED_ALL){
            return matrix[pos][0];
        }
        if(dp[mask][pos]!=-1){
            return dp[mask][pos];
        }

        //Now from current node, we will try to go to every other node and take the min ans
        int ans = 999999;

        //Visit all the unvisited cities and take the best route
        for(int city=0;city<n;city++){

            if((mask&(1<<city))==0){

                int newAns = matrix[pos][city] + TspDynamic( mask|(1<<city), city);
                ans = Math.min(ans, newAns);
            }

        }

        return dp[mask][pos] = ans;
    }

    public static int[] TspGreedy(int M[][],int size){
        int[] V = new int[size];

        //fill V with large garbage values
        for(int i = 0; i < size;i++)
            V[i] = 99999;

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
package nl.tudelft.instrumentation.fuzzing;

import java.util.*;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * You should write your own solution using this class.
 */
public class FuzzingLab {
        private static final float K = 1;
        static Random r = new Random();
        static List<String> currentTrace;
        static int traceLength = 10;
        static String currentTraceSymbol;

        static HashSet<Integer> visitedBranches = new HashSet<Integer>();
        static HashMap<String, HashSet<Integer>> branchesPerTrace =new  HashMap<String, HashSet<Integer>>();
        static HashMap<String, Float> branchDistancePerTrace =new  HashMap<String, Float>();

        static List<String> generalTrace;
        static String generalTraceString;
        static float distanceSumOfTrace = 0;
        static long start = System.currentTimeMillis();
        static long end = start + 60*1000*10; //stop after 10 minutes


        /**
         * Write your solution that specifies what should happen when a new branch has been found.
         */
        static void encounteredNewBranch(MyVar condition, boolean value, int line_nr){
                //System.out.println(currentTraceSymbol);
                if(currentTraceSymbol != "R") {

                        // do something useful
                        //System.out.println("Current Trace Symbol: " + currentTraceSymbol);
                        /**
                         add branches which the symbol covered to their set +
                         add line number to set of visited branches
                         */

                        if (branchesPerTrace.containsKey(generalTraceString)) {
                                branchesPerTrace.get(generalTraceString).add(line_nr);
                        } else {
                                HashSet<Integer> set = new HashSet<>();
                                set.add(line_nr);
                                branchesPerTrace.put(generalTraceString, set);
                        }
                        visitedBranches.add(line_nr);

                        //System.out.println("input trace: " + currentTraceSymbol);
                        //System.out.println("Condition: " + condition.toString());
                        //System.out.println("Value: " + value);
                        //System.out.println("Visited Branch size: " + visitedBranches.size());


                        /**
                         summing up the distances for the trace (will be put to zero when it's empty)
                         */

                        float branchDistance = calculateBranchDistance(condition);
                        distanceSumOfTrace += branchDistance;
                }
        }

        /**
         *Branch Distance calculation for type 1, 4 and 5 MyVar types. Combination and operation logic implemented here in the switch statement
         */
        private static float calculateBranchDistance(MyVar condition) {
                float branch_distance;

                if(condition.type == 1){
                        branch_distance = condition.value ? 0 : 1;
                        return normalize(branch_distance);
                }

                if(condition.type == 5 || condition.type == 4){
                        MyVar left_side = condition.left;
                        MyVar right_side;

                        float left_distance = -1;
                        float right_distance = -1;

                        ArrayList<Integer> string_value_left = new ArrayList<>();
                        ArrayList<Integer> string_value_right = new ArrayList<>();

                        left_distance = distanceHelper(left_side, left_distance, string_value_left);

                        if(condition.type == 5) {
                                right_side = condition.right;
                                right_distance = distanceHelper(right_side, right_distance, string_value_right);
                        }
//                        System.out.println("left distance is : " + left_distance + " right distance is : " +right_distance);
//                        if(!string_value_left.isEmpty() && !string_value_right.isEmpty()) {
//                                String listString_left = string_value_left.stream().map(Object::toString)
//                                        .collect(Collectors.joining(", "));
//                                String listString_right = string_value_right.stream().map(Object::toString)
//                                        .collect(Collectors.joining(", "));
//                                System.out.println("left string is : " + listString_left + " right sting is : " +listString_right);
//                        }
                        switch(condition.operator) {
                                case "||":
                                        branch_distance = Math.min((left_distance), (right_distance));
                                        branch_distance = normalize(branch_distance);
                                        break;
                                case "&&":
                                        branch_distance = (left_distance) + (right_distance);
                                        branch_distance = normalize(branch_distance);
                                        break;
                                case "!":
                                        branch_distance = 1- (left_distance);
                                        branch_distance = normalize(branch_distance);
                                        break;
                                case "<":
                                        branch_distance = ((left_distance < right_distance) ? 0 : normalize(left_distance-right_distance + K));
                                        break;
                                case ">":
                                        branch_distance = ((left_distance > right_distance) ? 0 : normalize(right_distance-left_distance + K));
                                        break;
                                case "<=":
                                        branch_distance = ((left_distance <= right_distance) ? 0 : normalize(left_distance-right_distance));
                                        break;
                                case ">=":
                                        branch_distance = ((left_distance >= right_distance) ? 0 : normalize(right_distance-left_distance));
                                        break;

                                case "==":
                                        if(!string_value_left.isEmpty() && !string_value_right.isEmpty()) {
                                                branch_distance = normalize(calculateStringDistance(string_value_left, string_value_right));
                                        }else{
                                                branch_distance = normalize(Math.abs(left_distance-right_distance));
                                        }
                                        break;

                                case "!=":
                                        if(!string_value_left.isEmpty() && !string_value_right.isEmpty()) {
                                                branch_distance = normalize(!string_value_left.equals(string_value_right) ? 0 : 1);
                                        }else{
                                                branch_distance = normalize(left_distance!=right_distance ? 0 : 1);
                                        }
                                        break;

                                default:
                                        System.err.println("You missed operator "+ condition.operator );
                                        branch_distance= -1;
                        }

                }else{
                        System.err.println("Recursion problem");
                        branch_distance = -1;

                }
//                System.out.println("Subbranch distance of " + condition.toString() + " is found to be " + branch_distance);

                return branch_distance;
        }

        private static float normalize(float i){
                return i/(i+1);
        }

        /**
         *String comparator as given in the lecture slides, modified to work with out string list representations
         */
        private static int calculateStringDistance(ArrayList<Integer> left, ArrayList<Integer> right ){
                int m = left.size();
                int n = right.size();
                int[][] table = new int[m+ 1][n + 1];
                for (int i = 0; i <= m; i++) {
                        for (int j = 0; j <= n; j++) {
                                if (i == 0) {
                                        table[i][j] = j;
                                }
                                else if (j == 0) {
                                        table[i][j] = i;
                                }
                                else if (left.get(i-1) == right.get(j-1)){
                                        table[i][j] = table[i-1][j-1];

                                }else{
                                        table[i][j] = Math.abs(left.get(i-1) - right.get(j-1))+
                                                Math.min(Math.min(table[i][j-1], table[i-1][j]), table[i-1][j-1]);
                                }
                        }
                }

                return table[m][n];


        }
        /**
         *Recursion helper to extract the base case values such as strings, integers and booleans.
         * For type 4 and 5, since they still have MyVar bases inside, calls the calculateBranchDistance function
         */
        private static float distanceHelper(MyVar side, float distance, ArrayList<Integer> string_value ){
                switch (side.type){
                        case 1:
                                //do sth, binary type
                                distance = normalize(side.value ? 0 : 1);
                                break;
                        case 2:
                                //do sth, int type
                                distance = (float) side.int_value;
                                break;
                        case 3:
                                //do sth, string type
                                String s = side.str_value;
                                for(int i = 0; i < s.length(); i++) {
                                        string_value.add((int) s.charAt(i));
                                }
                                distance = -1;
                                break;
                        case 4:
                                //do sth, unary type
                                if(side.operator.equals("!")){
                                        distance = calculateBranchDistance(side);
                                }else{
                                        distance = Integer.parseInt(side.operator + side.left.int_value);
                                }
                                break;
                        case 5:
                                distance = calculateBranchDistance(side);
                                break;
                }
                return distance;
        }

        /**
         * Method for fuzzing new inputs for a program.
         * @param inputSymbols the inputSymbols
         * @return a fuzzed input
         */
        static String fuzz(String[] inputSymbols){
                String nextInput = null;
                // If the current trace does not exist,
                // then generate a random one.
                if (currentTrace == null) {
                        System.out.println("current trace does not exist, Generating a random trace");
                        currentTrace = generateRandomTrace(inputSymbols);
                        generalTrace = new ArrayList<>(currentTrace);
                        generalTraceString = String.join("-", generalTrace);
                        nextInput = currentTrace.remove(0);
                }
                // Check if the current trace is empty and if it is
                // then generate a new random trace.
                else if (currentTrace.isEmpty()) {

                        branchDistancePerTrace.put(generalTraceString, distanceSumOfTrace);
                        List<String> traceMaxBranchCov = getTraceHighestBranchCoverage();
                        List<String> traceLowestDistance = getTraceLowestDistance();
                        System.out.println("Size of visited Branches: " + visitedBranches.size());
                        System.out.println("Trace "+ traceMaxBranchCov + " has max coverage of " + branchesPerTrace.get(traceMaxBranchCov.get(0)).size());
                        System.out.println("Trace "+ traceLowestDistance + " has min distance of " + branchDistancePerTrace.get(traceLowestDistance.get(0)));

                        if(System.currentTimeMillis() > end){
                                System.exit(0);
                        }

                        /**
                         * new round - new trace and distance = 0
                         * We also check that we have a unique trace - if not while loop
                         */

                        distanceSumOfTrace = 0;

                        System.out.println("current trace is empty, Generating a random trace");
                        currentTrace = generateRandomTrace(inputSymbols);

                        while(branchDistancePerTrace.containsKey(String.join("-", currentTrace))){
                                currentTrace = generateRandomTrace(inputSymbols);
                        }

                        generalTrace = new ArrayList<>(currentTrace);
                        generalTraceString = String.join("-", generalTrace);
                        nextInput = currentTrace.remove(0);
                }
                // If we are not done running on the current trace,
                // grab the next input from the current trace.
                else {
                        nextInput = currentTrace.remove(0);
                        //System.out.println("current trace is still going on, grabbing the next input from the current trace: "+ nextInput);
                        currentTraceSymbol = nextInput;
                }

                return nextInput;
        }

        /**
         * Comparison of the symbols in the hashmap with the biggest set
         * @return symbol with biggest set
         */
        static List<String> getTraceHighestBranchCoverage() {
                Map.Entry<String, HashSet<Integer>> max = null;
                for (Map.Entry<String, HashSet<Integer>> entry : branchesPerTrace.entrySet()) {
                        if (max == null || max.getValue().size() < entry.getValue().size()) {
                                max = entry;
                        }
                }
                List<String> max_traces = new ArrayList<>();

                int max_value = max.getValue().size();

                for (Map.Entry<String, HashSet<Integer>> entry : branchesPerTrace.entrySet()) {
                        if (entry.getValue().size() == max_value) {
                                max_traces.add(entry.getKey());
                        }
                }


                return max_traces;
        }
        /**
         * Comparison of the traces in the hashmap with the smallest distance
         * @return trace with smallest distance
         */
        static List<String> getTraceLowestDistance(){
                Map.Entry<String, Float> min = null;
                for (Map.Entry<String, Float> entry : branchDistancePerTrace.entrySet()) {
                        if (min == null || min.getValue() > entry.getValue()) {
                                min = entry;
                        }
                }
                List<String> min_traces = new ArrayList<>();

                float min_value = min.getValue();
                for (Map.Entry<String, Float> entry : branchDistancePerTrace.entrySet()) {
                        if (entry.getValue() == min_value) {
                                min_traces.add(entry.getKey());
                        }
                }

                return min_traces;
        }


        /**
         * Generate a random trace from an array of symbols.
         * @param symbols the symbols from which a trace should be generated from.
         * @return a random trace that is generated from the given symbols.
         */
        static List<String> generateRandomTrace(String[] symbols) {
                ArrayList<String> trace = new ArrayList<>();
                for (int i = 0; i < traceLength; i++) {
                        trace.add(symbols[r.nextInt(symbols.length)]);
                }
                trace.add("R"); // Reset symbol that marks that we have arrived at the end of a trace.
                return trace;
        }

        /**
         * Method that is used for catching the output from standard out.
         * You should write your own logic here.
         * @param out the string that has been outputted in the standard out.
         */
        static void output(String out){
                System.out.println(out);
        }
}

package nl.tudelft.instrumentation.fuzzing;

import java.util.*;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * You should write your own solution using this class.
 */
public class FuzzingLab {
        static Random r = new Random();
        static List<String> currentTrace;
        static int traceLength = 10;
        static String currentTraceSymbol;


        /**
         * Write your solution that specifies what should happen when a new branch has been found.
         */
        static void encounteredNewBranch(MyVar condition, boolean value, int line_nr){
                // do something useful
//                System.out.println("The current trace is "+String.join("-", currentTrace));
                int branchDistance = calculateBranchDistance(condition);
                System.out.println(condition.toString());
//
//                }else {
//                        System.out.println("The left side:  " + condition.left + "The right side was null:  "  +
//                                "operand:  " + condition.operator + " the line number is " + line_nr + " cond type: " + condition.type);
//                }

        }
//        switch(condition.operator) {
//                case "||":
//                        // boolean type
//
//                        break;
//                case "&&":
//                        // int type
//                        break;
//                case "<":
//                        // string type
//                        break;
//                case ">":
//                        // unary type
//                        break;
//                case "<=":
//                        // binary type
//                        break;
//                case ">=":
//                        // binary type
//                        break;
//
//                case "==":
//                        // binary type
//                        break;
//
//                default:
//                        // code block
//        }

        private static int calculateBranchDistance(MyVar condition) {

                if(condition.type != 5){
                        System.err.println("lol u fucked");
                }else{
                        MyVar left_side = condition.left;
                        MyVar right_side = condition.right;
                        int left_distance = -1;
                        int right_distance = -1;
                        ArrayList<Integer> string_value_left = new ArrayList<>();
                        ArrayList<Integer> string_value_right = new ArrayList<>();

                        left_distance = distanceHelper(left_side, left_distance, string_value_left);
                        right_distance = distanceHelper(right_side, right_distance, string_value_right);
                        System.out.println("left distance is : " + left_distance + " right distance is : " +right_distance);
                        if(!string_value_left.isEmpty() || !string_value_right.isEmpty()) {
                                String listString_left = string_value_left.stream().map(Object::toString)
                                        .collect(Collectors.joining(", "));
                                String listString_right = string_value_right.stream().map(Object::toString)
                                        .collect(Collectors.joining(", "));
                                System.out.println("left string is : " + listString_left + " right sting is : " +listString_right);
                        }
                }


                return 0;
        }

        private static int distanceHelper(MyVar side, int distance, ArrayList<Integer> string_value) {
                switch (side.type){
                        case 1:
                                //do sth, binary type
                                distance = side.value ? 0 : 1;
                                break;
                        case 2:
                                //do sth, int type
                                distance = side.int_value;
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
                                        distance = side.left.value ? 1 : 0;
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
                        nextInput = currentTrace.remove(0);
                }
                // Check if the current trace is empty and if it is
                // then generate a new random trace.
                else if (currentTrace.isEmpty()) {
                        System.out.println("current trace is empty, Generating a random trace");
                        currentTrace = generateRandomTrace(inputSymbols);
                        nextInput = currentTrace.remove(0);
                }
                // If we are not done running on the current trace,
                // grab the next input from the current trace.
                else {
                        nextInput = currentTrace.remove(0);
                        System.out.println("current trace is still going on, grabbing the next input from the current trace: "+ nextInput);
                        currentTraceSymbol = nextInput;
                }

                return nextInput;
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

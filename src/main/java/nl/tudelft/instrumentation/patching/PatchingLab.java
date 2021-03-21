package nl.tudelft.instrumentation.patching;
import java.util.*;

public class PatchingLab {

        static Random r = new Random();
        static boolean isFinished = false;
        static ArrayList<HashSet<Integer>> operatorsPerTest = new ArrayList<>();
        static ArrayList<Integer> passCounter = new ArrayList<>(Collections.nCopies(OperatorTracker.operators.length, 0));
        static ArrayList<Integer> failCounter = new ArrayList<>(Collections.nCopies(OperatorTracker.operators.length, 0));
        static ArrayList<Double> tarantulaScore = new ArrayList<>(Collections.nCopies(OperatorTracker.operators.length, 0.0));


        static void initialize(){
                // initialize the population based on OperatorTracker.operators
        }

        // encounteredOperator gets called for each operator encountered while running tests
        static boolean encounteredOperator(String operator, int left, int right, int operator_nr){
                // Do something useful
                if(operatorsPerTest.size() <= OperatorTracker.current_test){
                        HashSet<Integer> temp = new HashSet<>();
                        temp.add(operator_nr);
                        operatorsPerTest.add(OperatorTracker.current_test, temp);
                }else{
                        operatorsPerTest.get(OperatorTracker.current_test)
                                .add(operator_nr);
                }
                String replacement = OperatorTracker.operators[operator_nr];
                if(replacement.equals("!=")) return left != right;
                if(replacement.equals("==")) return left == right;
                if(replacement.equals("<")) return left < right;
                if(replacement.equals(">")) return left > right;
                if(replacement.equals("<=")) return left <= right;
                if(replacement.equals(">=")) return left >= right;
                return false;
        }

        static boolean encounteredOperator(String operator, boolean left, boolean right, int operator_nr){
                // Do something useful
                if(operatorsPerTest.size() <= OperatorTracker.current_test){
                        HashSet<Integer> temp = new HashSet<>();
                        temp.add(operator_nr);
                        operatorsPerTest.add(OperatorTracker.current_test, temp);
                }else{
                        operatorsPerTest.get(OperatorTracker.current_test)
                                .add(operator_nr);
                }

                String replacement = OperatorTracker.operators[operator_nr];
                if(replacement.equals("!=")) return left != right;
                if(replacement.equals("==")) return left == right;
                return false;
        }


        static void calculateTarantula(List<Boolean> testResults){
                int nTests = OperatorTracker.tests.size();
                int nTestsPassed = Collections.frequency(testResults, true);
                int nTestsFailed = Collections.frequency(testResults, false);

                for(int i = 0; i < nTests; i++ ){
                        HashSet<Integer> operator_set = operatorsPerTest.get(i);

                        if(testResults.get(i)){
                                for(Integer operator : operator_set){
                                        int count = passCounter.get(operator) + 1;
                                        passCounter.set(operator, count);
                                }
                        }else{
                                for(Integer operator : operator_set){
                                        int count = failCounter.get(operator) + 1;
                                        failCounter.set(operator, count);
                                }
                        }
                }
                System.out.println("FAIL COUNTER: " +failCounter);
                System.out.println("PASS COUNTER: " +passCounter);

                for(int j = 0; j < passCounter.size(); j++){
                        int failC = failCounter.get(j);
                        int passC = passCounter.get(j);

                        if(nTestsPassed == 0 || nTestsFailed == 0) {
                                tarantulaScore.set(j, 0.0);
                        }
                        else if(passC == 0 && failC == 0){
                                tarantulaScore.set(j,0.0);
                        }
                        else{
                                double failComponent = (double) failC / nTestsFailed;
                                double passComponent = (double) passC / nTestsPassed;
                                double result = failComponent/(failComponent+passComponent);
                                tarantulaScore.set(j, result);
                        }
                }
        }

        static void run() {
                initialize();
                // Place the code here you want to run once:
                // You want to change this of course, this is just an example
                // Tests are loaded from resources/tests.txt, make sure you put in the right tests for the right problem!
                List<Boolean> testResults = OperatorTracker.runAllTests();
                System.out.println(operatorsPerTest);
                int nTests = OperatorTracker.tests.size();
                int nTestsPassed = Collections.frequency(testResults, true);
                int nTestsFailed = Collections.frequency(testResults, false);
                calculateTarantula(testResults);
                System.out.println("Tarantula: " + tarantulaScore);
                System.out.println("Initially, " + nTestsPassed + "/" + nTests + " passed. Fitness: " + (double)nTestsPassed/nTests);
                System.out.println("Entered run");

                // Loop here, running your genetic algorithm until you think it is done
                while (!isFinished) {
                      // Do things!
                        try {
                                System.out.println("Woohoo, looping!");
                                Thread.sleep(1000);
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                }
        }

        public static void output(String out){
                // This will get called when the problem code tries to print things,
                // the prints in the original code have been removed for your convenience

                // System.out.println(out);
        }
}
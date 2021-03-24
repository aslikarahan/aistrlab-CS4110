package nl.tudelft.instrumentation.patching;
import java.util.*;

public class PatchingLab {

        static Random r = new Random();
        static boolean isFinished = false;
        static ArrayList<HashSet<Integer>> operatorsPerTest = new ArrayList<>();
        static ArrayList<Integer> passCounter = new ArrayList<>(Collections.nCopies(OperatorTracker.operators.length, 0));
        static ArrayList<Integer> failCounter = new ArrayList<>(Collections.nCopies(OperatorTracker.operators.length, 0));
        static ArrayList<Double> tarantulaScore = new ArrayList<>(Collections.nCopies(OperatorTracker.operators.length, 0.0));
        //1 is integer operator, 0 is boolean operator
        static ArrayList<Integer> typeDistinguisher = new ArrayList<>(Collections.nCopies(OperatorTracker.operators.length, -1));
        static int populationSize = 30;
        static ArrayList<String[]> population = new ArrayList<String[]>();
        static ArrayList<ArrayList<Double>> mutatedTarantulaScores = new ArrayList<ArrayList<Double>>();
        static ArrayList<Double> mutatedFitnessScores = new ArrayList<Double>();
        static double maxFitness = 0;
        static String[] best_operators;

        static final String[] integerOperators = {"!=", "==", "<", ">", "<=", ">="};
        static final String[] stringOperators = {"!=", "=="};
        private static int mutationRate = 5;

        static void initialize(){
                // initialize the population based on OperatorTracker.operators
                for (int i = 0; i < populationSize; i++) {
                        String[] temp = OperatorTracker.operators.clone();
                        population.add(i, temp);
                        ArrayList<Double> temp_scores = new ArrayList<>(Collections.nCopies(OperatorTracker.operators.length, 0.0));
                        mutatedTarantulaScores.add(i, temp_scores);
                        mutatedFitnessScores.add(i, 0.0);
                }
            best_operators = OperatorTracker.operators.clone();
        }

        // encounteredOperator gets called for each operator encountered while running tests
        static boolean encounteredOperator(String operator, int left, int right, int operator_nr){
                // Do something useful
                typeDistinguisher.set(operator_nr, 1);
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
                typeDistinguisher.set(operator_nr, 0);
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


        static ArrayList<Double> calculateTarantula(List<Boolean> testResults){

                ArrayList<Double> tempScore = new ArrayList<>(Collections.nCopies(OperatorTracker.operators.length, 0.0));

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
                double result;
                for(int j = 0; j < passCounter.size(); j++){
                        int failC = failCounter.get(j);
                        int passC = passCounter.get(j);

                        if(nTestsPassed == 0 || nTestsFailed == 0) {
                                result = 0.0;
                        }
                        else if(passC == 0 && failC == 0){
                                result = 0.0;
                        }
                        else{
                                double failComponent = (double) failC / nTestsFailed;
                                double passComponent = (double) passC / nTestsPassed;
                                result = failComponent/(failComponent+passComponent);
                        }
                        tempScore.set(j, result);

                }
                return tempScore;
        }

        static void createPopulation(String [] winner, ArrayList<Double> winnerTarantulaScore){

                for (int j = 0; j < populationSize; j++) {
                        String[] new_individual = winner.clone();

                        for (int i = 0; i < winnerTarantulaScore.size(); i++) {
                                Double value = winnerTarantulaScore.get(i);
                                Integer operator_type = typeDistinguisher.get(i);
                                if(value > 0.75 && r.nextInt(10)<5){
                                        if(operator_type == 0){
                                                new_individual[i] = stringOperators[r.nextInt(stringOperators.length)];
                                        }else{
                                                new_individual[i] = integerOperators[r.nextInt(integerOperators.length)];
                                        }
                                }
                        }
                        for (int m = 0; m < new_individual.length; m++) {
                                if (r.nextInt(100)<2){
                                        Integer operator_type_mutation = typeDistinguisher.get(m);
                                        if(operator_type_mutation == 0){
                                                new_individual[m] = stringOperators[r.nextInt(stringOperators.length)];
                                        }else{
                                                new_individual[m] = integerOperators[r.nextInt(integerOperators.length)];
                                        }
                                }
                        }
                        population.set(j, new_individual);
                }

        }
        private static void createPopulation(String[] parent1, String[] parent2, ArrayList<Double> tarantulaScore1, ArrayList<Double> tarantulaScore2, double fitness1, double fitness2) {

                for (int j = 0; j < populationSize; j++) {
                        String[] p1 = parent1.clone();
                        String[] p2 = parent2.clone();
                        for (int i = 0; i < tarantulaScore1.size(); i++) {
                                Double value_1 = tarantulaScore1.get(i);
                                Double value_2 = tarantulaScore2.get(i);
                                Integer operator_type = typeDistinguisher.get(i);
                                if(value_1 > 0.8 && r.nextInt(10)>mutationRate){
                                        if(operator_type == 0){
                                                p1[i] = stringOperators[r.nextInt(stringOperators.length)];
                                        }else{
                                                p1[i] = integerOperators[r.nextInt(integerOperators.length)];
                                        }
                                }
                                if(value_2 > 0.8 && r.nextInt(10)>mutationRate){
                                        if(operator_type == 0){
                                                p2[i] = stringOperators[r.nextInt(stringOperators.length)];
                                        }else{
                                                p2[i] = integerOperators[r.nextInt(integerOperators.length)];
                                        }
                                }
                        }

                        int crossover_index = r.nextInt(p1.length);
                        String[] new_individual = p1.clone();
                        for (int m = crossover_index; m < p1.length ; m++) {
                                new_individual[m] = p2[m];
                        }

                        for (int m = 0; m < new_individual.length; m++) {
                                if (r.nextInt(100)<2){
                                        Integer operator_type_mutation = typeDistinguisher.get(m);
                                        if(operator_type_mutation == 0){
                                                new_individual[m] = stringOperators[r.nextInt(stringOperators.length)];
                                        }else{
                                                new_individual[m] = integerOperators[r.nextInt(integerOperators.length)];
                                        }
                                }
                        }

                        population.set(j, new_individual);
                }
        }

        static void run() {
                initialize();
                // Place the code here you want to run :
                List<Boolean> testResults = OperatorTracker.runAllTests();

                int nTests = OperatorTracker.tests.size();
                int nTestsPassed = Collections.frequency(testResults, true);
                int nTestsFailed = Collections.frequency(testResults, false);
                tarantulaScore = calculateTarantula(testResults);

                createPopulation(OperatorTracker.operators, tarantulaScore);

                operatorsPerTest = new ArrayList<>();
                failCounter = new ArrayList<>(Collections.nCopies(OperatorTracker.operators.length, 0));
                passCounter = new ArrayList<>(Collections.nCopies(OperatorTracker.operators.length, 0));

                System.out.println("Initially, " + nTestsPassed + "/" + nTests + " passed. Fitness: " + (double)nTestsPassed/nTests);

                // Loop here, running your genetic algorithm until you think it is done
                while (!isFinished) {
                      // Do things!
                        try {
                                for (int i = 0; i < populationSize; i++){
                                        OperatorTracker.operators = population.get(i).clone();
                                        testResults = OperatorTracker.runAllTests();
                                        nTests = OperatorTracker.tests.size();
                                        nTestsPassed = Collections.frequency(testResults, true);
                                        nTestsFailed = Collections.frequency(testResults, false);
                                        ArrayList<Double> tarantula_of_individual = calculateTarantula(testResults);
                                        mutatedTarantulaScores.set(i,tarantula_of_individual);
                                        mutatedFitnessScores.set(i, (double)nTestsPassed/nTests);
                                        passCounter = new ArrayList<>(Collections.nCopies(OperatorTracker.operators.length, 0));
                                        failCounter = new ArrayList<>(Collections.nCopies(OperatorTracker.operators.length, 0));
                                        operatorsPerTest = new ArrayList<>();
                                        System.out.println("Individual "+ i+" " + nTestsPassed + "/" + nTests + " passed. Fitness: " + (double)nTestsPassed/nTests);
                                }

                                int winner = selectWinner();
                                int[] winners = selectWinners();
                                if(Collections.max(mutatedFitnessScores)>maxFitness) {
                                    maxFitness = Collections.max(mutatedFitnessScores);
                                    best_operators = population.get(winner).clone();
                                    System.out.println("The current best operators are updated and as follows: ");
                                    System.out.println(Arrays.toString(best_operators));
                                }
                                createPopulation(population.get(winners[0]), population.get(winners[1]), mutatedTarantulaScores.get(winners[0]), mutatedTarantulaScores.get(winners[1]),
                                        mutatedFitnessScores.get(winners[0]),  mutatedFitnessScores.get(winners[1]));
                                System.out.println("Woohoo, looping! The best is "+maxFitness);
                                Thread.sleep(1000);
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                }
        }

        private static int selectWinner() {
                Double max = Collections.max(mutatedFitnessScores);
                return mutatedFitnessScores.indexOf(max);
        }

        private static int[] selectWinners() {
                ArrayList<Double>  local_temp = new ArrayList<>();
                local_temp.addAll(mutatedFitnessScores);
                Collections.shuffle(local_temp);
                Double max_parent_1 = Collections.max(local_temp.subList(0, (int) local_temp.size()/2));
                Double max_parent_2 = Collections.max(local_temp.subList((int) local_temp.size()/2, local_temp.size()));
                int[] parents = {mutatedFitnessScores.indexOf(max_parent_1), mutatedFitnessScores.indexOf(max_parent_2)};
                return parents;
        }

        public static void output(String out){
                // This will get called when the problem code tries to print things,
                // the prints in the original code have been removed for your convenience

                // System.out.println(out);
        }
}
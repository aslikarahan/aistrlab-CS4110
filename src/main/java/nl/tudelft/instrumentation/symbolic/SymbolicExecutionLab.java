package nl.tudelft.instrumentation.symbolic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

import com.microsoft.z3.*;

import java.util.Random;

/**
 * You should write your solution using this class.
 */
public class SymbolicExecutionLab {

    static Random r = new Random();
    public static LinkedList<LinkedList<String>> inputs_to_fuzz = new LinkedList<LinkedList<String>>();
    static HashSet<Integer> branchCoverage = new HashSet<>();
    static HashSet<String> errorCodes = new HashSet<>();

    static LinkedList<String> currentTrace = new LinkedList<String>();
    static int traceLength = 26;

    static long start = System.currentTimeMillis();
    static long end = start + 60*1000*15; //stop after 10 minutes

    static StringBuilder sbInformation = new StringBuilder();
    static boolean firstInit = true;


    static MyVar createVar(String name, Expr value, Sort s) {
        Context c = PathTracker.ctx;
        Expr z3var = c.mkConst(c.mkSymbol(name + "_" + PathTracker.z3counter++), s);
        PathTracker.z3model = c.mkAnd(c.mkEq(z3var, value), PathTracker.z3model);
        return new MyVar(z3var, name);
    }

    static MyVar createInput(String name, Expr value, Sort s) {
        Context c = PathTracker.ctx;
        Expr z3var = c.mkConst(c.mkSymbol(name + "_" + PathTracker.z3counter++), s);
        MyVar new_input = new MyVar(z3var, name);
        PathTracker.inputs.add(new_input);
        return new_input;
    }

    static MyVar createBoolExpr(BoolExpr var, String operator) {
        Context c = PathTracker.ctx;
        Expr z3var;
        switch (operator) {
            case "!":
                z3var = c.mkNot(var);
                break;
            default:
                z3var = c.mkFalse();
                break;
        }

        MyVar result = new MyVar(z3var);
        return result;
    }

    static MyVar createBoolExpr(BoolExpr left_var, BoolExpr right_var, String operator) {
        Context c = PathTracker.ctx;
        Expr z3var;
        switch (operator) {
            case "||":
            case "|":
                z3var = c.mkOr(left_var, right_var);
                break;
            case "&&":
            case "&":
                z3var = c.mkAnd(left_var, right_var);
                break;
            default:
                z3var =  c.mkFalse();
                break;
        }
        MyVar result = new MyVar(z3var);
        return result;
    }

    static MyVar createIntExpr(IntExpr var, String operator) {
        Context c = PathTracker.ctx;
        Expr z3var;
        switch (operator) {
            case "-":
//                z3var = c.mkMul(var, c.mkInt("-1"));
                z3var = c.mkUnaryMinus(var);
                break;
            default:
                z3var = var;
                break;
        }
        MyVar result = new MyVar(z3var);
        return result;
    }

    static MyVar createIntExpr(IntExpr left_var, IntExpr right_var, String operator) {
        Context c = PathTracker.ctx;
        Expr z3var;
        switch (operator) {
            case "+":
                z3var = c.mkAdd(left_var, right_var);
                break;
            case "-":
                z3var = c.mkSub(left_var, right_var);
                break;
            case "/":
                z3var = c.mkDiv(left_var, right_var);
                break;
            case "*":
                z3var = c.mkMul(left_var, right_var);
                break;
            case "%":
                z3var = c.mkRem(left_var, right_var);
                break;
            case "^":
                z3var = c.mkRem(left_var, right_var);
                break;
            case "==":
                z3var = c.mkEq(left_var, right_var);
                break;
            default:
                z3var = PathTracker.ctx.mkFalse();
                break;
        }
        MyVar result = new MyVar(z3var);
        return result;
    }

    static MyVar createStringExpr(SeqExpr left_var, SeqExpr right_var, String operator) {
        Context c = PathTracker.ctx;
        Expr z3var = c.mkEq(left_var, right_var);
        return new MyVar(z3var);
    }

    static void assign(MyVar var, String name, Expr value, Sort s) {
        Context c = PathTracker.ctx;
        var.z3var = c.mkConst(c.mkSymbol(name + "_" + PathTracker.z3counter++), s);
        PathTracker.z3model = c.mkAnd(c.mkEq(var.z3var, value), PathTracker.z3model);
    }

    /*
    If the branch has not been covered yet - then we want to solve the opposite value and will put
    the solution (the input traces to reach the opposite value branch) into the global list.
    When seeing a branch, we will add it to z3branches.
     */
    static void encounteredNewBranch(MyVar condition, boolean value, int line_nr) {
        Context c = PathTracker.ctx;
        if (value) {
            if(!branchCoverage.contains(line_nr)) {
//                if (condition.z3var.toString().contains("input"))
                    PathTracker.solve(c.mkEq(condition.z3var, c.mkFalse()), false);
            }
            PathTracker.z3branches = c.mkAnd(c.mkEq(condition.z3var, c.mkTrue()), PathTracker.z3branches);
        } else {
            if(!branchCoverage.contains(line_nr)) {
//                if (condition.z3var.toString().contains("input"))
                    PathTracker.solve(c.mkEq(condition.z3var, c.mkTrue()), false);
            }
            PathTracker.z3branches = c.mkAnd(c.mkEq(condition.z3var, c.mkFalse()), PathTracker.z3branches);
        }
        branchCoverage.add(line_nr);
    }

    static void newSatisfiableInput(LinkedList<String> new_inputs) {
        LinkedList<String> tmp = new LinkedList<>();
        for(String input : new_inputs){
                tmp.add(input.substring(1, input.length() - 1));
        }
        inputs_to_fuzz.add(tmp);
    }

    /*
    We create a random new trace
     */
    static void createTrace(String[] inputs){
        for(int i = 0; i < traceLength-1; i++){
            currentTrace.add(inputs[r.nextInt(inputs.length)]);
        }
        currentTrace.add("#");
    }

    /*
    We extend the trace to the length of the tracelength
     */
    static void extendTrace(String[] inputs){
        for(int i = currentTrace.size(); i < traceLength-1; i++){
            currentTrace.add(inputs[r.nextInt(inputs.length)]);
        }
        currentTrace.add("#");
    }

    static String fuzz(String[] inputs) {
        /*
        Some initialization for the creation of CSV
         */
        String nextInput = null;
        if (firstInit) {
            sbInformation.append("Time");
            sbInformation.append(',');
            sbInformation.append("AmountOfErrors");
            sbInformation.append('\n');
            firstInit = false;
        }
        System.out.println("List to fuzz: " + inputs_to_fuzz);
        System.out.println("Current Trace: " + currentTrace);
        System.out.println("The branch coverage size for the previous input is: " + branchCoverage.size());
        System.out.println("Error Codes: " + errorCodes);

        /*
        If the time is over - we want to save the information in a CSV.
         */
        if (System.currentTimeMillis() > end) {
            PrintWriter graph = null;
            LocalDateTime now = LocalDateTime.now();
            String hour = String.valueOf(now.getHour());
            String minute = String.valueOf(now.getMinute());
            String sec = String.valueOf(now.getSecond());
            try {
                graph = new PrintWriter(new File("CSV_Symbolic/graph"+hour+minute+sec+"-"+branchCoverage.size()+".csv"));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            graph.write(sbInformation.toString());
            graph.flush();
            graph.close();
            System.exit(0);
        }

        /*
        Here we go!!
         */
        System.out.println("--------------------------- F U Z Z  --------------------------- ");
        String next_input;

        /*
        Check if trace is not empty - if not empty, then take the next element from trace.
        If the input is an empty string ("") - then we want to use only viable inputs.
        If the input is the reset symbol (#) - then we want to reset.
         */
        if(!currentTrace.isEmpty()){
            next_input = currentTrace.pop();
            if(next_input.isEmpty()){
                next_input= inputs[r.nextInt(inputs.length)];
            }
            else if(next_input.equals("#")){
                PathTracker.reset();
                System.out.println("RESET");
            }
        }
         /*
        If the trace is empty - we want to reset.
        If the global list is empty - we want to create a random input trace.
        If there are other input traces - we choose a random input trace to avoid bias.
         */
        else{
            PathTracker.reset();
            System.out.println("RESET");
            if(inputs_to_fuzz.isEmpty()){
                createTrace(inputs);
                System.out.println("RANDOM RANDOM RANDOM RANDOM RANDOM RANDOM RANDOM RANDOM RANDOM RANDOM RANDOM RANDOM ");
            }else{
                int randomInt = new Random().nextInt(inputs_to_fuzz.size());
                currentTrace = inputs_to_fuzz.get(randomInt);
                inputs_to_fuzz.remove(randomInt);
                extendTrace(inputs);
                System.out.println("Extend new Input");

            }
            next_input = currentTrace.pop();
        }
        return next_input;
    }

    static void output(String out) {
//        System.out.println(out);
    }

    public static void printError(String s) {
        System.out.println("Error: "+ s);
        if(!errorCodes.contains(s)) {
            errorCodes.add(s);
            sbInformation.append((System.currentTimeMillis() - start));
            sbInformation.append(',');
            sbInformation.append(errorCodes.size());
            sbInformation.append('\n');
        }
    }
}
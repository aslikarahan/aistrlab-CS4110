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

    static LinkedList<String> currentTrace;
    static int traceLength = 20;

    static long start = System.currentTimeMillis();
    static long end = start + 60*1000/6; //stop after 10 minutes

    static StringBuilder sbInformation = new StringBuilder();


    static MyVar createVar(String name, Expr value, Sort s) {
        Context c = PathTracker.ctx;
        // create var, assign value, add to path constraint
        // we show how to do it for creating new symbols
        // please add similar steps to the functions below in order to obtain a path constraint
        Expr z3var = c.mkConst(c.mkSymbol(name + "_" + PathTracker.z3counter++), s);
        PathTracker.z3model = c.mkAnd(c.mkEq(z3var, value), PathTracker.z3model);
        return new MyVar(z3var, name);
    }

    static MyVar createInput(String name, Expr value, Sort s) {
        // create an input var, these should be free variables!

        Context c = PathTracker.ctx;
        Expr z3var = c.mkConst(c.mkSymbol(name + "_" + PathTracker.z3counter++), s);
        MyVar new_input = new MyVar(z3var, name);
        PathTracker.inputs.add(new_input);
        return new_input;

//        OLD CODE
//        MyVar new_input = new MyVar(value, name);
//        PathTracker.inputs.add(new_input);
//        System.out.println("In createInput: " + new_input.z3var + " - " + new_input.name);
//        return new_input;
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
        // any binary expression (&, &&, |, ||)
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
        // any unary expression (+, -)
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
//        PathTracker.z3model = c.mkAnd(c.mkEq(var, z3var), PathTracker.z3model);
        MyVar result = new MyVar(z3var);
        return result;
    }

    static MyVar createIntExpr(IntExpr left_var, IntExpr right_var, String operator) {
        // any binary expression (+, -, /, etc)
//        if(operator == "+" || operator == "-" || operator == "/" || operator == "*" || operator == "%" || operator == "^")
//            return new MyVar(PathTracker.ctx.mkInt(0));
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
                //TODO: idk
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
        // all variable assignments, use single static assignment
        Context c = PathTracker.ctx;
        var.z3var = c.mkConst(c.mkSymbol(name + "_" + PathTracker.z3counter++), s);
        PathTracker.z3model = c.mkAnd(c.mkEq(var.z3var, value), PathTracker.z3model);
    }


    static void encounteredNewBranch(MyVar condition, boolean value, int line_nr) {
//        Context c = PathTracker.ctx;
//        if (value) {
//            if (condition.z3var.toString().contains("input"))
//                PathTracker.solve(c.mkEq(condition.z3var, c.mkFalse()), false);
//            PathTracker.z3branches = c.mkAnd(c.mkEq(condition.z3var, c.mkTrue()), PathTracker.z3branches);
//        } else {
//            if (condition.z3var.toString().contains("input"))
//                PathTracker.solve(c.mkEq(condition.z3var, c.mkTrue()), false);
//            PathTracker.z3branches = c.mkAnd(c.mkEq(condition.z3var, c.mkFalse()), PathTracker.z3branches);
//        }
//        branchCoverage.add(line_nr);


        Context c = PathTracker.ctx;
        if (value) {
            if(!branchCoverage.contains(line_nr)) {
                if (condition.z3var.toString().contains("input"))
                    PathTracker.solve(c.mkEq(condition.z3var, c.mkFalse()), false);
            }
            PathTracker.z3branches = c.mkAnd(c.mkEq(condition.z3var, c.mkTrue()), PathTracker.z3branches);
        } else {
            if(!branchCoverage.contains(line_nr)) {
                if (condition.z3var.toString().contains("input"))
                    PathTracker.solve(c.mkEq(condition.z3var, c.mkTrue()), false);
            }
            PathTracker.z3branches = c.mkAnd(c.mkEq(condition.z3var, c.mkFalse()), PathTracker.z3branches);
        }
        branchCoverage.add(line_nr);
    }

    static void newSatisfiableInput(LinkedList<String> new_inputs) {
        //System.out.println("SATISFIABLE with model: "+ PathTracker.z3model);
        //System.out.println("SATISFIABLE with branches: "+ PathTracker.z3branches);
//        inputs_to_fuzz.add("#");
//        System.out.println("New inputs: " + new_inputs);
//        for(String input : new_inputs){
//                inputs_to_fuzz.add(input.substring(1, input.length() - 1));
//        }
        //inputs_to_fuzz.add(new_inputs);
        LinkedList<String> tmp = new LinkedList<>();
        for(String input : new_inputs){
                tmp.add(input.substring(1, input.length() - 1));
        }
        inputs_to_fuzz.add(tmp);
    }

    static void createTrace(String[] inputs){
        for(int i = 0; i < traceLength-1; i++){
            currentTrace.add(inputs[r.nextInt(inputs.length)]);
        }
        currentTrace.add("#");
    }

    static void extendTrace(String[] inputs){
        for(int i = currentTrace.size(); i < traceLength-1; i++){
            currentTrace.add(inputs[r.nextInt(inputs.length)]);
        }
        currentTrace.add("#");
    }

    static String fuzz(String[] inputs) {
        String nextInput = null;
        if (currentTrace == null) {
            sbInformation.append("Time");
            sbInformation.append(',');
            sbInformation.append("Errorcode");
            sbInformation.append('\n');
            currentTrace = new LinkedList<String>();
        }

        //System.out.println("List to fuzz: " + inputs_to_fuzz);
        System.out.println("Current Trace: " + currentTrace);
        System.out.println("The branch coverage size for the previous input is: " + branchCoverage.size());
        System.out.println("Error Codes: " + errorCodes);
        if (System.currentTimeMillis() > end) {
            PrintWriter graph = null;
            LocalDateTime now = LocalDateTime.now();
            String hour = String.valueOf(now.getHour());
            String minute = String.valueOf(now.getMinute());
            try {
                graph = new PrintWriter(new File("CSV_Symbolic/graph"+hour+minute+"-"+branchCoverage.size()+".csv"));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            graph.write(sbInformation.toString());
            graph.flush();
            graph.close();
            System.exit(0);
        }
        System.out.println("--------------------------- F U Z Z  --------------------------- ");
        String next_input;


        if(!currentTrace.isEmpty()){
            next_input = currentTrace.pop();
            if(next_input.isEmpty()){
                next_input= inputs[r.nextInt(inputs.length)];
            }
            else if(next_input.equals("#")){
                PathTracker.reset();
                System.out.println("RESET");
            }
            //Trace is only empty after popping if the last sign was "#"
        }
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

//    static String fuzz_old(String[] inputs) {
//        System.out.println("List to fuzz: " + inputs_to_fuzz);
//        System.out.println("The branch coverage size for the previous input is: " + branchCoverage.size());
//        System.out.println("Error Codes: " + errorCodes);
//        if (System.currentTimeMillis() > end) {
//            System.exit(0);
//        }
//        System.out.println("--------------------------- F U Z Z  --------------------------- ");
//        String next_input;
//
//        if(inputs_to_fuzz.isEmpty()) {
//            if (r.nextDouble() < 0.01) {
//                PathTracker.reset();
//                return "#";
//            }
//            next_input= inputs[r.nextInt(inputs.length)];
//            System.out.println("RANDOM INPUT: " + next_input);
//            return next_input;
//        }else{
//            next_input = inputs_to_fuzz.pop();
//            if(next_input.equals("#")){
//                PathTracker.reset();
//                System.out.println("RESET");
//            }else if(next_input.isEmpty()){
//                next_input= inputs[r.nextInt(inputs.length)];
//                System.out.println("RANDOM INPUT: " + next_input);
//                return next_input;
//            }
//            System.out.println("NOT random input: " + next_input);
//            return next_input;
//        }
//    }

    static void output(String out) {
//        System.out.println(out);
    }

    public static void printError(String s) {
        System.out.println("Error: "+ s);
        if(!errorCodes.contains(s)) {
            errorCodes.add(s);
            sbInformation.append((System.currentTimeMillis() - start));
            sbInformation.append(',');
            sbInformation.append(s);
            sbInformation.append('\n');
        }
    }
}
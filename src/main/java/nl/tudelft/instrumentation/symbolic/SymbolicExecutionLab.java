package nl.tudelft.instrumentation.symbolic;

import java.util.*;

import com.microsoft.z3.*;

import java.util.Random;

/**
 * You should write your solution using this class.
 */
public class SymbolicExecutionLab {

    static Random r = new Random();
    public static LinkedList<String> inputs_to_fuzz = new LinkedList<String>();
    static HashSet<Integer> branchCoverage = new HashSet<>();
    static boolean unsat = false;

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

//        PathTracker.z3model = c.mkAnd(c.mkEq(var, z3var), PathTracker.z3model);
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
//        PathTracker.z3model = c.mkAnd(c.mkEq(z3var, z3var), PathTracker.z3model);
        return result;
    }

    static MyVar createIntExpr(IntExpr var, String operator) {
        // any unary expression (+, -)
        Context c = PathTracker.ctx;
        Expr z3var;
        switch (operator) {
            case "-":
                z3var = c.mkMul(var, c.mkInt("-1"));
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

        //If false call the the solver to see if we can get an input, if unsatisfiable ?
        //if true just add stuff to z3branches!
        System.out.println("EncounteredNewBranch: " + condition.z3var);
        System.out.println("Value: " + value + " - " + line_nr);

        Context c = PathTracker.ctx;
        branchCoverage.add(line_nr);
        if (value) {
            PathTracker.solve(c.mkEq(condition.z3var, c.mkFalse()), false);
            PathTracker.z3branches = c.mkAnd(c.mkEq(condition.z3var, c.mkTrue()), PathTracker.z3branches);
        } else {
            PathTracker.solve(c.mkEq(condition.z3var, c.mkTrue()), false);
            PathTracker.z3branches = c.mkAnd(c.mkEq(condition.z3var, c.mkFalse()), PathTracker.z3branches);

        }
    }

    static void newSatisfiableInput(LinkedList<String> new_inputs) {
        //System.out.println("SATISFIABLE with model: "+ PathTracker.z3model);
        //System.out.println("SATISFIABLE with branches: "+ PathTracker.z3branches);
        System.out.println("New inputs: " + new_inputs);
        for(String input : new_inputs){
            if(input.equals("\"#\"")){
                unsat = true;
            }else {
                inputs_to_fuzz.add(input.substring(1, input.length() - 1));
            }
        }
    }

    static String fuzz(String[] inputs) {
        if(!inputs_to_fuzz.isEmpty())
            inputs_to_fuzz.add("#"); // add to end of trace except for in the beginning
        System.out.println("List to fuzz: " + inputs_to_fuzz);
        System.out.println("The branch coverage size for the previous input is: " + branchCoverage.size());


        System.out.println("--------------------------- F U Z Z  --------------------------- ");
        String next_input;

        if(inputs_to_fuzz.isEmpty()) {
            if (r.nextDouble() < 0.01) return "#";
            next_input= inputs[r.nextInt(inputs.length)];
        }else{
            next_input = inputs_to_fuzz.pop();
            while(next_input.equals("#")){
                PathTracker.reset();
                if(inputs_to_fuzz.isEmpty()){
                    next_input= inputs[r.nextInt(inputs.length)];
                }else{
                    next_input = inputs_to_fuzz.pop();

                }
            }
        }

        if(next_input.equals("\"\"")){
            next_input= inputs[r.nextInt(inputs.length)];
        }

        return next_input;
    }

    static void output(String out) {
//        System.out.println(out);
    }

    public static void printError(String s) {
        System.out.println("Error: "+ s);
    }
}
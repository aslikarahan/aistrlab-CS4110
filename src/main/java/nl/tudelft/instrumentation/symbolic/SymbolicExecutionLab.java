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
        MyVar new_input = new MyVar(value, name);
        PathTracker.inputs.add(new_input);
        return new_input;
    }

    static MyVar createBoolExpr(BoolExpr var, String operator) {

        Context c = PathTracker.ctx;
        MyVar result;
        switch (operator) {
            case "!":
                result = new MyVar(c.mkNot(var));
                break;
            default:
                result = new MyVar(PathTracker.ctx.mkFalse());
                break;
        }
        return result;
    }

    static MyVar createBoolExpr(BoolExpr left_var, BoolExpr right_var, String operator) {

        // any binary expression (&, &&, |, ||)
        Context c = PathTracker.ctx;
        MyVar result;
        switch (operator) {
            case "||":
                result = new MyVar(c.mkOr(left_var, right_var));
                break;
            case "&&":
                result = new MyVar(c.mkAnd(left_var, right_var));
                break;
            case "|":
                result = new MyVar(c.mkOr(left_var, right_var));
                break;
            case "&":
                result = new MyVar(c.mkAnd(left_var, right_var));
                break;
            default:
                result = new MyVar(PathTracker.ctx.mkFalse());
                break;
        }
        return result;
    }

    static MyVar createIntExpr(IntExpr var, String operator) {
        // any unary expression (+, -)
        Context c = PathTracker.ctx;
        MyVar result;
        switch (operator) {
            case "-":
                result = new MyVar(c.mkMul(var, c.mkInt("-1")));
                break;
            default:
                result = new MyVar(var);
                break;
        }
        return result;
    }

    static MyVar createIntExpr(IntExpr left_var, IntExpr right_var, String operator) {
        // any binary expression (+, -, /, etc)
//        if(operator == "+" || operator == "-" || operator == "/" || operator == "*" || operator == "%" || operator == "^")
//            return new MyVar(PathTracker.ctx.mkInt(0));
        Context c = PathTracker.ctx;
        MyVar result;
        switch (operator) {
            case "+":
                result = new MyVar(c.mkAdd(left_var, right_var));
                break;
            case "-":
                result = new MyVar(c.mkSub(left_var, right_var));
                break;
            case "/":
                result = new MyVar(c.mkDiv(left_var, right_var));
                break;
            case "*":
                result = new MyVar(c.mkMul(left_var, right_var));
                break;
            case "%":
                result = new MyVar(c.mkRem(left_var, right_var));
                break;
            case "^":
                //TODO: idk exponent
                result = new MyVar(c.mkRem(left_var, right_var));
                break;
            case "==":
                result = new MyVar(c.mkEq(left_var, right_var));
                break;
            default:
                result = new MyVar(PathTracker.ctx.mkFalse());
                break;
        }
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
        //generic line counter li ne covarage hash set stuff
//        System.out.println("The branch: " + condition.z3var);
//        System.out.println("Value: " + value +"__"+line_nr);
//        System.out.println("The model: ");
//                System.out.println(PathTracker.z3model);
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
        // hurray! found a new branch using these new inputs!
        //  resety and run again with the new input
//        if(!new_inputs.isEmpty())
//            inputs_to_fuzz.addAll(new_inputs);
        for(String input : new_inputs){
            inputs_to_fuzz.add(input);
        }
        System.out.println("Satisfiable + all inputs: " + inputs_to_fuzz);
    }

    static String fuzz(String[] inputs) {

        PathTracker.reset();

        System.out.println("The branch coverage size for the previous input is: " + branchCoverage.size());
        String next_input;
//        System.out.println("Inputs to fuzz list is  "+ inputs_to_fuzz);
//        if(inputs_to_fuzz.isEmpty()) {
//            if (r.nextDouble() < 0.01) return "R";
//            next_input= inputs[r.nextInt(inputs.length)];
//        }else{
//            next_input = inputs_to_fuzz.pop();
//            System.out.println("The next input is: " + next_input);
//        }
        if (r.nextDouble() < 0.01) {
            System.out.println("------------------------------------------------------------------------------");
            return "R";
        }
        next_input = inputs[r.nextInt(inputs.length)];
//        PathTracker.reset();

        System.out.println("The random input is: " + next_input);
        return next_input;
    }

    static void output(String out) {
//        System.out.println(out);
    }

    public static void printError(String s) {
        System.out.println("Error: "+ s);

    }
}
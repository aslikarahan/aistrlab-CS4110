package nl.tudelft.instrumentation.symbolic;

import java.util.*;
import com.microsoft.z3.*;

import javax.sound.midi.Soundbank;
import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;

/**
 * You should write your solution using this class.
 */
public class SymbolicExecutionLab {

    static Random r = new Random();

    static MyVar createVar(String name, Expr value, Sort s){
        Context c = PathTracker.ctx;
        // create var, assign value, add to path constraint
        // we show how to do it for creating new symbols
        // please add similar steps to the functions below in order to obtain a path constraint
        Expr z3var = c.mkConst(c.mkSymbol(name + "_" + PathTracker.z3counter++), s);
        PathTracker.z3model = c.mkAnd(c.mkEq(z3var, value), PathTracker.z3model);
        return new MyVar(z3var, name);
    }

    static MyVar createInput(String name, Expr value, Sort s){
        // create an input var, these should be free variables!
        return new MyVar(PathTracker.ctx.mkString(""));
    }

    static MyVar createBoolExpr(BoolExpr var, String operator){

        Context c = PathTracker.ctx;
        MyVar result;
        switch(operator) {
            case "!":
                result = new MyVar(c.mkNot(var));
                break;
            default:
                result= new MyVar(PathTracker.ctx.mkFalse());
                break;
        }
        return result;
    }

    static MyVar createBoolExpr(BoolExpr left_var, BoolExpr right_var, String operator){

        // any binary expression (&, &&, |, ||)
        Context c = PathTracker.ctx;
        MyVar result;
        switch(operator) {
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
                result= new MyVar(PathTracker.ctx.mkFalse());
                break;
        }
        return result;
        }

    static MyVar createIntExpr(IntExpr var, String operator){
        // any unary expression (+, -)
        Context c = PathTracker.ctx;
        MyVar result;
        switch(operator) {
            case "-":
                result = new MyVar(c.mkMul(var, c.mkInt("-1")));
                break;
            default:
                result= new MyVar(var);
                break;
        }
        return result;
    }

    static MyVar createIntExpr(IntExpr left_var, IntExpr right_var, String operator){
        // any binary expression (+, -, /, etc)
//        if(operator == "+" || operator == "-" || operator == "/" || operator == "*" || operator == "%" || operator == "^")
//            return new MyVar(PathTracker.ctx.mkInt(0));
        Context c = PathTracker.ctx;
        MyVar result;
        switch(operator) {
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
                //        System.out.println("Left is " + left_var);
//        System.out.println("Right is " + right_var);
//        System.out.println("Operator  " + operator);
//        System.out.println("what we return " + z3var);
                result = new MyVar(c.mkEq(left_var, right_var));
                break;
            default:
                result= new MyVar(PathTracker.ctx.mkFalse());
                break;
        }
        return result;
    }

    static MyVar createStringExpr(SeqExpr left_var, SeqExpr right_var, String operator){
        Context c = PathTracker.ctx;
        Expr z3var = c.mkEq(left_var, right_var);
//        System.out.println("Left is " + left_var);
//        System.out.println("Right is " + right_var);
//        System.out.println("Operator  " + operator);
//        System.out.println("what we return " + z3var);
        return new MyVar(z3var);
    }

    static void assign(MyVar var, String name, Expr value, Sort s){
        // all variable assignments, use single static assignment
//        System.out.println("Assignment var: "+var.z3var);
//        System.out.println("Assignment name: "+var.name);
//        System.out.println("Argument name: "+name);
//        System.out.println("what to assign: "+value);
        Context c = PathTracker.ctx;

        Expr z3var = c.mkConst(c.mkSymbol(name + "_" + PathTracker.z3counter++), s);
        PathTracker.z3model = c.mkAnd(c.mkEq(z3var, value), PathTracker.z3model);


    }
    static HashSet <Integer> branchCoverage = new HashSet<>();
    static void encounteredNewBranch(MyVar condition, boolean value, int line_nr){

        // call the solver

        //If false call the the solver to see if we can get an input, if unsatisfiable ?
        //if true just add stuff to z3branches!
        //generic line counter line covarage hash set stuff
        branchCoverage.add(line_nr);
        System.out.println("Condition expression: "+condition.z3var);

        if(value){
//            System.out.println("Condition name: "+condition.name);
//            System.out.println("Value: "+ value);
            Context c = PathTracker.ctx;
//            System.out.print("Model: ");
//            System.out.println(PathTracker.z3model);
            PathTracker.z3branches = c.mkAnd((BoolExpr) condition.z3var, PathTracker.z3branches);
        }else{
            PathTracker.solve((BoolExpr) condition.z3var, true);
        }

//        if(line_nr == 74){
//            System.out.println(deneme);
//            System.exit(1);
//        }
//        System.out.println("Name is: " + condition.name);


    }

    static void newSatisfiableInput(LinkedList<String> new_inputs) {
        // hurray! found a new branch using these new inputs!
        //  resety and run again with the new input
        System.out.println("The new inputs that satisfy are: " + new_inputs);

    }

    static String fuzz(String[] inputs){
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("The branch coverage size for the previous input is: " + branchCoverage.size());
        PathTracker.reset();
        if(r.nextDouble() < 0.01) return "R";
        String charlie = inputs[r.nextInt(inputs.length)];
        System.out.println("The fucking input is: " + charlie);
        return charlie;
    }

    static void output(String out){
//        System.out.println(out);
    }

}
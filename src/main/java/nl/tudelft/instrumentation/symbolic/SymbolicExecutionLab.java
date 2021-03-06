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
//        System.out.println("----------------------------------------------");
//
//        System.out.println("createBoolExpr");
//
//        // any unary expression (!)
//        System.out.println("BoolExpr: "+var);
//        System.out.println("Operator: " + operator);
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
//        System.out.println("----------------------------------------------");
//
//        System.out.println("createBoolExpr");
//
//        System.out.println("BoolExpr left: "+left_var);
//        System.out.println("BoolExpr right: "+right_var);
//        System.out.println("Operator: " + operator);

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
//        System.out.println("----------------------------------------------");
//
//        System.out.println("createIntExpr");
//        System.out.println("IntExpr: "+var);
//        System.out.println("Operator: " + operator);

        // any unary expression (+, -)
        if(operator == "+" || operator == "-")
            return new MyVar(PathTracker.ctx.mkInt(0));
        return new MyVar(PathTracker.ctx.mkFalse());
    }

    static MyVar createIntExpr(IntExpr left_var, IntExpr right_var, String operator){
//        System.out.println("----------------------------------------------");
//
//        System.out.println("createIntExpr");
//
//        System.out.println("IntExpr: "+left_var);
//        System.out.println("IntExpr: "+right_var);
//        System.out.println("Operator: " + operator);

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
            default:
                result= new MyVar(PathTracker.ctx.mkFalse());
                break;
        }
        return result;
    }

    static MyVar createStringExpr(SeqExpr left_var, SeqExpr right_var, String operator){


//        System.out.println("----------------------------------------------");
        Context c = PathTracker.ctx;
//        // we only support String.equals
//
//        System.out.println("createStringExpr");
//        System.out.println("SeqExpr: "+left_var);
//        System.out.println("SeqExpr: "+right_var);
//
//        System.out.println("Operator: " + operator);
        Expr z3var = c.mkEq(left_var, right_var);
//        System.out.println("after evaluation");
//        System.out.println("Expr: "+z3var);


//        Expr z3var = c.mkConst(c.mkSymbol(name + "_" + PathTracker.z3counter++), s);
//        PathTracker.z3model = c.mkAnd(c.mkEq(z3var, value), PathTracker.z3model);
//        return new MyVar(z3var, name);



        return new MyVar(z3var);
    }

    static void assign(MyVar var, String name, Expr value, Sort s){
        // all variable assignments, use single static assignment
        System.out.println("Assignment var: "+var.z3var);
        System.out.println("Assignment name: "+var.name);
        System.out.println("Argument name: "+name);
        System.out.println("what to assign: "+value);



    }

    static void encounteredNewBranch(MyVar condition, boolean value, int line_nr){
        // call the solver
        System.out.println("Found new branch on line "+ line_nr);
        System.out.println("Condition name: "+condition.name);
        System.out.println("Condition asd: "+condition.z3var);
        System.out.println("Value: "+value);
//        PathTracker.solve(PathTracker.z3model, true);

        System.out.print("Model: ");
        System.out.println(PathTracker.z3model);
        System.out.print("Branches: ");
        System.out.println(PathTracker.z3branches);


//        System.out.println("Name is: " + condition.name);


    }

    static void newSatisfiableInput(LinkedList<String> new_inputs) {
        // hurray! found a new branch using these new inputs!
    }

    static String fuzz(String[] inputs){
        // do something useful
        if(r.nextDouble() < 0.01) return "R";
        return inputs[r.nextInt(inputs.length)];
    }

    static void output(String out){
//        System.out.println(out);
    }

}
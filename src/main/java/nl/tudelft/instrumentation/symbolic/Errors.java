package nl.tudelft.instrumentation.symbolic;

import nl.tudelft.instrumentation.symbolic.SymbolicExecutionLab;

public class Errors {
    //static HashSet<String> allErrors = new HashSet<>();

    public static void __VERIFIER_error(int i) {
        //throw new IllegalStateException( "error_" + i );
        /*
        String errorString = ("error_" + i);
        System.out.println(errorString);
        allErrors.add(errorString);
        System.out.println(allErrors);
        */
        SymbolicExecutionLab.printError("error_" + i);
    }
}
package nl.tudelft.instrumentation.fuzzing;

import nl.tudelft.instrumentation.fuzzing.FuzzingLab;
import java.util.HashSet;

public class Errors {
    static HashSet<String> allErrors = new HashSet<>();

    public static void __VERIFIER_error(int i) {
        //throw new IllegalStateException( "error_" + i );
        /*
        String errorString = ("error_" + i);
        System.out.println(errorString);
        allErrors.add(errorString);
        System.out.println(allErrors);
        */
        FuzzingLab.printError("error_" + i);

    }
}
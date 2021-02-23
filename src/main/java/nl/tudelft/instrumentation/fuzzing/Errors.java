package nl.tudelft.instrumentation.fuzzing;

public class Errors {

    public static void __VERIFIER_error(int i) {
        //throw new IllegalStateException( "error_" + i );
        System.out.println("error_" + i);
    }
}
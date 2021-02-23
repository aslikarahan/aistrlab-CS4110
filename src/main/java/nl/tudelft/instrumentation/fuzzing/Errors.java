package nl.tudelft.instrumentation.fuzzing;
import java.io.IOException;  // Import the IOException class to handle errors
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;

public class Errors {
    static HashSet<String> allErrors = new HashSet<>();

    public static void __VERIFIER_error(int i) {
        //throw new IllegalStateException( "error_" + i );
        String errorString = ("error_" + i);
        System.out.println(errorString);
        allErrors.add(errorString);
        System.out.println(allErrors);

    }
}
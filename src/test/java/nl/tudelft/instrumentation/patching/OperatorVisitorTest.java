package nl.tudelft.instrumentation.patching;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperatorVisitorTest {

    public CompilationUnit instrument(String code){
        // read string as Java code
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> results = parser.parse(new StringReader(code));
        CompilationUnit unit = results.getResult().get();

        // print code
        System.out.println(unit.toString());

        // instrumentation
        unit.accept(new OperatorVisitor("Example.java"), null);
        return unit;
    }

    @Test
    public void testInstrumentationShouldAddImport(){
        String imprt = "import nl.tudelft.instrumentation.patching.*;";
        StringBuilder builder = new StringBuilder();
        builder.append("public class Test {\n")
                .append("    public static void main(String[] args) {\n")
                .append("        if (a.equals(b))\n")
                .append("           return \"INVALID\";\n" )
                .append("    }\n")
                .append("\n")
                .append("}");

        String code = builder.toString();
        CompilationUnit unit = instrument(code);
        System.out.println(unit.toString());

        int count = StringUtils.countMatches(unit.toString(), imprt);
        assertTrue(unit.toString().contains(imprt));
        assertEquals(1, count);
    }

    @Test
    public void testInstrumentationShouldCreatCallMethod(){
        String call = "public Void call()";
        StringBuilder builder = new StringBuilder();
        builder.append("public class Test {\n")
                .append("    public static void main(String[] args) {\n")
                .append("        if (a.equals(b))\n")
                .append("           return \"INVALID\";\n" )
                .append("    }\n")
                .append("\n")
                .append("}");

        String code = builder.toString();
        CompilationUnit unit = instrument(code);
        System.out.println(unit.toString());

        int count = StringUtils.countMatches(unit.toString(), call);
        assertTrue(unit.toString().contains(call));
        assertEquals(1, count);
    }

    @Test
    public void testInstrumentationShouldInsertImplements(){
        String implement = "public class Test implements CallableTraceRunner<Void>";
        StringBuilder builder = new StringBuilder();
        builder.append("public class Test {\n")
                .append("    public static void main(String[] args) {\n")
                .append("        if (a.equals(b))\n")
                .append("           return \"INVALID\";\n" )
                .append("    }\n")
                .append("\n")
                .append("}");

        String code = builder.toString();
        CompilationUnit unit = instrument(code);
        System.out.println(unit.toString());

        int count = StringUtils.countMatches(unit.toString(), implement);
        assertTrue(unit.toString().contains(implement));
        assertEquals(1, count);
    }

    @Test
    public void testInstrumentationShouldAddSequence(){
        String sequence = "public String[] sequence";
        StringBuilder builder = new StringBuilder();
        builder.append("public class Test {\n")
                .append("    public static void main(String[] args) {\n")
                .append("        if (a.equals(b))\n")
                .append("           return \"INVALID\";\n" )
                .append("    }\n")
                .append("\n")
                .append("}");

        String code = builder.toString();
        CompilationUnit unit = instrument(code);
        System.out.println(unit.toString());

        int count = StringUtils.countMatches(unit.toString(), sequence);
        assertTrue(unit.toString().contains(sequence));
        assertEquals(1, count);
    }

    @Test
    public void testInstrumentationShouldAddSetSequence(){
        String setSequence = "public void setSequence(String[] trace)";
        StringBuilder builder = new StringBuilder();
        builder.append("public class Test {\n")
                .append("    public static void main(String[] args) {\n")
                .append("        if (a.equals(b))\n")
                .append("           return \"INVALID\";\n" )
                .append("    }\n")
                .append("\n")
                .append("}");

        String code = builder.toString();
        CompilationUnit unit = instrument(code);
        System.out.println(unit.toString());

        int count = StringUtils.countMatches(unit.toString(), setSequence);
        assertTrue(unit.toString().contains(setSequence));
        assertEquals(1, count);
    }

    @Test
    public void testEqualsOperatorShouldCreateMyOperatorCall(){
        String myOperator = "nl.tudelft.instrumentation.patching.OperatorTracker.myOperator(\"==\", a, b, 0)";
        StringBuilder builder = new StringBuilder();
        builder.append("public class Test {\n")
                .append("    public static void main(String[] args) {\n")
                .append("        if (a == b)\n")
                .append("           return \"INVALID\";\n" )
                .append("    }\n")
                .append("\n")
                .append("}");

        String code = builder.toString();
        CompilationUnit unit = instrument(code);
        System.out.println(unit.toString());

        int count = StringUtils.countMatches(unit.toString(), myOperator);
        assertTrue(unit.toString().contains(myOperator));
        assertEquals(1, count);
    }

    @Test
    public void testNotEqualsOperatorShouldCreateMyOperatorCall(){
        String myOperator = "nl.tudelft.instrumentation.patching.OperatorTracker.myOperator(\"!=\", a, b, 0)";
        StringBuilder builder = new StringBuilder();
        builder.append("public class Test {\n")
                .append("    public static void main(String[] args) {\n")
                .append("        if (a != b)\n")
                .append("           return \"INVALID\";\n" )
                .append("    }\n")
                .append("\n")
                .append("}");

        String code = builder.toString();
        CompilationUnit unit = instrument(code);
        System.out.println(unit.toString());

        int count = StringUtils.countMatches(unit.toString(), myOperator);
        assertTrue(unit.toString().contains(myOperator));
        assertEquals(1, count);
    }

    @Test
    public void testGTOperatorShouldCreateMyOperatorCall(){
        String myOperator = "nl.tudelft.instrumentation.patching.OperatorTracker.myOperator(\">\", a, b, 0)";
        StringBuilder builder = new StringBuilder();
        builder.append("public class Test {\n")
                .append("    public static void main(String[] args) {\n")
                .append("        if (a > b)\n")
                .append("           return \"INVALID\";\n" )
                .append("    }\n")
                .append("\n")
                .append("}");

        String code = builder.toString();
        CompilationUnit unit = instrument(code);
        System.out.println(unit.toString());

        int count = StringUtils.countMatches(unit.toString(), myOperator);
        assertTrue(unit.toString().contains(myOperator));
        assertEquals(1, count);
    }

    @Test
    public void testGEOperatorShouldCreateMyOperatorCall(){
        String myOperator = "nl.tudelft.instrumentation.patching.OperatorTracker.myOperator(\">=\", a, b, 0)";
        StringBuilder builder = new StringBuilder();
        builder.append("public class Test {\n")
                .append("    public static void main(String[] args) {\n")
                .append("        if (a >= b)\n")
                .append("           return \"INVALID\";\n" )
                .append("    }\n")
                .append("\n")
                .append("}");

        String code = builder.toString();
        CompilationUnit unit = instrument(code);
        System.out.println(unit.toString());

        int count = StringUtils.countMatches(unit.toString(), myOperator);
        assertTrue(unit.toString().contains(myOperator));
        assertEquals(1, count);
    }

    @Test
    public void testLTOperatorShouldCreateMyOperatorCall(){
        String myOperator = "nl.tudelft.instrumentation.patching.OperatorTracker.myOperator(\"<\", a, b, 0)";
        StringBuilder builder = new StringBuilder();
        builder.append("public class Test {\n")
                .append("    public static void main(String[] args) {\n")
                .append("        if (a < b)\n")
                .append("           return \"INVALID\";\n" )
                .append("    }\n")
                .append("\n")
                .append("}");

        String code = builder.toString();
        CompilationUnit unit = instrument(code);
        System.out.println(unit.toString());

        int count = StringUtils.countMatches(unit.toString(), myOperator);
        assertTrue(unit.toString().contains(myOperator));
        assertEquals(1, count);
    }

    @Test
    public void testLEOperatorShouldCreateMyOperatorCall(){
        String myOperator = "nl.tudelft.instrumentation.patching.OperatorTracker.myOperator(\"<=\", a, b, 0)";
        StringBuilder builder = new StringBuilder();
        builder.append("public class Test {\n")
                .append("    public static void main(String[] args) {\n")
                .append("        if (a <= b)\n")
                .append("           return \"INVALID\";\n" )
                .append("    }\n")
                .append("\n")
                .append("}");

        String code = builder.toString();
        CompilationUnit unit = instrument(code);
        System.out.println(unit.toString());

        int count = StringUtils.countMatches(unit.toString(), myOperator);
        assertTrue(unit.toString().contains(myOperator));
        assertEquals(1, count);
    }



}


mvn clean package

java -cp target/aistr.jar nl.tudelft.instrumentation.Main --type=symbolic --file=../RERS/Problem11/Problem11.java > Problem11.java


javac -cp target/aistr.jar:lib/com.microsoft.z3.jar:. Problem11.java

java -cp target/aistr.jar:lib/com.microsoft.z3.jar:Problem11:. Problem11
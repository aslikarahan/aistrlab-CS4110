mvn clean package

java -cp target/aistr.jar nl.tudelft.instrumentation.Main --type=symbolic --file=../RERS/Problem1/Problem1.java > Problem1.java


javac -cp target/aistr.jar:lib/com.microsoft.z3.jar:. Problem1.java

java -cp target/aistr.jar:lib/com.microsoft.z3.jar:Problem1:. Problem1
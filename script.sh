mvn clean package

java -cp target/aistr.jar nl.tudelft.instrumentation.Main --type=symbolic --file=../RERS/Problem14/Problem14.java > Problem14.java

javac -cp target/aistr.jar:lib/com.microsoft.z3.jar:. Problem14.java

java -cp target/aistr.jar:lib/com.microsoft.z3.jar:Problem14:. Problem14
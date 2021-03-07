mvn clean package

java -cp target/aistr.jar nl.tudelft.instrumentation.Main --type=symbolic --file=../RERS/Problem19/Problem19.java > Problem19.java


javac -cp target/aistr.jar:lib/com.microsoft.z3.jar:. Problem19.java

java -cp target/aistr.jar:lib/com.microsoft.z3.jar:Problem19:. Problem19
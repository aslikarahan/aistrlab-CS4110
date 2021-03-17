mvn clean package

java -cp target/aistr.jar nl.tudelft.instrumentation.Main --type=patch --file=../RERS/Problem14/Problem14.java > Problem14.java

javac -cp target/aistr.jar:. Problem14.java

java -cp target/aistr.jar:/Problem14:. Problem14
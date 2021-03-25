x=17

mvn clean package

java -cp target/aistr.jar nl.tudelft.instrumentation.Main --type=patch --file=../RERS/Problem$x/Problem$x.java > Problem$x.java

javac -cp target/aistr.jar:. Problem$x.java

java -cp target/aistr.jar:/Problem$x:. Problem$x
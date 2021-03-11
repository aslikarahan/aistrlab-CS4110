mvn clean package
x=11
while [ $x -le 15 ]
do
  java -cp target/aistr.jar nl.tudelft.instrumentation.Main --type=symbolic --file=../RERS/Problem$x/Problem$x.java > Problem$x.java
  javac -cp target/aistr.jar:lib/com.microsoft.z3.jar:. Problem$x.java
  x=$(( $x + 1 ))
done

./run_all.sh
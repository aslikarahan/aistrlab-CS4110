x=11
while [ $x -le 15 ]
do
  java -cp target/aistr.jar nl.tudelft.instrumentation.Main --type=distance --file=../RERS/Problem$x/Problem$x.java > Problem$x.java
  javac -cp target/aistr.jar Problem$x.java
  x=$(( $x + 1 ))
done

./script.sh
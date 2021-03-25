mvn clean package

problems=( 7 11 12 13 14 15 17)
for i in "${problems[@]}"
do
  echo $i
	java -cp target/aistr.jar nl.tudelft.instrumentation.Main --type=patch --file=../RERS/Problem$i/Problem$i.java > Problem$i.java
  javac -cp target/aistr.jar:. Problem$i.java
  java -cp target/aistr.jar:/Problem$i:. Problem$i
done


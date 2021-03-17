x=11
while [ $x -le 15 ]
do
  java -cp target/aistr.jar:lib/com.microsoft.z3.jar:Problem$x:. Problem$x
  x=$(( $x + 1 ))
done
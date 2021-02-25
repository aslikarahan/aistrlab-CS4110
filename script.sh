x=11
while [ $x -le 16 ]
do
  java -cp target/aistr.jar:. Problem$x
  x=$(( $x + 1 ))
done
if test -f README.txt; then
    rm README.txt
fi
pwd


for i in ./*; do
  ../../../Problems/Problem11 < $i
done
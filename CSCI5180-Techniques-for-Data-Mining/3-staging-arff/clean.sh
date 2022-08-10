# cat lesscrap2 | sed -e 's/[ ][0-9]+/ /g' > test
cat lesscrap2 | sed 's/\([ ][0-9]*[ ]\)/\1,"/' | sed 's/$/"/'> test
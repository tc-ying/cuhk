# Counts the wordoccurances in file. 

perl -0777 -lape's/\s+/\n/g' FILENAME | sort | uniq -c | sort -nr > wordoccurences.txt

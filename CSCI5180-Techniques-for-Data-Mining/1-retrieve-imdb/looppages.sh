# Loops through html pages in folder and gets the imdb link strings.
# The links are saved in the thebooyahlist.txt

for page in `ls | grep .html`;
do
	cat $page | sed 's/tt[0-9][0-9]*/BOOYAH&HAYOOB/g' | grep BOOYAH | sed 's/.*BOOYAH//' | sed 's/HAYOOB.*//' | uniq >> thebooyahlist.txt
done

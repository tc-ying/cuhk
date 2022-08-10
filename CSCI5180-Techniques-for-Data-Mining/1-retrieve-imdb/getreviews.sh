rm lesscrap

for link in `cat thebooyahlist.txt`;
do

#	$page = http://www.imdb.com/title/$link/usercomments?start=0
#	echo $page | grep alt=\"[0-9][0-9]*\/10\" | sed 's/.*alt=\"//' | sed 's/[0-9][0-9]*\/10/&BOOYAH/' | sed 's/BOOYAH.*//' >> lesscrap
#	echo $page | tr '\n' ' ' | sed -e 's/\<\/p\>.\<p\>/BOOYAH/g'| sed 's/\<\/p\>\<p\>/BOOYAH/g' | sed 's/BOOYAH/\/g'| tr '' '\n' | sed -e 's/<[a-zA-Z\/][^>]*//g' | sed -e 's/[^a-zA-Z \n]//g' | sed -e 's/Was the above review useful to you.*//g' | sed -e 's/DOCTYPE.*//g' >> lesscrap

#	curl http://www.imdb.com/title/$link/usercomments?start=0 | grep alt=\"[0-9][0-9]*\/10\" | sed 's/.*alt=\"//' | sed 's/[0-9][0-9]*\/10/&BOOYAH/' | sed 's/BOOYAH.*//' >> lesscrap

	curl http://www.imdb.com/title/$link/usercomments?start=0 | tr '\n' ' ' | sed -e 's/\<img width=\"102/BOOYAH/g'| sed 's/\<div class=\"yn\" id=\"ynd_[0-9]*/BOOYAH/g' | sed 's/BOOYAH/\/g'| tr '' '\n' | sed -e 's/<[a-zA-Z\/][^>]*//g' | sed -e 's/[^a-zA-Z0-9 \n]//g' | sed -e 's/Was the above review useful to you.*//g' | sed -e 's/DOCTYPE.*//g' | sed 's/height[0-9]*[ ]*alt//' | sed 's/srchttpimediaimdbcomimagesshowtimes[0-9]*gif//' | sed 's/10[ ]*Author//' >> lesscrap

#	curl http://www.imdb.com/title/$link/usercomments?start=0 | tr '\n' ' ' | sed -e 's/\<\/p\>.\<p\>/BOOYAH/g'| sed 's/\<\/p\>\<p\>/BOOYAH/g' | sed 's/BOOYAH/\/g'| tr '' '\n' | sed -e 's/<[a-zA-Z\/][^>]*//g' | sed -e 's/[^a-zA-Z \n]//g' | sed -e 's/Was the above review useful to you.*//g' | sed -e 's/DOCTYPE.*//g' >> lesscrap

done

# cat dump | sed -e 's/\<\/p\>.\<p\>/BOOYAH/g'| sed 's/\<\/p\>\<p\>/BOOYAH/g' | sed 's/BOOYAH/\/g'| tr '' '\n' > dump3
# cat dump3 | sed -e 's/<[a-zA-Z\/][^>]*//g' > notags
# cat notags | sed -e 's/[^a-zA-Z \n]//g' > justascii
# cat justascii | sed -e 's/Was the above review useful to you.*//g' > lesscrap
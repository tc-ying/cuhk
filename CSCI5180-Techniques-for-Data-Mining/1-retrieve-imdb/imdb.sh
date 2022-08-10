#!/bin/bash
# Script gets the pages with links to movies with more than 1000 votes
# in descending order by the release date. Pages are saved as page101.html etc...
# Modify the link to change the search, the variable index points to the result
# page number.

for index in 1 101 201 301 401 501 601 701 801 901 1001 1101 1201 1301 1401 1501 1601; 
do
	curl -o page$index.html http://www.imdb.com/search/title?num_votes=1000,&sort=year,desc&start=$index&view=simple
#	curl http://www.imdb.com/search/title?num_votes=1000,&sort=year,desc&start=$index&view=simple > searchRes$index.html
done

# Capstone Project : Sentiment Analysis of IMDb reviews

This is a project to predict movie rating from what user says in [online review](https://www.imdb.com/).

Users are posting very few negative reviews. The model might discard rather than learn from bad reviews. We under-sampled good reviews to balance learning materials feeding into the model.

![imdb-rating](https://github.com/tc-ying/cuhk/blob/main/CSCI5180-Techniques-for-Data-Mining/docs/imdb-rating-relative-frequency.png)

Our team inspected the correlation between specific words and rating numbers. We picked out words that strongly indicate a good or bad review. This mix of positive and negative words were fed into [WEKA](https://www.cs.waikato.ac.nz/ml/weka/) for regression.

![feature-selection](https://github.com/tc-ying/cuhk/blob/main/CSCI5180-Techniques-for-Data-Mining/docs/selected-emotional-words.png)

We loved decision tree for its interpretability.

![model](https://github.com/tc-ying/cuhk/blob/main/CSCI5180-Techniques-for-Data-Mining/docs/j48-tree-teaser.PNG)

---------------
Copyright (c) 2010, M.Jauhiainen, H.P.Thai, Tc.Ying.  
All rights reserved.

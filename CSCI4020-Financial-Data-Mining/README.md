
# Financial Data Mining : Price movement prediction in Hong Kong equity market

This is an undergrad's data mining exercise to predict stock trading signals.

## Trading signals rather than price signals

Forecast of shares' rise and fall is not enough for buy-and-sell decisions. To make a trade, we must estimate [how long the move sustains](https://www.investopedia.com/terms/i/investment_horizon.asp) and [when to stop bleeding if forecast gets wrong](https://www.investopedia.com/terms/b/bracketedsellorder.asp).

## Buy signal : *Price up in n day*

In this definition, *buy signal* is set only if share price will remain up *after *n* days*. When it predicts an up-trend, **the signal also tells how long the market bottom will remain**. Therefore, a signal with small parameter *n* is equal to a short-term forecast. While large *n* is useful to long-term investors only.

![trading-signal](https://github.com/tc-ying/cuhk/blob/main/CSCI4020-Financial-Data-Mining/docs/breakeven-teaser.png)


## Buy signal : *Price up in n day + drawdown limit*

Price fluctuation during holding period is bad, even if profit is made at finishing line. We further embed a [drawdown](https://www.investopedia.com/terms/d/drawdown.asp) limit into the *buy signal*. When share price sinks too much before heading above water, buying signal is not initiated. Therefore, buying signal implies share price going up after *n* days, **and promises share price not falling below a threshold after bought**. Investor's position should not get stopped out if the signal is correct. Risk management, or capital protection, becomes possible.

![hang-seng-index](https://github.com/tc-ying/cuhk/blob/main/CSCI4020-Financial-Data-Mining/docs/stop-14-bottom-1-margin-teaser.png)

## Modeling : Technical indicator, Sliding window, K-nearest neighbor

The model was trained using [Hang Seng Index](https://www.bloomberg.com/quote/HSI:IND).

 1. Historical price was passed into [technical analysis](https://www.investopedia.com/terms/t/technicalindicator.asp) filters.
 2. Training and testing data were generated from sliding window across time series.
 3. K-nearest neighbor did the modeling.
 4. False negative signal means a missed profit. But false positive means a losing bet. To err on the safe side, maximizing *TP rate* is the training objective.

![kNN](https://github.com/tc-ying/cuhk/blob/main/CSCI4020-Financial-Data-Mining/docs/performance-teaser.png)

## Implementation

[*FDMIII*](https://github.com/tc-ying/cuhk/blob/master/CSCI4020-Financial-Data-Mining/src/FDMIII) is an analytics pipeline implementation in Java. It connects to Yahoo Finance, local MySQL, [WEKA](https://www.cs.waikato.ac.nz/ml/weka/) API, and co-ordinates the data wrangling, visualization and modeling in between. Web crawler may not work now as Yahoo Finance is disabling its API.

---------------
Copyright (c) 2011, Tc.Ying, C.M.<span></span>Au.  
All rights reserved.

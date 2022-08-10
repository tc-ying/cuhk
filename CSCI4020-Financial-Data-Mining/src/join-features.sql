SELECT 
    *
FROM
    (SELECT 
        date, adj_c
    FROM
        _hsi) A
        NATURAL JOIN
    (SELECT 
        date, ema8slope, ema13slope, ema18slope
    FROM
        hsi_emaslope) C
        NATURAL JOIN
    (SELECT 
        date, sma22slope
    FROM
        hsi_smaslope) H
        NATURAL JOIN
    (SELECT 
        date, rocp5, rocp6
    FROM
        hsi_rocp) D
        NATURAL JOIN
    (SELECT 
        date, devema8, devema16
    FROM
        hsi_ldevema) E
        NATURAL JOIN
    (SELECT 
        date, devsma9, devsma22
    FROM
        hsi_ldevsma) F
        NATURAL JOIN
    (SELECT 
        date, devsma10
    FROM
        hsi_hdevsma) G
        NATURAL JOIN
    (SELECT 
        date, gap
    FROM
        hsi_gap) I
        NATURAL JOIN
    (SELECT 
        date, 14bottom
    FROM
        hsi_stop1bottom) Z
WHERE
    date > '2006-01-01'
ORDER BY date;

SELECT 
    *
FROM
    (SELECT 
        date, adj_c
    FROM
        _hsi) A
        NATURAL JOIN
    (SELECT 
        date, ema8slope, ema13slope, ema18slope
    FROM
        hsi_emaslope) C
        NATURAL JOIN
    (SELECT 
        date, sma22slope
    FROM
        hsi_smaslope) H
        NATURAL JOIN
    (SELECT 
        date, rocp5, rocp6
    FROM
        hsi_rocp) D
        NATURAL JOIN
    (SELECT 
        date, devema8, devema16
    FROM
        hsi_ldevema) E
        NATURAL JOIN
    (SELECT 
        date, devsma9, devsma22
    FROM
        hsi_ldevsma) F
        NATURAL JOIN
    (SELECT 
        date, devsma10
    FROM
        hsi_hdevsma) G
        NATURAL JOIN
    (SELECT 
        date, gap
    FROM
        hsi_gap) I
        NATURAL JOIN
    (SELECT 
        date, rsi13
    FROM
        hsi_rsi) J
        NATURAL JOIN
    (SELECT 
        date, 14bottom
    FROM
        hsi_stop1bottom) Z
WHERE
    date > '2003-01-01'
ORDER BY date;
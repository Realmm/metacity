SELECT * FROM TradeSessions
WHERE state NOT IN ('EXECUTED', 'CANCELED');
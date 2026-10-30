-- Strategy Master Table
CREATE TABLE Strategies (
    StrategyID VARCHAR(64) PRIMARY KEY,
    StrategyName VARCHAR(128) NOT NULL,
    Description TEXT,
    IsActive BOOLEAN DEFAULT TRUE,
    CreatedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Generated Trade Captures with Composite Primary Key
CREATE TABLE trade_capture (
    CaptureID VARCHAR(64) PRIMARY KEY,
    StrategyID VARCHAR(64) NOT NULL,
    EntryDate DATE NOT NULL,
    Security VARCHAR(64) NOT NULL,
    InstrumentID VARCHAR(64) NOT NULL,
    EntryTime TIME NOT NULL,
    Side VARCHAR(10) NOT NULL CHECK (Side IN ('BUY', 'SELL')),
    LotSize INT NOT NULL,
    TargetQuantity DECIMAL(18, 4) NOT NULL,
    CalcEntry DECIMAL(18, 4) NOT NULL,
    CalcTG DECIMAL(18, 4) NOT NULL,
    CalcSL DECIMAL(18, 4) NOT NULL,
    CalcTSL DECIMAL(18, 4),
    GoNoDecision VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    CaptureTimestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trade_capture_strategy
        FOREIGN KEY (StrategyID) REFERENCES Strategies(StrategyID),

    -- Unique composite constraint
    CONSTRAINT uq_entrydate_security_instrument
        UNIQUE (EntryDate, Security, InstrumentID)
);
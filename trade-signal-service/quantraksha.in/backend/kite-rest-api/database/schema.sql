-- Strategy Master Table
CREATE TABLE Strategies (
    StrategyID VARCHAR(64) PRIMARY KEY,
    StrategyName VARCHAR(128) NOT NULL,
    Description TEXT,
    IsActive BOOLEAN DEFAULT TRUE,
    CreatedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Generated Trade Signals
CREATE TABLE TradeSignals (
    SignalID VARCHAR(64) PRIMARY KEY,
    StrategyID VARCHAR(64) NOT NULL,
    Security VARCHAR(64) NOT NULL,
    Side VARCHAR(10) NOT NULL CHECK (Side IN ('BUY', 'SELL')),
    LotSize INT NOT NULL,
    TargetQuantity DECIMAL(18, 4) NOT NULL,

    -- Date & Time Tracking
    EntryDate DATE NOT NULL,
    EntryTime TIME NOT NULL,
    ExitDate DATE,
    ExitTime TIME,

    -- Calculated Targets (Model Outputs)
    CalcEntry DECIMAL(18, 4) NOT NULL,
    CalcTG DECIMAL(18, 4) NOT NULL,        -- Target
    CalcSL DECIMAL(18, 4) NOT NULL,        -- Stop Loss
    CalcTSL DECIMAL(18, 4),               -- Trailing Stop Loss

    -- Actual Realized Price Levels
    ActEntry DECIMAL(18, 4),
    ActTG DECIMAL(18, 4),
    ActSL DECIMAL(18, 4),
    ActTSL DECIMAL(18, 4),

    -- Price Structure / Market Action
    HH DECIMAL(18, 4),                    -- Higher High
    LL DECIMAL(18, 4),                    -- Lower Low

    -- Risk Profile
    RiskRewardRatio DECIMAL(10, 4),
    RiskInPercentage DECIMAL(8, 4),
    RewardInPercentage DECIMAL(8, 4),

    -- Evaluation State
    GoNoDecision VARCHAR(10) NOT NULL CHECK (GoNoDecision IN ('GO', 'NO_GO', 'PENDING')),
    SignalTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (StrategyID) REFERENCES Strategies(StrategyID)
);
package com.quantraksha.kite.core;

import com.quantraksha.kite.model.Levels;
import com.quantraksha.kite.model.TradeCapture;

public class BankNiftyTradeCalculator extends ITradeCalculator{
    private final static double entryBuffer = 0.15;
    private final static double exitBuffer = 1.5;

    public BankNiftyTradeCalculator() {
        setStrategyName("BANKNIFTY-FUTURE-HH-LL");
        setTotalCapitalRequired(500000.00);
        setBufferCapitalRequiredAsPerRisk(50000.00);
        setRiskForTrade(25000.00);
    }

    @Override
    public TradeCapture calculateTrade(Levels levels) {
        TradeCapture tradeCapture = new TradeCapture();
        double entryPrice = calculateEntryPrice(levels.hh(), entryBuffer);
        double asmStopLossPrice = calculateAsmStopLossPrice(levels.ll(), entryBuffer);
        double targetPrice = calculateTargetPrice(entryPrice, exitBuffer);
        double stopLossPrice = calculateStopLossPrice(entryPrice, exitBuffer);


        tradeCapture.setInstrumentToken(levels.instrumentToken());
        tradeCapture.setCalcEntryPrice(entryPrice);
        tradeCapture.setCalcTargetPrice(targetPrice);
        tradeCapture.setCalcStopLoss(stopLossPrice);
        tradeCapture.setCalcTrailingStopLoss(asmStopLossPrice);
        tradeCapture.setEntryDate(levels.to());
        return tradeCapture;
    }
}

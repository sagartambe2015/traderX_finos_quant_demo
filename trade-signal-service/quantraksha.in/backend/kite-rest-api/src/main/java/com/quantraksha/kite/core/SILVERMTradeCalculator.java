package com.quantraksha.kite.core;

import com.quantraksha.kite.core.model.Levels;
import com.quantraksha.kite.core.model.TradeCapture;

public class SILVERMTradeCalculator extends ITradeCalculator {

    private final static double entryBuffer = 0.12;
    private final static double exitBuffer = 2;

    public SILVERMTradeCalculator() {
        setStrategyName("SILVERMM-FUTURE-HH-LL");
        setTotalCapitalRequired(600000.00);
        setBufferCapitalRequiredAsPerRisk(40000.00);
        setBufferCapitalRequiredAsPerRisk(200000.00);
        setRiskForTrade(70000.00);
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

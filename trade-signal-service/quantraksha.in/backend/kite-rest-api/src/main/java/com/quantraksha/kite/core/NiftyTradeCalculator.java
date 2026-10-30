package com.quantraksha.kite.core;

import com.quantraksha.kite.core.model.Levels;
import com.quantraksha.kite.core.model.TradeCapture;

import java.time.LocalDate;

public class NiftyTradeCalculator extends ITradeCalculator{
    private final static double entryBuffer = 0.125;
    private final static double exitBuffer = 1.25;
    private final static int LOT_SIZE = 65;

    public NiftyTradeCalculator() {
        setStrategyName("Nifty-FUTURE-HH-LL");
        setTotalCapitalRequired(400000.00);
        setBufferCapitalRequiredAsPerRisk(100000.00);
        setRiskForTrade(20000.00);
    }

    @Override
    public TradeCapture calculateTrade(Levels levels) {
        TradeCapture tradeCapture = new TradeCapture();
        double entryPrice = calculateEntryPrice(levels.hh(), entryBuffer);
        double asmStopLossPrice = calculateAsmStopLossPrice(levels.ll(), entryBuffer);
        double targetPrice = calculateTargetPrice(entryPrice, exitBuffer);
        double stopLossPrice = calculateStopLossPrice(entryPrice, exitBuffer);


        tradeCapture.setInstrumentToken(levels.instrumentToken());
        tradeCapture.setStrategyName(getStrategyName());
        tradeCapture.setEntryDate(LocalDate.parse(levels.to()));
        tradeCapture.setLotSize(LOT_SIZE);

        tradeCapture.setCalcEntryPrice(entryPrice);
        tradeCapture.setCalcTargetPrice(targetPrice);
        tradeCapture.setCalcStopLoss(stopLossPrice);
        tradeCapture.setCalcTrailingStopLoss(asmStopLossPrice);

        return tradeCapture;
    }
}

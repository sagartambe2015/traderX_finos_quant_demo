package com.quantraksha.kite.core;

import com.quantraksha.kite.core.model.Levels;
import com.quantraksha.kite.core.model.TradeCapture;

import java.time.LocalDate;


public class CrudeOilMTradeCalculator extends ITradeCalculator{

    private final static double entryBuffer = 0.4;
    private final static double exitBuffer = 4;
    private final static int LOT_SIZE = 10;

    public CrudeOilMTradeCalculator() {
        setStrategyName("CRUDEOIL-M-FUTURE-HH-LL");

        setTotalCapitalRequired(600000.00);
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
        tradeCapture.setStrategyName(getStrategyName());
        tradeCapture.setEntryDate(LocalDate.parse(levels.to()));
        tradeCapture.setLotSize(CrudeOilMTradeCalculator.LOT_SIZE);

        tradeCapture.setCalcEntryPrice(entryPrice);
        tradeCapture.setCalcTargetPrice(targetPrice);
        tradeCapture.setCalcStopLoss(stopLossPrice);
        tradeCapture.setCalcTrailingStopLoss(asmStopLossPrice);

        return tradeCapture;
    }


}

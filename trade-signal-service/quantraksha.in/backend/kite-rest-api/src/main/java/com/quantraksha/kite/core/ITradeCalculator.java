package com.quantraksha.kite.core;

import com.quantraksha.kite.model.Levels;
import com.quantraksha.kite.model.TradeCapture;


public abstract class ITradeCalculator {
    private Double totalCapitalRequired;
    private Double bufferCapitalRequiredAsPerRisk;
    private Double riskForTrade;
    private String strategyName;

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }



    public Double getTotalCapitalRequired() {
        return totalCapitalRequired;
    }

    public void setTotalCapitalRequired(Double totalCapitalRequired) {
        this.totalCapitalRequired = totalCapitalRequired;
    }

    public Double getBufferCapitalRequiredAsPerRisk() {
        return bufferCapitalRequiredAsPerRisk;
    }

    public void setBufferCapitalRequiredAsPerRisk(Double bufferCapitalRequiredAsPerRisk) {
        this.bufferCapitalRequiredAsPerRisk = bufferCapitalRequiredAsPerRisk;
    }

    public Double getRiskForTrade() {
        return riskForTrade;
    }

    public void setRiskForTrade(Double riskForTrade) {
        this.riskForTrade = riskForTrade;
    }



    public abstract TradeCapture calculateTrade(Levels levels);

    public double calculateEntryPrice(double highestHigh, double entryBuffer) {
        return  Math.ceil(highestHigh + (highestHigh * entryBuffer/100));
    }

    public double calculateAsmStopLossPrice(double lowestLow, double entryBuffer) {
        return Math.ceil(lowestLow - (lowestLow * entryBuffer/100));
    }

    public double calculateTargetPrice(double entryPrice, double exitBuffer) {
        return Math.ceil(entryPrice + (entryPrice * exitBuffer/100));
    }

    public double calculateStopLossPrice(double entryPrice, double exitBuffer) {
        return Math.ceil(entryPrice - (entryPrice * exitBuffer/100));
    }

}

package com.quantraksha.kite.model;

public class TradeCapture {
    long entryTime;
    long exitTime;
    String entryDate;
    String exitDate;
    long quantity;
    long tradeId;

    String strategyName;
    String instrumentToken;

    double actualEntryPrice;
    double actualExitPrice;
    double actualStopLoss;
    double actualProfitLoss;

    double calcEntryPrice;
    double calcTargetPrice;
    double calcStopLoss;

    double calcTrailingStopLoss;

    public long getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime;
    }

    public long getExitTime() {
        return exitTime;
    }

    public void setExitTime(long exitTime) {
        this.exitTime = exitTime;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    public String getExitDate() {
        return exitDate;
    }

    public void setExitDate(String exitDate) {
        this.exitDate = exitDate;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public long getTradeId() {
        return tradeId;
    }

    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }

    public double getActualEntryPrice() {
        return actualEntryPrice;
    }

    public void setActualEntryPrice(double actualEntryPrice) {
        this.actualEntryPrice = actualEntryPrice;
    }

    public double getActualExitPrice() {
        return actualExitPrice;
    }

    public void setActualExitPrice(double actualExitPrice) {
        this.actualExitPrice = actualExitPrice;
    }

    public double getActualStopLoss() {
        return actualStopLoss;
    }

    public void setActualStopLoss(double actualStopLoss) {
        this.actualStopLoss = actualStopLoss;
    }

    public double getActualProfitLoss() {
        return actualProfitLoss;
    }

    public void setActualProfitLoss(double actualProfitLoss) {
        this.actualProfitLoss = actualProfitLoss;
    }

    public double getCalcEntryPrice() {
        return calcEntryPrice;
    }

    public void setCalcEntryPrice(double calcEntryPrice) {
        this.calcEntryPrice = calcEntryPrice;
    }

    public double getCalcTargetPrice() {
        return calcTargetPrice;
    }

    public void setCalcTargetPrice(double calcTargetPrice) {
        this.calcTargetPrice = calcTargetPrice;
    }

    public double getCalcStopLoss() {
        return calcStopLoss;
    }

    public void setCalcStopLoss(double calcStopLoss) {
        this.calcStopLoss = calcStopLoss;
    }

    public double getCalcTrailingStopLoss() {
        return calcTrailingStopLoss;
    }

    public void setCalcTrailingStopLoss(double calcTrailingStopLoss) {
        this.calcTrailingStopLoss = calcTrailingStopLoss;
    }


    public String getInstrumentToken() {
        return instrumentToken;
    }

    public void setInstrumentToken(String instrumentToken) {
        this.instrumentToken = instrumentToken;
    }



    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    @Override
    public String toString() {
        return "TradeCaptureEntity{" +
                "calcEntryPrice=" + calcEntryPrice +
                ", calcStopLoss=" + calcStopLoss +
                ", calcTargetPrice=" + calcTargetPrice +
                ", calcTrailingStopLoss=" + calcTrailingStopLoss +
                ", instrumentToken='" + instrumentToken + '\'' +
                '}';
    }
}

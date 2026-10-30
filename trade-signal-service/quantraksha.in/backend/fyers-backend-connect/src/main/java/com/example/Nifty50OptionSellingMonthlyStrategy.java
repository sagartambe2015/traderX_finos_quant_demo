package com.example;

import java.util.List;

public class Nifty50OptionSellingMonthlyStrategy {
    // Parameters and Constants from the PDF rules
    private static final int MIN_OI_LOTS = 500;
    private static final double MIN_PREMIUM_MULTIPLIER = 1.5;
    private static final int STRIKE_OFFSET = 10;
    private static final int ENTRY_PRICE_SUBTRACT = 7;
    private static final int TARGET_ADD = 60;
    private static final int STOP_LOSS_ADD = 7;

    // Data structures for market data
    private double spotPrice;
    private double highestHighLast2Days;
    private double lowestLowLast2Days;
    private double currentDayHigh;
    private double currentDayLow;
    private List<OptionContract> availableOptions;

    // Option contract class — for storing option info
    private class OptionContract {
        double strike;
        String type; // "CALL" or "PUT"
        double premium;
        int openInterest;
    }

    private double roundUp(double value) {
        return Math.ceil(value);
    }

    // Step 1: Determine Strike Prices
    public double getCallStrike(double lowestLowLast2Days) {
        return roundUp(lowestLowLast2Days + (highestHighLast2Days * 0.15));
    }

    private double roundDown(double value) {
        return Math.floor(value);
    }

    public double getPutStrike(double highestHighLast2Days) {
        return roundDown(highestHighLast2Days - (lowestLowLast2Days * 0.15));
    }

    // Step 2 & 3: First and last strikes
    public double getCallLastStrike(double firstStrike) {
        return firstStrike - STRIKE_OFFSET;
    }
    public double getPutLastStrike(double firstStrike) {
        return firstStrike + STRIKE_OFFSET;
    }

    // Step 4: Filter by OI and Premium
    public OptionContract selectOption(String type) {
        // Loop to filter by OI and minimum premium
        for (OptionContract option : availableOptions) {
            if (option.type.equals(type) && option.openInterest >= MIN_OI_LOTS
                    && option.premium >= MIN_PREMIUM_MULTIPLIER * lowestLowLast2Days) {
                return option;
            }
        }
        return null;
    }

    // Step 6: Entry price calculation
    public double getEntryPrice() {
        return lowestLowLast2Days - ENTRY_PRICE_SUBTRACT;
    }

    // Step 7: Target price calculation
    public double getTargetPrice(double entryPrice) {
        return entryPrice + TARGET_ADD;
    }

    // Step 8: Stop-loss calculation
    public double getStopLossPrice(double entryPrice, double highestHigh) {
        return Math.min(entryPrice + STOP_LOSS_ADD, highestHigh);
    }

    // Methods for order timing logic, replacement orders, etc. as per rules
    // ...
}

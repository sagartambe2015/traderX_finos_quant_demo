package com.example;

public class Pojo {
    record HighLow(double maxHigh, double minLow) {}
    record CallPutStrikes(int callStrike, int putStrike){}
    record OrderEntry(String strike,double entryPrice, double target, double sl, double tsl ){}
    record OptionSellHighLowStrike(double maxHigh, double minLow) {}
}

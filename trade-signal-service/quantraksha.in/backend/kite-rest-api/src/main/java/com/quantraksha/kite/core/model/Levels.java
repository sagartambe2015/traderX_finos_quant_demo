package com.quantraksha.kite.core.model;



import java.time.LocalDate;
import java.util.List;

/** from/to are the dates of the first and last candle used */
public record Levels(String instrumentToken, int days, double hh, double ll, String from, String to,
                     List<Candle> candles) {
    @Override
    public String toString() {
        return "Levels{" +
                "instrumentToken='" + instrumentToken + '\'' +
                ", days=" + days +
                ", hh=" + hh +
                ", ll=" + ll +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", candles=" + candles +
                '}';
    }
}
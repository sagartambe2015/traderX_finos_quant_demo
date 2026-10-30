package com.quantraksha.kite.model;



import java.util.List;

/** from/to are the dates of the first and last candle used */
public record Levels(String instrumentToken, int days, double hh, double ll, String from, String to,
                     List<Candle> candles) {
}
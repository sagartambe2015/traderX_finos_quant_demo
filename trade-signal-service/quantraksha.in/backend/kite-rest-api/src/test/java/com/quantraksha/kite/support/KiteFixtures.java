package com.quantraksha.kite.support;

import com.zerodhatech.models.HistoricalData;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

/** Builds Kite HistoricalData responses the way the SDK returns them: candles in dataArrayList, oldest first. */
public final class KiteFixtures {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private KiteFixtures() {
    }

    public static LocalDate todayIst() {
        return LocalDate.now(IST);
    }

    /** A daily candle for {@code date}, with Kite's timestamp format, e.g. 2026-10-27T00:00:00+0530. */
    public static HistoricalData candle(LocalDate date, double open, double high, double low, double close) {
        HistoricalData c = new HistoricalData();
        c.timeStamp = date + "T00:00:00+0530";
        c.open = open;
        c.high = high;
        c.low = low;
        c.close = close;
        return c;
    }

    public static HistoricalData history(HistoricalData... candles) {
        HistoricalData data = new HistoricalData();
        data.dataArrayList = new ArrayList<>(java.util.List.of(candles));
        return data;
    }
}

package com.quantraksha.kite.core.service;

import com.quantraksha.kite.api.KiteApiException;
import com.quantraksha.kite.core.model.Candle;
import com.quantraksha.kite.core.model.Levels;
import com.quantraksha.kite.core.session.KiteSessionService;
import com.zerodhatech.models.HistoricalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class LevelCalculatorService {


    private static final Logger log = LoggerFactory.getLogger(LevelCalculatorService.class);

    private final KiteSessionService kite;

    private static final int MAX_DAYS = 20;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    public LevelCalculatorService(KiteSessionService kite) {
        this.kite = kite;
    }

    public Levels getLevels(String instrumentToken, int days) {
        if (!instrumentToken.matches("\\d+")) {
            throw new IllegalArgumentException("instrumentToken must be numeric, got " + instrumentToken);
        }
        if (days < 1 || days > MAX_DAYS) {
            throw new IllegalArgumentException("days must be 1 to " + MAX_DAYS);
        }

        LocalDate today = LocalDate.now(IST);
        // Calendar lookback wide enough to cover weekends and exchange holidays
        LocalDate start = today.minusDays(days * 2L + 10);
        Date from = Date.from(start.atStartOfDay(IST).toInstant());
        Date to = Date.from(today.atStartOfDay(IST).toInstant());
        HistoricalData data = kite.call(k -> k.getHistoricalData(from, to, instrumentToken, "day", false, false));

        // Timestamps look like 2026-10-27T00:00:00+0530; the first 10 chars are the IST trading date
        List<Candle> completed = data.dataArrayList.stream()
                .map(c -> new Candle(c.timeStamp.substring(0, 10), c.open, c.high, c.low, c.close))
                .filter(c -> c.date().compareTo(today.toString()) < 0)
                .toList();
        if (completed.size() < days) {
            throw new KiteApiException(HttpStatus.BAD_GATEWAY, "DataException",
                    "Kite returned " + completed.size() + " completed daily candles, need " + days);
        }

        List<Candle> window = completed.subList(completed.size() - days, completed.size());
        double hh = window.stream().mapToDouble(Candle::high).max().orElseThrow();
        double ll = window.stream().mapToDouble(Candle::low).min().orElseThrow();
        return new Levels(instrumentToken, days, hh, ll, window.get(0).date(), window.get(days - 1).date(), window);
    }





}

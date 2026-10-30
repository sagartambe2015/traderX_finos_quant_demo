package com.quantraksha.kite.api;

import com.quantraksha.kite.core.session.KiteSessionService;
import com.zerodhatech.models.LTPQuote;
import com.zerodhatech.models.OHLCQuote;
import com.zerodhatech.models.Quote;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Instruments are EXCHANGE:TRADINGSYMBOL, repeated: /api/quote?i=NSE:INFY&i=NSE:TCS */
@RestController
@RequestMapping("/api")
public class MarketController {

    // Kite's limit for /quote; /ohlc and /ltp allow more, but one cap keeps it simple
    private static final int MAX_INSTRUMENTS = 500;

    private final KiteSessionService kite;

    public MarketController(KiteSessionService kite) {
        this.kite = kite;
    }

    @GetMapping("/quote")
    public Map<String, Quote> quote(@RequestParam("i") List<String> instruments) {
        String[] ids = validate(instruments);
        return kite.call(k -> k.getQuote(ids));
    }

    @GetMapping("/ohlc")
    public Map<String, OHLCQuote> ohlc(@RequestParam("i") List<String> instruments) {
        String[] ids = validate(instruments);
        return kite.call(k -> k.getOHLC(ids));
    }

    @GetMapping("/ltp")
    public Map<String, LTPQuote> ltp(@RequestParam("i") List<String> instruments) {
        String[] ids = validate(instruments);
        return kite.call(k -> k.getLTP(ids));
    }

    private static String[] validate(List<String> instruments) {
        if (instruments.isEmpty() || instruments.size() > MAX_INSTRUMENTS) {
            throw new IllegalArgumentException("Pass 1 to " + MAX_INSTRUMENTS + " instruments as ?i=EXCHANGE:SYMBOL");
        }
        for (String id : instruments) {
            if (!id.contains(":")) {
                throw new IllegalArgumentException("Instrument must be EXCHANGE:SYMBOL, got " + id);
            }
        }
        return instruments.toArray(String[]::new);
    }
}

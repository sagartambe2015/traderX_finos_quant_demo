package com.quantraksha.kite.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.quantraksha.kite.core.CrudeOilMTradeCalculator;
import com.quantraksha.kite.core.NiftyTradeCalculator;
import com.quantraksha.kite.model.Levels;
import com.quantraksha.kite.model.TradeCapture;
import java.util.List;
import org.junit.jupiter.api.Test;

class TradeCaptureServiceTest {

    private final TradeCaptureService service = new TradeCaptureService();
    private final Levels levels = new Levels("146796039", 2, 6010, 5905, "2026-10-28", "2026-10-29", List.of());

    @Test
    void crudeOilMUsesTheCrudeOilMCalculator() {
        TradeCapture trade = service.getTradeCaptureForCrudeOilMFuture(levels);

        TradeCapture expected = new CrudeOilMTradeCalculator().calculateTrade(levels);
        assertThat(trade).usingRecursiveComparison().isEqualTo(expected);
        assertThat(trade.getCalcEntryPrice()).isEqualTo(6035);
    }

    @Test
    void niftyUsesTheNiftyCalculator() {
        TradeCapture trade = service.getTradeCaptureForNiftyFuture(levels);

        TradeCapture expected = new NiftyTradeCalculator().calculateTrade(levels);
        assertThat(trade).usingRecursiveComparison().isEqualTo(expected);
        // Nifty's 0.125% entry buffer, not crude's 0.4%: ceil(6010 * 1.00125) = 6018
        assertThat(trade.getCalcEntryPrice()).isEqualTo(6018);
    }
}

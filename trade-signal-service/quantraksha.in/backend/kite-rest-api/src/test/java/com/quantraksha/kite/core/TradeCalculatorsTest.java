package com.quantraksha.kite.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.quantraksha.kite.core.model.Levels;
import com.quantraksha.kite.core.model.TradeCapture;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Each instrument calculator with its own entry/exit buffers. Expected prices were worked out by hand, e.g.
 * CRUDEOILM (0.4% / 4%): entry = ceil(6010 * 1.004) = 6035, target = ceil(6035 * 1.04) = 6277.
 */
class TradeCalculatorsTest {

    static Stream<Arguments> calculators() {
        return Stream.of(
                // calculator, strategy name, HH, LL, entry, ASM stop (trailing), target, stop loss
                Arguments.of(named(CrudeOilMTradeCalculator::new), "CRUDEOIL-M-FUTURE-HH-LL",
                        6010, 5905, 6035, 5882, 6277, 5794),
                Arguments.of(named(NiftyTradeCalculator::new), "Nifty-FUTURE-HH-LL",
                        24010, 23805, 24041, 23776, 24342, 23741),
                Arguments.of(named(BankNiftyTradeCalculator::new), "BANKNIFTY-FUTURE-HH-LL",
                        52010, 51505, 52089, 51428, 52871, 51308),
                Arguments.of(named(GOLDMTradeCalculator::new), "GOLDM-M-FUTURE-HH-LL",
                        78010, 77550, 78104, 77457, 79276, 76933),
                Arguments.of(named(SILVERMTradeCalculator::new), "SILVERMM-FUTURE-HH-LL",
                        95010, 94005, 95125, 93893, 97028, 93223));
    }

    private static Named<Supplier<ITradeCalculator>> named(Supplier<ITradeCalculator> s) {
        return Named.of(s.get().getClass().getSimpleName(), s);
    }

    private static Levels levels(String token, double hh, double ll) {
        return new Levels(token, 2, hh, ll, "2026-10-28", "2026-10-29", List.of());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("calculators")
    void calculatesLevelsFromHighestHighAndLowestLow(Supplier<ITradeCalculator> factory, String strategy,
            double hh, double ll, double entry, double asmStop, double target, double stopLoss) {
        TradeCapture trade = factory.get().calculateTrade(levels("12345", hh, ll));

        assertThat(trade.getInstrumentToken()).isEqualTo("12345");
        assertThat(trade.getCalcEntryPrice()).isEqualTo(entry);
        assertThat(trade.getCalcTrailingStopLoss()).isEqualTo(asmStop);
        assertThat(trade.getCalcTargetPrice()).isEqualTo(target);
        assertThat(trade.getCalcStopLoss()).isEqualTo(stopLoss);
        // Entry date is the last candle used for the levels
        assertThat(trade.getEntryDate()).isEqualTo("2026-10-29");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("calculators")
    void configuresStrategyNameAndCapital(Supplier<ITradeCalculator> factory, String strategy) {
        ITradeCalculator calc = factory.get();

        assertThat(calc.getStrategyName()).isEqualTo(strategy);
        assertThat(calc.getTotalCapitalRequired()).isPositive();
        assertThat(calc.getBufferCapitalRequiredAsPerRisk()).isPositive();
        assertThat(calc.getRiskForTrade()).isPositive();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("calculators")
    void pricesAreOrderedForALongBreakout(Supplier<ITradeCalculator> factory, String strategy, double hh, double ll) {
        TradeCapture trade = factory.get().calculateTrade(levels("1", hh, ll));

        assertThat(trade.getCalcTargetPrice()).isGreaterThan(trade.getCalcEntryPrice());
        assertThat(trade.getCalcEntryPrice()).isGreaterThan(hh);
        assertThat(trade.getCalcStopLoss()).isLessThan(trade.getCalcEntryPrice());
        assertThat(trade.getCalcTrailingStopLoss()).isLessThan(ll);
    }

    @Test
    void niftyCapitalAndRisk() {
        NiftyTradeCalculator calc = new NiftyTradeCalculator();
        assertThat(calc.getTotalCapitalRequired()).isEqualTo(400000.00);
        assertThat(calc.getBufferCapitalRequiredAsPerRisk()).isEqualTo(100000.00);
        assertThat(calc.getRiskForTrade()).isEqualTo(20000.00);
    }

    @Disabled("bug: calculateTrade never copies the calculator's strategyName into TradeCapture, so it is always null")
    @ParameterizedTest(name = "{0}")
    @MethodSource("calculators")
    void tradeCarriesStrategyName(Supplier<ITradeCalculator> factory, String strategy) {
        TradeCapture trade = factory.get().calculateTrade(levels("1", 100.5, 99.5));
        assertThat(trade.getStrategyName()).isEqualTo(strategy);
    }

    @Disabled("bug: CrudeOilM/GOLDM/SILVERM constructors call setBufferCapitalRequiredAsPerRisk twice (40000, then 200000);"
            + " the first value is overwritten. Update the expected value once the intended field is confirmed")
    @Test
    void commodityBufferCapitalIsNotOverwritten() {
        assertThat(new CrudeOilMTradeCalculator().getBufferCapitalRequiredAsPerRisk()).isEqualTo(40000.00);
    }
}

package com.quantraksha.kite.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.quantraksha.kite.model.Levels;
import com.quantraksha.kite.model.TradeCapture;
import org.junit.jupiter.api.Test;

/** The shared price formulas: each adds or subtracts a percentage buffer and rounds up to a whole rupee. */
class ITradeCalculatorTest {

    private final ITradeCalculator calc = new ITradeCalculator() {
        @Override
        public TradeCapture calculateTrade(Levels levels) {
            throw new UnsupportedOperationException();
        }
    };

    @Test
    void entryIsHighestHighPlusBufferRoundedUp() {
        // 1000 + 0.25% = 1002.5 -> 1003
        assertThat(calc.calculateEntryPrice(1000, 0.25)).isEqualTo(1003);
    }

    @Test
    void asmStopLossIsLowestLowMinusBufferRoundedUp() {
        // 1000 - 0.25% = 997.5 -> 998
        assertThat(calc.calculateAsmStopLossPrice(1000, 0.25)).isEqualTo(998);
    }

    @Test
    void targetIsEntryPlusExitBufferRoundedUp() {
        // 1003 + 1.5% = 1018.045 -> 1019
        assertThat(calc.calculateTargetPrice(1003, 1.5)).isEqualTo(1019);
    }

    @Test
    void stopLossIsEntryMinusExitBufferRoundedUp() {
        // 1003 - 1.5% = 987.955 -> 988
        assertThat(calc.calculateStopLossPrice(1003, 1.5)).isEqualTo(988);
    }

    @Test
    void zeroBufferLeavesWholePricesUnchanged() {
        assertThat(calc.calculateEntryPrice(1000, 0)).isEqualTo(1000);
        assertThat(calc.calculateAsmStopLossPrice(1000, 0)).isEqualTo(1000);
    }

    @Test
    void stopLossIsBelowEntryAndTargetAbove() {
        double entry = calc.calculateEntryPrice(24010, 0.125);
        assertThat(calc.calculateStopLossPrice(entry, 1.25)).isLessThan(entry);
        assertThat(calc.calculateTargetPrice(entry, 1.25)).isGreaterThan(entry);
    }
}

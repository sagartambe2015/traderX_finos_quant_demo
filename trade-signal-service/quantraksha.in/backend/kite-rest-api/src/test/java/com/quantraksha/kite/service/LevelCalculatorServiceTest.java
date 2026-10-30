package com.quantraksha.kite.service;

import static com.quantraksha.kite.support.KiteFixtures.IST;
import static com.quantraksha.kite.support.KiteFixtures.candle;
import static com.quantraksha.kite.support.KiteFixtures.history;
import static com.quantraksha.kite.support.KiteFixtures.todayIst;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.quantraksha.kite.api.KiteApiException;
import com.quantraksha.kite.model.Levels;
import com.quantraksha.kite.session.KiteSessionService;
import com.quantraksha.kite.session.KiteSessionService.KiteCall;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.models.HistoricalData;
import java.time.LocalDate;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class LevelCalculatorServiceTest {

    @Mock
    private KiteSessionService kite;

    @Captor
    private ArgumentCaptor<KiteCall<HistoricalData>> kiteCall;

    private LevelCalculatorService service;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        service = new LevelCalculatorService(kite);
        today = todayIst();
    }

    @Test
    void highestHighAndLowestLowOfLastNCompletedCandles() {
        when(kite.call(any())).thenReturn(history(
                candle(today.minusDays(3), 100, 150, 90, 120),   // outside a 2-day window
                candle(today.minusDays(2), 120, 130, 110, 125),
                candle(today.minusDays(1), 125, 140, 115, 135)));

        Levels levels = service.getLevels("15736578", 2);

        assertThat(levels.instrumentToken()).isEqualTo("15736578");
        assertThat(levels.days()).isEqualTo(2);
        assertThat(levels.hh()).isEqualTo(140);
        assertThat(levels.ll()).isEqualTo(110);
        assertThat(levels.from()).isEqualTo(today.minusDays(2).toString());
        assertThat(levels.to()).isEqualTo(today.minusDays(1).toString());
        assertThat(levels.candles()).hasSize(2);
    }

    @Test
    void todaysFormingCandleIsIgnored() {
        when(kite.call(any())).thenReturn(history(
                candle(today.minusDays(2), 120, 130, 110, 125),
                candle(today.minusDays(1), 125, 140, 115, 135),
                candle(today, 135, 999, 1, 500)));

        Levels levels = service.getLevels("15736578", 2);

        assertThat(levels.hh()).isEqualTo(140);
        assertThat(levels.ll()).isEqualTo(110);
        assertThat(levels.to()).isEqualTo(today.minusDays(1).toString());
    }

    @Test
    void candleDateIsTakenFromKiteTimestamp() {
        when(kite.call(any())).thenReturn(history(candle(today.minusDays(1), 1, 2, 0.5, 1.5)));

        Levels levels = service.getLevels("1", 1);

        assertThat(levels.candles().get(0).date()).isEqualTo(today.minusDays(1).toString());
        assertThat(levels.candles().get(0).close()).isEqualTo(1.5);
    }

    @Test
    void requestsDailyCandlesWithALookbackThatCoversHolidays() throws Throwable {  // KiteException extends Throwable
        when(kite.call(kiteCall.capture())).thenReturn(history(
                candle(today.minusDays(2), 1, 2, 0.5, 1.5),
                candle(today.minusDays(1), 1, 2, 0.5, 1.5)));

        service.getLevels("15736578", 2);

        KiteConnect client = mock(KiteConnect.class);
        kiteCall.getValue().call(client);
        Date from = Date.from(today.minusDays(2 * 2 + 10).atStartOfDay(IST).toInstant());
        Date to = Date.from(today.atStartOfDay(IST).toInstant());
        verify(client).getHistoricalData(eq(from), eq(to), eq("15736578"), eq("day"), eq(false), eq(false));
    }

    @Test
    void tooFewCompletedCandlesIsABadGateway() {
        when(kite.call(any())).thenReturn(history(
                candle(today.minusDays(1), 1, 2, 0.5, 1.5),
                candle(today, 1, 2, 0.5, 1.5)));

        assertThatThrownBy(() -> service.getLevels("15736578", 2))
                .isInstanceOfSatisfying(KiteApiException.class, e -> {
                    assertThat(e.status()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(e.errorType()).isEqualTo("DataException");
                    assertThat(e.getMessage()).contains("1 completed daily candles, need 2");
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {"NIFTY", "123abc", "", "-1", "12.5"})
    void nonNumericTokenIsRejectedWithoutCallingKite(String token) {
        assertThatThrownBy(() -> service.getLevels(token, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("instrumentToken must be numeric");
        verifyNoInteractions(kite);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 21})
    void daysOutsideOneToTwentyIsRejected(int days) {
        assertThatThrownBy(() -> service.getLevels("15736578", days))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("days must be 1 to 20");
        verifyNoInteractions(kite);
    }

    @Test
    void kiteErrorsPropagate() {
        when(kite.call(any())).thenThrow(
                new KiteApiException(HttpStatus.UNAUTHORIZED, "NotLoggedIn", "No Kite session. Log in first."));

        assertThatThrownBy(() -> service.getLevels("15736578", 2))
                .isInstanceOfSatisfying(KiteApiException.class,
                        e -> assertThat(e.status()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }
}

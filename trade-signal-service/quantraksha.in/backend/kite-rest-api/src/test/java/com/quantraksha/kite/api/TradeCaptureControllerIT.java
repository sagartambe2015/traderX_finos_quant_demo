package com.quantraksha.kite.api;

import static com.quantraksha.kite.support.KiteFixtures.candle;
import static com.quantraksha.kite.support.KiteFixtures.history;
import static com.quantraksha.kite.support.KiteFixtures.todayIst;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quantraksha.kite.session.KiteSessionService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** HTTP -> controller -> LevelCalculatorService -> calculator -> Gson JSON, with only the Kite session mocked. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
class TradeCaptureControllerIT {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private KiteSessionService kite;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = todayIst();
    }

    private void kiteReturnsTwoDaysWithHighAndLow(double hh, double ll) {
        when(kite.call(any())).thenReturn(history(
                candle(today.minusDays(2), ll + 10, hh - 5, ll, hh - 10),
                candle(today.minusDays(1), hh - 10, hh, ll + 5, hh - 5),
                candle(today, hh, hh + 1000, ll - 1000, hh)));   // today's forming candle, ignored
    }

    @Test
    void niftyFutureTrade() throws Exception {
        kiteReturnsTwoDaysWithHighAndLow(24010, 23805);

        mvc.perform(get("/api/calculateTrade/nse/15736578"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instrumentToken").value("15736578"))
                .andExpect(jsonPath("$.calcEntryPrice").value(24041.0))
                .andExpect(jsonPath("$.calcTargetPrice").value(24342.0))
                .andExpect(jsonPath("$.calcStopLoss").value(23741.0))
                .andExpect(jsonPath("$.calcTrailingStopLoss").value(23776.0))
                .andExpect(jsonPath("$.entryDate").value(today.minusDays(1).toString()));
    }

    @Test
    void crudeOilMFutureTrade() throws Exception {
        kiteReturnsTwoDaysWithHighAndLow(6010, 5905);

        mvc.perform(get("/api/calculateTrade/mcx/146796039"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instrumentToken").value("146796039"))
                .andExpect(jsonPath("$.calcEntryPrice").value(6035.0))
                .andExpect(jsonPath("$.calcTargetPrice").value(6277.0))
                .andExpect(jsonPath("$.calcStopLoss").value(5794.0))
                .andExpect(jsonPath("$.calcTrailingStopLoss").value(5882.0));
    }

    @Test
    void daysParameterWidensTheWindow() throws Exception {
        when(kite.call(any())).thenReturn(history(
                candle(today.minusDays(3), 100, 24500, 23000, 100),
                candle(today.minusDays(2), 100, 24010, 23805, 100),
                candle(today.minusDays(1), 100, 24000, 23900, 100)));

        // 3 days picks up the 24500 high: ceil(24500 * 1.00125) = 24531
        mvc.perform(get("/api/calculateTrade/nse/15736578").param("days", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calcEntryPrice").value(24531.0));
    }

    @Test
    void nonNumericTokenIsA400InKiteErrorShape() throws Exception {
        mvc.perform(get("/api/calculateTrade/nse/NIFTY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_type").value("InputException"))
                .andExpect(jsonPath("$.message").value("instrumentToken must be numeric, got NIFTY"));
        verifyNoInteractions(kite);
    }

    @Test
    void daysOutOfRangeIsA400() throws Exception {
        mvc.perform(get("/api/calculateTrade/mcx/146796039").param("days", "21"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("days must be 1 to 20"));
    }

    @Test
    void notLoggedInIsA401() throws Exception {
        when(kite.call(any())).thenThrow(
                new KiteApiException(HttpStatus.UNAUTHORIZED, "NotLoggedIn", "No Kite session. Log in first."));

        mvc.perform(get("/api/calculateTrade/nse/15736578"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error_type").value("NotLoggedIn"));
    }

    @Test
    void tooFewCandlesIsA502() throws Exception {
        when(kite.call(any())).thenReturn(history(candle(today.minusDays(1), 1, 2, 0.5, 1.5)));

        mvc.perform(get("/api/calculateTrade/nse/15736578"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error_type").value("DataException"));
    }

    @Disabled("bug: /calculateTrade/mcx/{token} always uses CrudeOilMTradeCalculator, so a GOLDM token gets"
            + " crude's 0.4%/4% buffers instead of GOLDM's 0.12%/1.5%")
    @Test
    void mcxGoldMTokenUsesGoldMBuffers() throws Exception {
        kiteReturnsTwoDaysWithHighAndLow(78010, 77550);

        // Replace with the real GOLDM futures instrument token
        mvc.perform(get("/api/calculateTrade/mcx/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calcEntryPrice").value(78104.0))
                .andExpect(jsonPath("$.calcTargetPrice").value(79276.0));
    }
}

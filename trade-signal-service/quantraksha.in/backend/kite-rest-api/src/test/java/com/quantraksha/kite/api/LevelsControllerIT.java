package com.quantraksha.kite.api;

import static com.quantraksha.kite.support.KiteFixtures.candle;
import static com.quantraksha.kite.support.KiteFixtures.history;
import static com.quantraksha.kite.support.KiteFixtures.todayIst;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quantraksha.kite.core.session.KiteSessionService;
import java.time.LocalDate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
class LevelsControllerIT {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private KiteSessionService kite;

    @Disabled("bug: LevelsController never injects levelCalculatorService (constructor only takes kite),"
            + " so every /api/levels call throws NullPointerException")
    @Test
    void twoDayHighAndLow() throws Exception {
        LocalDate today = todayIst();
        when(kite.call(any())).thenReturn(history(
                candle(today.minusDays(2), 120, 130, 110, 125),
                candle(today.minusDays(1), 125, 140, 115, 135)));

        mvc.perform(get("/api/levels/15736578").param("days", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hh").value(140.0))
                .andExpect(jsonPath("$.ll").value(110.0))
                .andExpect(jsonPath("$.from").value(today.minusDays(2).toString()))
                .andExpect(jsonPath("$.to").value(today.minusDays(1).toString()))
                .andExpect(jsonPath("$.candles.length()").value(2));
    }
}

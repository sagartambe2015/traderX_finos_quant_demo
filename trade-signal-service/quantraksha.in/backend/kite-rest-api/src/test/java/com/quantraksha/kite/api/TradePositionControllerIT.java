package com.quantraksha.kite.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quantraksha.kite.core.session.KiteSessionService;
import com.quantraksha.kite.persist.entity.GoNoDecision;
import com.quantraksha.kite.persist.entity.StrategyEntity;
import com.quantraksha.kite.persist.entity.TradeCaptureEntity;
import com.quantraksha.kite.persist.entity.TradeSide;
import com.quantraksha.kite.persist.repository.StrategyRepository;
import com.quantraksha.kite.persist.repository.TradeCaptureRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Real repositories and JSON converter on H2. Not transactional, so the strategy stays an unloaded proxy, as in production. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
class TradePositionControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TradeCaptureRepository captures;

    @Autowired
    private StrategyRepository strategies;

    @MockitoBean
    private KiteSessionService kite;

    // H2 is shared with the other ITs, which expect empty tables
    @AfterEach
    void tearDown() {
        captures.deleteAll();
        strategies.deleteAll();
    }

    @Test
    void noSavedSignalsGivesAnEmptyList() throws Exception {
        mvc.perform(get("/api/getAllTradesAndPosition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void savedSignalIsReturnedWithItsDatesAndStrategy() throws Exception {
        StrategyEntity strategy = strategies.save(new StrategyEntity("CRUDE-HH-LL", "CRUDE-HH-LL"));
        captures.save(capture(strategy));

        mvc.perform(get("/api/getAllTradesAndPosition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].captureId").value("signal-1"))
                .andExpect(jsonPath("$[0].strategyId").value("CRUDE-HH-LL"))
                .andExpect(jsonPath("$[0].security").value("CRUDEOILM25NOVFUT"))
                .andExpect(jsonPath("$[0].side").value("LONG"))
                .andExpect(jsonPath("$[0].calcEntry").value(6001.0))
                .andExpect(jsonPath("$[0].entryDate").value("2026-10-29"))
                .andExpect(jsonPath("$[0].entryTime").value("09:15:30"))
                .andExpect(jsonPath("$[0].captureTimestamp").value("2026-10-29T09:15:30"))
                .andExpect(jsonPath("$[0].goNoDecision").value("PENDING"))
                .andExpect(jsonPath("$[0].tradeCaptureEntities").doesNotExist());
    }

    private static TradeCaptureEntity capture(StrategyEntity strategy) {
        TradeCaptureEntity entity = new TradeCaptureEntity();
        entity.setCaptureId("signal-1");
        entity.setStrategy(strategy);
        entity.setSecurity("CRUDEOILM25NOVFUT");
        entity.setInstrumentId("146796039");
        entity.setSide(TradeSide.LONG);
        entity.setLotSize(10);
        entity.setTargetQuantity(BigDecimal.TEN);
        entity.setEntryDate(LocalDate.of(2026, 10, 29));
        entity.setEntryTime(LocalTime.of(9, 15, 30));
        entity.setCalcEntry(BigDecimal.valueOf(6001));
        entity.setCalcTg(BigDecimal.valueOf(6241));
        entity.setCalcSl(BigDecimal.valueOf(5761));
        entity.setGoNoDecision(GoNoDecision.PENDING);
        entity.setCaptureTimestamp(LocalDateTime.of(2026, 10, 29, 9, 15, 30));
        return entity;
    }
}

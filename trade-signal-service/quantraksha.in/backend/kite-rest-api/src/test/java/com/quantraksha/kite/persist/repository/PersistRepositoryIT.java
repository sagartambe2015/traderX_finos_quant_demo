package com.quantraksha.kite.persist.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.quantraksha.kite.persist.GoNoDecision;
import com.quantraksha.kite.persist.StrategyEntity;
import com.quantraksha.kite.persist.TradeCaptureEntity;
import com.quantraksha.kite.persist.TradeSide;
import com.quantraksha.kite.session.KiteSessionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

/** Exercises the Spring Data repositories against the in-memory database. Each test rolls back. */
@SpringBootTest
@ActiveProfiles("it")
@Transactional
class PersistRepositoryIT {

    @Autowired
    private StrategyRepository strategies;

    @Autowired
    private TradeCaptureRepository captures;

    @MockitoBean
    private KiteSessionService kite;

    private static TradeCaptureEntity capture(String id, StrategyEntity strategy) {
        TradeCaptureEntity t = new TradeCaptureEntity();
        t.setCaptureId(id);
        t.setStrategy(strategy);
        t.setSecurity("NIFTY25NOVFUT");
        t.setSide(TradeSide.BUY);
        t.setLotSize(75);
        t.setTargetQuantity(new BigDecimal("75"));
        t.setEntryDate(LocalDate.of(2026, 10, 30));
        t.setEntryTime(LocalTime.of(9, 20));
        t.setCalcEntry(new BigDecimal("24041.0000"));
        t.setCalcTg(new BigDecimal("24342.0000"));
        t.setCalcSl(new BigDecimal("23741.0000"));
        t.setGoNoDecision(GoNoDecision.PENDING);
        return t;
    }

    @Test
    void savesAndFindsStrategies() {
        strategies.saveAndFlush(new StrategyEntity("NIFTY-HH-LL", "Nifty-FUTURE-HH-LL"));

        assertThat(strategies.findById("NIFTY-HH-LL")).isPresent();
        assertThat(strategies.findByStrategyName("Nifty-FUTURE-HH-LL")).isPresent();
        assertThat(strategies.findByIsActiveTrue())
                .extracting(StrategyEntity::getStrategyId)
                .containsExactly("NIFTY-HH-LL");
    }

    @Test
    void savesAndQueriesTradeCaptures() {
        StrategyEntity strategy = strategies.save(new StrategyEntity("CRUDE-HH-LL", "CRUDEOIL-M-FUTURE-HH-LL"));
        captures.saveAndFlush(capture("C1", strategy));

        assertThat(captures.findByStrategyEntity_StrategyId("CRUDE-HH-LL"))
                .extracting(TradeCaptureEntity::getCaptureId)
                .containsExactly("C1");
        assertThat(captures.findByGoNoDecision(GoNoDecision.PENDING)).hasSize(1);
        assertThat(captures.findByGoNoDecision(GoNoDecision.GO)).isEmpty();
        assertThat(captures.findByExitDateIsNull()).hasSize(1);
        assertThat(captures.findBySecurityAndEntryDate("NIFTY25NOVFUT", LocalDate.of(2026, 10, 30))).hasSize(1);
        assertThat(captures.findByEntryDateBetween(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .hasSize(1);
    }
}

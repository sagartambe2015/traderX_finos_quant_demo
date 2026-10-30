package com.quantraksha.kite.persist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quantraksha.kite.persist.entity.GoNoDecision;
import com.quantraksha.kite.persist.entity.StrategyEntity;
import com.quantraksha.kite.persist.entity.TradeCaptureEntity;
import com.quantraksha.kite.persist.entity.TradeSide;
import com.quantraksha.kite.core.session.KiteSessionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

/** Round-trips the JPA entities through the in-memory database. Each test rolls back. */
@SpringBootTest
@ActiveProfiles("it")
@Transactional
class TradeCapturePersistenceIT {

    @PersistenceContext
    private EntityManager em;

    @MockitoBean
    private KiteSessionService kite;

    private static TradeCaptureEntity capture(String id, StrategyEntity strategy) {
        TradeCaptureEntity t = new TradeCaptureEntity();
        t.setCaptureId(id);
        t.setStrategy(strategy);
        t.setSecurity("NIFTY25NOVFUT");
        t.setInstrumentId("15736578");
        t.setSide(TradeSide.LONG);
        t.setLotSize(75);
        t.setTargetQuantity(new BigDecimal("75"));
        t.setEntryDate(LocalDate.of(2026, 10, 30));
        t.setEntryTime(LocalTime.of(9, 20));
        t.setCalcEntry(new BigDecimal("24041.0000"));
        t.setCalcTg(new BigDecimal("24342.0000"));
        t.setCalcSl(new BigDecimal("23741.0000"));
        t.setCalcTsl(new BigDecimal("23776.0000"));
        t.setHh(new BigDecimal("24010.0000"));
        t.setLl(new BigDecimal("23805.0000"));
        t.setGoNoDecision(GoNoDecision.PENDING);
        return t;
    }

    @Test
    void savesAndReloadsAStrategyWithItsTradeCaptures() {
        StrategyEntity strategy = new StrategyEntity("NIFTY-HH-LL", "Nifty-FUTURE-HH-LL");
        strategy.getTradeCaptures().add(capture("C1", strategy));
        em.persist(strategy);
        em.flush();
        em.clear();

        StrategyEntity reloaded = em.find(StrategyEntity.class, "NIFTY-HH-LL");
        assertThat(reloaded.getStrategyName()).isEqualTo("Nifty-FUTURE-HH-LL");
        assertThat(reloaded.getIsActive()).isTrue();
        assertThat(reloaded.getCreatedDate()).isNotNull();
        assertThat(reloaded.getTradeCaptures()).singleElement().satisfies(t -> {
            assertThat(t.getCaptureId()).isEqualTo("C1");
            assertThat(t.getInstrumentId()).isEqualTo("15736578");
            assertThat(t.getSide()).isEqualTo(TradeSide.LONG);
            assertThat(t.getGoNoDecision()).isEqualTo(GoNoDecision.PENDING);
            assertThat(t.getCalcEntry()).isEqualByComparingTo("24041");
            assertThat(t.getEntryTime()).isEqualTo(LocalTime.of(9, 20));
            assertThat(t.getStrategy().getStrategyId()).isEqualTo("NIFTY-HH-LL");
        });
    }

    @Test
    void removingACaptureFromItsStrategyDeletesIt() {
        StrategyEntity strategy = new StrategyEntity("CRUDE-HH-LL", "CRUDEOIL-M-FUTURE-HH-LL");
        strategy.getTradeCaptures().add(capture("C2", strategy));
        em.persist(strategy);
        em.flush();

        strategy.getTradeCaptures().clear();
        em.flush();
        em.clear();

        assertThat(em.find(TradeCaptureEntity.class, "C2")).isNull();
    }

    @Test
    void captureWithoutRequiredFieldsIsRejected() {
        StrategyEntity strategy = new StrategyEntity("GOLDM-HH-LL", "GOLDM-M-FUTURE-HH-LL");
        em.persist(strategy);
        TradeCaptureEntity incomplete = new TradeCaptureEntity();
        incomplete.setCaptureId("C3");
        incomplete.setStrategy(strategy);

        assertThatThrownBy(() -> {
            em.persist(incomplete);
            em.flush();
        }).isInstanceOf(RuntimeException.class);
    }
}

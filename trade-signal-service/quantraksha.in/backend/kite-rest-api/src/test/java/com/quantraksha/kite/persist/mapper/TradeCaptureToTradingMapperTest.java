package com.quantraksha.kite.persist.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.quantraksha.kite.core.model.TradeCapture;
import com.quantraksha.kite.persist.entity.GoNoDecision;
import com.quantraksha.kite.persist.entity.StrategyEntity;
import com.quantraksha.kite.persist.entity.TradeCaptureEntity;
import com.quantraksha.kite.persist.entity.TradeSide;
import com.zerodhatech.models.Instrument;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TradeCaptureToTradingMapperTest {

    private final TradeCaptureToTradingMapper mapper = new TradeCaptureToTradingMapper();
    private final StrategyEntity strategy = new StrategyEntity("Nifty-FUTURE-HH-LL", "Nifty-FUTURE-HH-LL");

    private static TradeCapture capture() {
        TradeCapture c = new TradeCapture();
        c.setInstrumentToken("15736578");
        c.setEntryDate(LocalDate.of(2026, 10, 29));
        c.setCalcEntryPrice(24041);
        c.setCalcTargetPrice(24342);
        c.setCalcStopLoss(23741);
        c.setCalcTrailingStopLoss(23776);
        return c;
    }

    private static Instrument instrument() {
        Instrument i = new Instrument();
        i.instrument_token = 15736578;
        i.tradingsymbol = "NIFTY25NOVFUT";
        i.lot_size = 75;
        return i;
    }

    @Test
    void mapsCaptureAndInstrumentToAPendingLongEntity() {
        TradeCaptureEntity entity = mapper.mapToTradeCaptureEntity(capture(), strategy, instrument());

        assertThat(entity.getStrategy()).isSameAs(strategy);
        assertThat(entity.getSecurity()).isEqualTo("NIFTY25NOVFUT");
        assertThat(entity.getInstrumentId()).isEqualTo("15736578");
        assertThat(entity.getSide()).isEqualTo(TradeSide.LONG);
        assertThat(entity.getLotSize()).isEqualTo(75);
        assertThat(entity.getTargetQuantity()).isEqualByComparingTo("75");
        assertThat(entity.getCalcEntry()).isEqualByComparingTo("24041");
        assertThat(entity.getCalcTg()).isEqualByComparingTo("24342");
        assertThat(entity.getCalcSl()).isEqualByComparingTo("23741");
        assertThat(entity.getCalcTsl()).isEqualByComparingTo("23776");
        assertThat(entity.getEntryDate()).isEqualTo(LocalDate.of(2026, 10, 29));
        assertThat(entity.getEntryTime()).isNotNull();
        assertThat(entity.getGoNoDecision()).isEqualTo(GoNoDecision.PENDING);
    }

    @Test
    void everyEntityGetsItsOwnCaptureId() {
        String first = mapper.mapToTradeCaptureEntity(capture(), strategy, instrument()).getCaptureId();
        String second = mapper.mapToTradeCaptureEntity(capture(), strategy, instrument()).getCaptureId();

        assertThat(first).isNotBlank().isNotEqualTo(second);
    }
}

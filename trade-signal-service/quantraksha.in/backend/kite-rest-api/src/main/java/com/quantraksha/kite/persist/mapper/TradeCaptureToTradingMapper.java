package com.quantraksha.kite.persist.mapper;

import com.quantraksha.kite.core.model.TradeCapture;
import com.quantraksha.kite.persist.entity.GoNoDecision;
import com.quantraksha.kite.persist.entity.StrategyEntity;
import com.quantraksha.kite.persist.entity.TradeCaptureEntity;
import com.quantraksha.kite.persist.entity.TradeSide;
import com.zerodhatech.models.Instrument;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

@Component
public class TradeCaptureToTradingMapper {
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    public TradeCaptureEntity mapToTradeCaptureEntity(TradeCapture tradeCapture, StrategyEntity strategy,
                                                      Instrument instrument) {
        TradeCaptureEntity entity = new TradeCaptureEntity();
        entity.setCaptureId(UUID.randomUUID().toString());
        entity.setStrategy(strategy);
        entity.setSecurity(instrument.tradingsymbol);
        entity.setInstrumentId(tradeCapture.getInstrumentToken());
        // Every calculator produces a long breakout: entry above HH, target above entry
        entity.setSide(TradeSide.LONG);
        entity.setLotSize(instrument.lot_size);
        entity.setTargetQuantity(BigDecimal.valueOf(instrument.lot_size));

        // One lot until the calculators size positions from riskForTrade


        entity.setCalcEntry(BigDecimal.valueOf(tradeCapture.getCalcEntryPrice()));
        entity.setCalcSl(BigDecimal.valueOf(tradeCapture.getCalcStopLoss()));
        entity.setCalcTsl(BigDecimal.valueOf(tradeCapture.getCalcTrailingStopLoss()));
        entity.setCalcTg(BigDecimal.valueOf(tradeCapture.getCalcTargetPrice()));

        entity.setEntryDate(tradeCapture.getEntryDate());
        entity.setEntryTime(LocalTime.now(IST));
        entity.setGoNoDecision(GoNoDecision.PENDING);

        return entity;

    }

}

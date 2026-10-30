package com.quantraksha.kite.persist.mapper;

import com.quantraksha.kite.api.TradeCaptureController;
import com.quantraksha.kite.core.model.TradeCapture;
import com.quantraksha.kite.persist.entity.TradeCaptureEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class TradeCaptureToTradingEntity {
    private static final Logger log = LoggerFactory.getLogger(TradeCaptureToTradingEntity.class);

    public TradeCaptureEntity mapToTradeCaptureEntity(TradeCapture tradeCapture) {
        TradeCaptureEntity entity = new TradeCaptureEntity();
        entity.setInstrumentToken(tradeCapture.getInstrumentToken());
        entity.setCalcEntry(BigDecimal.valueOf(tradeCapture.getCalcEntryPrice()));
        entity.setCalcSl(BigDecimal.valueOf(tradeCapture.getCalcStopLoss()));
        entity.setCalcTsl(BigDecimal.valueOf(tradeCapture.getCalcTrailingStopLoss()));
        entity.setCalcTg(BigDecimal.valueOf(tradeCapture.getCalcTargetPrice()));

        entity.setEntryDate(tradeCapture.getEntryDate());

        return entity;

    }

}

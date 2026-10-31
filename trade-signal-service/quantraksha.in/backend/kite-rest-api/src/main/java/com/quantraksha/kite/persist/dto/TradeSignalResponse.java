package com.quantraksha.kite.persist.dto;

import com.quantraksha.kite.persist.entity.GoNoDecision;
import com.quantraksha.kite.persist.entity.TradeCaptureEntity;
import com.quantraksha.kite.persist.entity.TradeSide;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A saved signal as JSON. The entity can't be serialized directly: its strategy is a lazy proxy, loaded
 * outside any session, and links back to its captures. Only the strategy's ID is exposed, which needs no query.
 */
public record TradeSignalResponse(
        String captureId,
        String strategyId,
        String security,
        String instrumentId,
        TradeSide side,
        Integer lotSize,
        BigDecimal targetQuantity,
        LocalDate entryDate,
        LocalTime entryTime,
        LocalDate exitDate,
        LocalTime exitTime,
        BigDecimal calcEntry,
        BigDecimal calcTg,
        BigDecimal calcSl,
        BigDecimal calcTsl,
        BigDecimal actEntry,
        BigDecimal actTg,
        BigDecimal actSl,
        BigDecimal actTsl,
        BigDecimal hh,
        BigDecimal ll,
        BigDecimal riskRewardRatio,
        BigDecimal riskInPercentage,
        BigDecimal rewardInPercentage,
        GoNoDecision goNoDecision,
        LocalDateTime captureTimestamp) {

    public static TradeSignalResponse from(TradeCaptureEntity entity) {
        return new TradeSignalResponse(
                entity.getCaptureId(),
                entity.getStrategy().getStrategyId(),
                entity.getSecurity(),
                entity.getInstrumentId(),
                entity.getSide(),
                entity.getLotSize(),
                entity.getTargetQuantity(),
                entity.getEntryDate(),
                entity.getEntryTime(),
                entity.getExitDate(),
                entity.getExitTime(),
                entity.getCalcEntry(),
                entity.getCalcTg(),
                entity.getCalcSl(),
                entity.getCalcTsl(),
                entity.getActEntry(),
                entity.getActTg(),
                entity.getActSl(),
                entity.getActTsl(),
                entity.getHh(),
                entity.getLl(),
                entity.getRiskRewardRatio(),
                entity.getRiskInPercentage(),
                entity.getRewardInPercentage(),
                entity.getGoNoDecision(),
                entity.getCaptureTimestamp());
    }
}

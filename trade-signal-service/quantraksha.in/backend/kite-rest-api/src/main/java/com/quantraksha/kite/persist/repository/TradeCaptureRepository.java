package com.quantraksha.kite.persist.repository;

import com.quantraksha.kite.persist.entity.GoNoDecision;
import com.quantraksha.kite.persist.entity.TradeCaptureEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data access to the {@code trade_capture} table.
 *
 * <p>Property paths follow the entity's field names, so the strategy link is {@code strategyEntity}
 * (not the {@code getStrategy()} getter).
 */
public interface TradeCaptureRepository extends JpaRepository<TradeCaptureEntity, String> {

    List<TradeCaptureEntity> findByStrategyEntity_StrategyId(String strategyId);

    List<TradeCaptureEntity> findBySecurityAndEntryDate(String security, LocalDate entryDate);

    List<TradeCaptureEntity> findByGoNoDecision(GoNoDecision decision);

    List<TradeCaptureEntity> findByEntryDateBetween(LocalDate from, LocalDate to);

    /** Captures not yet exited, i.e. open trades. */
    List<TradeCaptureEntity> findByExitDateIsNull();

    boolean existsByEntryDateAndSecurityAndInstrumentId(
            LocalDate entryDate,
            String security,
            String instrumentId
    );

    Optional<TradeCaptureEntity> findByEntryDateAndSecurityAndInstrumentId(
            LocalDate entryDate,
            String security,
            String instrumentId
    );
}

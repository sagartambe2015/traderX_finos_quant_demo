package com.quantraksha.kite.persist.repository;

import com.quantraksha.kite.persist.StrategyEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data access to the {@code strategies} table. */
public interface StrategyRepository extends JpaRepository<StrategyEntity, String> {

    Optional<StrategyEntity> findByStrategyName(String strategyName);

    List<StrategyEntity> findByIsActiveTrue();
}

package org.twominds.bonuscheck.core.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.twominds.bonuscheck.core.domian.Strategy;
import org.twominds.bonuscheck.core.domian.StrategyType;

@Repository
public interface StrategyRepository extends JpaRepository<Strategy, Long> {
    // Можно добавить методы для поиска стратегии по типу или другим критериям
    Strategy findByStrategyType(StrategyType strategyType);
}
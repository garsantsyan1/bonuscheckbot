package org.twominds.bonuscheck.core.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.twominds.bonuscheck.core.domian.Check;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckRepository extends JpaRepository<Check, Long> {
    // Можно добавить методы для поиска чеков по различным критериям
    Check findByQrCodeData(String qrCodeData);
}
package org.twominds.bonuscheck.core.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.twominds.bonuscheck.core.domian.Seller;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByTelegramId(Long telegramId);

    boolean existsByTelegramId(Long telegramId);
}

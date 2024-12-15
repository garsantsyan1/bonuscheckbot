package org.twominds.bonuscheck.core.repositories;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.twominds.bonuscheck.core.domian.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
package org.twominds.bonuscheck.core.domian;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "checks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Check {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(name = "check_date", nullable = false)
    private LocalDateTime checkDate = LocalDateTime.now();

    @Column(name = "qr_code_data", nullable = false)
    private String qrCodeData;

    @Column(name = "product_quantity", nullable = false)
    private Integer productQuantity;

    @Column(name = "is_returned")
    private Boolean isReturned = false;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "bonus_amount")
    private BigDecimal bonusAmount;

    @Column(name = "is_bonus_paid")
    private Boolean isBonusPaid = false;


}

package org.twominds.bonuscheck.core.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.twominds.bonuscheck.core.domian.Seller;
import org.twominds.bonuscheck.core.repositories.SellerRepository;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;


    // Регистрация нового продавца
    public Seller registerSeller(Long telegramId, Seller seller) {
        if (sellerRepository.existsByTelegramId(telegramId)) {
            throw new IllegalStateException("Пользователь уже зарегистрирован.");
        }
        return sellerRepository.save(seller);
    }

    // Получение информации о продавце по Telegram ID
    public Seller getSellerByTelegramId(Long telegramId) {
        return sellerRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден."));
    }
}


package org.twominds.bonuscheck.core.services;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.twominds.bonuscheck.core.domian.Admin;
import org.twominds.bonuscheck.core.domian.Seller;
import org.twominds.bonuscheck.core.domian.Strategy;
import org.twominds.bonuscheck.core.repositories.AdminRepository;
import org.twominds.bonuscheck.core.repositories.SellerRepository;
import org.twominds.bonuscheck.core.repositories.StrategyRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {


    private final AdminRepository adminRepository;  // Репозиторий для админов
    private final SellerRepository sellerRepository;  // Репозиторий для продавцов
    private final StrategyRepository strategyRepository;  // Репозиторий для стратегий


    @Transactional
    public void registerUser(Long telegramId, String phoneNumber) {
        // Проверяем, есть ли продавец с таким telegramId
        Seller existingSeller = sellerRepository.findByTelegramId(telegramId).orElse(null);

        if (existingSeller != null) {
            // Пользователь уже зарегистрирован
            return;
        }

        // Создаем нового продавца
        Seller newSeller = new Seller();
        newSeller.setTelegramId(telegramId);
        newSeller.setPhoneNumber(phoneNumber);

        // Присваиваем стратегию продавцу (например, первую по умолчанию)
        Strategy defaultStrategy = strategyRepository.findById(1L).orElseThrow(() -> new RuntimeException("Стратегия не найдена"));
        newSeller.setStrategy(defaultStrategy);

        newSeller.setBalance(BigDecimal.ZERO);  // Начальный баланс
        sellerRepository.save(newSeller);  // Сохраняем нового продавца в базу
    }

    @Transactional
    public void addAdmin(Long telegramId, String phoneNumber, String firstName, String lastName) {
        // Добавляем нового администратора вручную
        Admin newAdmin = new Admin();
        newAdmin.setTelegramId(telegramId);
        newAdmin.setPhoneNumber(phoneNumber);
        newAdmin.setFirstName(firstName);
        newAdmin.setLastName(lastName);
        adminRepository.save(newAdmin);  // Сохраняем администратора в базу данных
    }
}

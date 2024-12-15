package org.twominds.bonuscheck.core.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.twominds.bonuscheck.core.domian.Admin;
import org.twominds.bonuscheck.core.repositories.AdminRepository;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;


    // Получение информации об администраторе по Telegram ID
    public Admin getAdminByTelegramId(Long telegramId) {
        return adminRepository.findByTelegramId(telegramId)
                .orElse(null);  // Возвращает null, если администратор не найден
    }

}
package org.twominds.bonuscheck.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.twominds.bonuscheck.telegramBot.BonusCheckBot;

@Configuration
public class BonusCheckBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(BonusCheckBot bonusCheckBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bonusCheckBot);
        return api;
    }
}

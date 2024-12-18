package org.twominds.bonuscheck.telegramBot;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.twominds.bonuscheck.core.domian.Seller;
import org.twominds.bonuscheck.core.services.AdminService;
import org.twominds.bonuscheck.core.services.SellerService;
import org.twominds.bonuscheck.core.validation.PhoneNumberValidation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class BonusCheckBot extends TelegramLongPollingBot {

    private final SellerService sellerService;
    private final PhoneNumberValidation phoneNumberValidation;
    private final Map<Long, String> userStates = new HashMap<>();


    // Конструктор, принимающий token бота из конфигурационного файла через @Value
    @Autowired
    public BonusCheckBot(@Value("${telegram.bot.token}") String botToken, SellerService sellerService, AdminService adminService, PhoneNumberValidation phoneNumberValidation) {
        super(botToken);  // Устанавливаем токен через родительский конструктор
        this.sellerService = sellerService;
        this.phoneNumberValidation = phoneNumberValidation;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userMessage = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // Проверяем состояние пользователя
            if (userStates.containsKey(chatId) && userStates.get(chatId).equals("AWAITING_PHONE")) {
                // Пользователь вводит номер телефона
                startRegistration(chatId, userMessage);
            } else {
                // Обработка команд
                switch (userMessage) {
                    case "/start":
                        showMainMenu(chatId);
                        break;
                    case "/register":
                        userStates.put(chatId, "AWAITING_PHONE"); // Устанавливаем состояние ожидания ввода номера
                        sendTextMessage(chatId, "Введите ваш номер телефона в формате +7925******* или 8912*******.");
                        break;
                    default:
                        sendTextMessage(chatId, "Неизвестная команда. Введите /start для начала работы.");
                }
            }
        }
    }


    private void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    // Метод для отображения главного меню с кнопками
    private void showMainMenu(Long telegramUserId) {
        SendMessage message = new SendMessage();
        message.setChatId(telegramUserId.toString());
        message.setText("Добро пожаловать! Для регистрации нажмите кнопку /register или напишите /register.");

        // Добавляем клавиатуру
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("/register"));

        keyboardMarkup.setKeyboard(List.of(row));
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void startRegistration(Long chatId, String userInput) {
        if (phoneNumberValidation.isPhoneNumberValid(userInput)) {
            // Проверяем, зарегистрирован ли пользователь
            if (sellerService.isSellerRegistered(chatId)) {
                sendTextMessage(chatId, "Вы уже зарегистрированы. Номер телефона: "
                        + sellerService.getSellerPhoneNumber(chatId));
            } else {
                // Регистрируем нового продавца
                Seller seller = Seller.builder()
                        .telegramId(chatId)
                        .phoneNumber(userInput)
                        .balance(BigDecimal.ZERO)
                        .strategy(null)
                        .createdAt(LocalDateTime.now())
                        .build();

                sellerService.registerSeller(chatId, seller);
                sendTextMessage(chatId, "Спасибо! Ваш номер телефона сохранен. Теперь вы можете сканировать чеки.");
            }
            userStates.remove(chatId); // Сбрасываем состояние
        } else {
            sendTextMessage(chatId, "Это не номер телефона. Пожалуйста, введите корректный номер в формате +7925******* или 8912*******.");
        }
    }


    @Override
    public String getBotUsername() {
        return "TheHIABonusCheckBot";  // Возвращаем имя бота для идентификации в Telegram
    }
}


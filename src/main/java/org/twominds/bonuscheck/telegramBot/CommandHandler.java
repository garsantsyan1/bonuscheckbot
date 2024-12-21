package org.twominds.bonuscheck.telegramBot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.twominds.bonuscheck.core.services.SellerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommandHandler {

    private final SellerService sellerService;

    public void handleCommand(Long chatId, String command, BonusCheckBot bot) {
        switch (command) {
            case "/start":
                // При старте всегда показываем меню или предложение зарегистрироваться
                showMainMenu(chatId, bot);
                break;
            case "/register":
                handleRegistrationCommand(chatId, bot);
                break;
            default:
                // Проверяем регистрацию для остальных команд
                if (!sellerService.isSellerRegistered(chatId)) {
                    bot.sendTextMessage(chatId, "Вы не зарегистрированы. Используйте команду /register для регистрации.");
                    return;
                }

                switch (command) {
                    case "📊 Показать баланс":
                        showBalance(chatId, bot);
                        break;
                    case "📷 Сканировать QR-код":
                        initiateQrScanning(chatId, bot);
                        break;
                    default:
                        bot.sendTextMessage(chatId, "Неизвестная команда. Введите /start для начала работы.");
                }
        }
    }

    void showMainMenu(Long chatId, BonusCheckBot bot) {
        if (!sellerService.isSellerRegistered(chatId)) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            // Кнопка для регистрации
            InlineKeyboardButton registerButton = new InlineKeyboardButton();
            registerButton.setText("📋 Зарегистрироваться");
            registerButton.setCallbackData("REGISTER");

            // Добавляем кнопку в разметку
            rows.add(Collections.singletonList(registerButton));
            markup.setKeyboard(rows);

            // Отправляем сообщение с кнопкой
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Вы не зарегистрированы. Для регистрации используйте команду /register или нажмите кнопку ниже.");
            message.setReplyMarkup(markup);

            try {
                bot.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }

        // Показываем меню, если пользователь зарегистрирован
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton balanceButton = new InlineKeyboardButton();
        balanceButton.setText("📊 Показать баланс");
        balanceButton.setCallbackData("SHOW_BALANCE");

        InlineKeyboardButton scanQrButton = new InlineKeyboardButton();
        scanQrButton.setText("📷 Сканировать QR-код через веб");
        scanQrButton.setUrl("https://yourdomain.com/scanner.html");

        InlineKeyboardButton uploadPhotoButton = new InlineKeyboardButton();
        uploadPhotoButton.setText("📷 Сканировать QR через фото");
        uploadPhotoButton.setCallbackData("SCAN_QR_PHOTO");

        rows.add(Collections.singletonList(balanceButton));
        rows.add(Collections.singletonList(scanQrButton));
        rows.add(Collections.singletonList(uploadPhotoButton));
        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Добро пожаловать! Выберите действие ниже.");
        message.setReplyMarkup(markup);

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void handleRegistrationCommand(Long chatId, BonusCheckBot bot) {
        if (sellerService.isSellerRegistered(chatId)) {
            bot.sendTextMessage(chatId, "Вы уже зарегистрированы.");
        } else {
            bot.sendTextMessage(chatId, "Введите ваш номер телефона в формате +7925******* или 8912*******, чтобы зарегистрироваться.");
            bot.setUserState(chatId, "AWAITING_PHONE");
        }
    }

    private void showBalance(Long chatId, BonusCheckBot bot) {
        if (sellerService.isSellerRegistered(chatId)) {
            bot.sendTextMessage(chatId, "Ваш текущий баланс: " + sellerService.getSellerBalance(chatId) + " бонусов.");
        } else {
            bot.sendTextMessage(chatId, "Вы не зарегистрированы. Используйте /register для регистрации.");
        }
    }


    private void initiateQrScanning(Long chatId, BonusCheckBot bot) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Кнопка для запуска сканера через веб
        InlineKeyboardButton scanQrButton = new InlineKeyboardButton();
        scanQrButton.setText("Открыть сканер QR");
        scanQrButton.setUrl("http://localhost:8080/scanner.html"); // Веб-страница со сканером

        // Кнопка для загрузки фото
        InlineKeyboardButton uploadPhotoButton = new InlineKeyboardButton();
        uploadPhotoButton.setText("Сканировать QR через фото");
        uploadPhotoButton.setCallbackData("SCAN_QR_PHOTO");

        // Добавляем кнопки
        rows.add(Collections.singletonList(scanQrButton));
        rows.add(Collections.singletonList(uploadPhotoButton));
        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Выберите способ сканирования QR-кода.");
        message.setReplyMarkup(markup);

        bot.sendTextMessage(chatId, message.getText());


    }
}


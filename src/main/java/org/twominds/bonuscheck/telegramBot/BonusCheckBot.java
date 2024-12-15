package org.twominds.bonuscheck.telegramBot;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.twominds.bonuscheck.core.domian.Admin;
import org.twominds.bonuscheck.core.domian.Seller;
import org.twominds.bonuscheck.core.services.AdminService;
import org.twominds.bonuscheck.core.services.SellerService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
//
//@Component
//@RequiredArgsConstructor
//public class BonusCheckBot extends TelegramLongPollingBot {
//
//    private final SellerService sellerService;
//    private final AdminService adminService;
//
//    // Конструктор, принимающий token бота из конфигурационного файла через @Value
//    @Autowired
//    public BonusCheckBot(@Value("${telegram.bot.token}") String botToken, SellerService sellerService, AdminService adminService) {
//        super(botToken);  // Устанавливаем токен через родительский конструктор
//        this.sellerService = sellerService;
//        this.adminService = adminService;
//    }
//
//    @Override
//    public void onUpdateReceived(Update update) {
//        String message = update.getMessage().getText();  // Получаем текст сообщения от пользователя
//        Long telegramId = update.getMessage().getChatId();  // Получаем Telegram ID пользователя
//
//        // Обработка различных команд
//        if (message.equals("/start")) {
//            showMainMenu(telegramId);
//        } else if (message.equals("/register")) {
//            startRegistration(update, telegramId);
//        } else if (message.equals("/balance")) {
//            showBalance(telegramId);
//        } else if (message.equals("/admin")) {
//            showAdminMenu(telegramId);
//        }
//    }
//
//    @Override
//    public String getBotUsername() {
//        return "TheHIABonusCheckBot";  // Возвращаем имя бота для идентификации в Telegram
//    }
//
//    // Метод для отображения главного меню с кнопками
//    private void showMainMenu(Long telegramId) {
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(telegramId.toString());
//        sendMessage.setText("Добро пожаловать! для регистрации нажмите кнопку /register либо напишите /register");
//
//        // Создаем клавиатуру с кнопками
//        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//        KeyboardRow row = new KeyboardRow();
//        row.add(new KeyboardButton("/register"));
// //       row.add(new KeyboardButton("/balance"));
////        row.add(new KeyboardButton("/admin"));
//        keyboardMarkup.setKeyboard(List.of(row));
//        sendMessage.setReplyMarkup(keyboardMarkup);
//
//        try {
//            execute(sendMessage);  // Отправляем сообщение с клавиатурой
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Метод для регистрации продавца
//    private void startRegistration(Update update, Long telegramId) {
//        // Регистрируем продавца по ID
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(telegramId.toString());
//        sendMessage.setText("Напишите ваш номер телефона");
//        String phoneNumber = update.getMessage().getText();
//        Seller seller = Seller.builder()
//                .telegramId(telegramId)
//                .phoneNumber(phoneNumber)
//                .balance(BigDecimal.ZERO)
//                .strategy(null)
//                .createdAt(LocalDateTime.now()).build();
//        sellerService.registerSeller(telegramId, seller);
//        sendMessage.setText("Вы успешно зарегистрированы! Ваш личный кабинет доступен через команду /balance.");
//
//        try {
//            execute(sendMessage);  // Отправляем сообщение о регистрации
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Метод для отображения баланса продавца
//    private void showBalance(Long telegramId) {
//        Seller seller = sellerService.getSellerByTelegramId(telegramId);  // Получаем информацию о продавце
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(telegramId.toString());
//        sendMessage.setText("Ваш баланс: " + seller.getBalance() + " рублей.");
//
//        try {
//            execute(sendMessage);  // Отправляем сообщение с балансом
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Метод для отображения меню администратора
//    private void showAdminMenu(Long telegramId) {
//        Admin admin = adminService.getAdminByTelegramId(telegramId);  // Проверяем, является ли пользователь администратором
//        if (admin == null) {
//            sendMessage(telegramId, "Вы не являетесь администратором.");  // Если нет, показываем ошибку
//            return;
//        }
//
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(telegramId.toString());
//        sendMessage.setText("Меню администратора: Вы можете управлять стратегиями и балансами продавцов.");
//
//        try {
//            execute(sendMessage);  // Отправляем сообщение с меню администратора
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Метод для отправки простого сообщения
//    private void sendMessage(Long telegramId, String text) {
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(telegramId.toString());
//        sendMessage.setText(text);
//
//        try {
//            execute(sendMessage);  // Отправляем сообщение
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}


@Component
@RequiredArgsConstructor
public class BonusCheckBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(BonusCheckBot.class);

    private final SellerService sellerService;
    private final AdminService adminService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String message = update.getMessage().getText();
        Long telegramId = update.getMessage().getChatId();

        switch (message) {
            case "/start" -> handleStartCommand(telegramId);
            case "/register" -> handleRegisterCommand(telegramId);
            case "/balance" -> handleBalanceCommand(telegramId);
            case "/admin" -> handleAdminCommand(telegramId);
            default -> sendMessage(telegramId, "Команда не распознана. Используйте /start для начала работы.");
        }
    }

    @Override
    public String getBotUsername() {
        return "TheHIABonusCheckBot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    // Метод для обработки команды /start
    private void handleStartCommand(Long telegramId) {
        Seller seller = sellerService.getSellerByTelegramId(telegramId);
        Admin admin = adminService.getAdminByTelegramId(telegramId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(telegramId.toString());

        if (admin != null) {
            // Если пользователь администратор
            sendMessage.setText("Добро пожаловать, администратор! Нажмите /admin для входа в меню администратора.");
        } else if (seller != null) {
            // Если пользователь зарегистрированный продавец
            sendMessage.setText("Добро пожаловать! Вы уже зарегистрированы. Нажмите /balance для просмотра вашего баланса.");
        } else {
            // Если пользователь новый
            sendMessage.setText("Добро пожаловать! Нажмите /register для регистрации.");
        }

        try {
            execute(sendMessage);
        } catch (Exception e) {
            log.error("Ошибка при обработке команды /start", e);
        }
    }

    // Метод для обработки команды /register
    private void handleRegisterCommand(Long telegramId) {
        Seller existingSeller = sellerService.getSellerByTelegramId(telegramId);
        if (existingSeller != null) {
            sendMessage(telegramId, "Вы уже зарегистрированы! Нажмите /balance для просмотра вашего баланса.");
            return;
        }

        // Регистрация нового пользователя
        Seller newSeller = Seller.builder()
                .telegramId(telegramId)
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();
        sellerService.registerSeller(telegramId, newSeller);

        sendMessage(telegramId, "Вы успешно зарегистрированы! Нажмите /balance для просмотра вашего баланса.");
    }

    // Метод для обработки команды /balance
    private void handleBalanceCommand(Long telegramId) {
        Seller seller = sellerService.getSellerByTelegramId(telegramId);
        if (seller == null) {
            sendMessage(telegramId, "Вы не зарегистрированы. Пожалуйста, используйте /register для регистрации.");
            return;
        }

        sendMessage(telegramId, "Ваш баланс: " + seller.getBalance() + " рублей.");
    }

    // Метод для обработки команды /admin
    private void handleAdminCommand(Long telegramId) {
        Admin admin = adminService.getAdminByTelegramId(telegramId);
        if (admin == null) {
            sendMessage(telegramId, "Вы не являетесь администратором.");
            return;
        }

        sendMessage(telegramId, "Добро пожаловать в меню администратора! Здесь вы можете управлять стратегиями и балансами продавцов.");
    }

    // Метод для отправки простого сообщения
    private void sendMessage(Long telegramId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(telegramId.toString());
        sendMessage.setText(text);

        try {
            execute(sendMessage);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения", e);
        }
    }
}

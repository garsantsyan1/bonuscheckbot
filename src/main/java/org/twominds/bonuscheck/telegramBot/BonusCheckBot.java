package org.twominds.bonuscheck.telegramBot;


import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.twominds.bonuscheck.core.domian.Seller;
import org.twominds.bonuscheck.core.services.SellerService;
import org.twominds.bonuscheck.core.validation.PhoneNumberValidation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class BonusCheckBot extends TelegramLongPollingBot {

    private final SellerService sellerService;
    private final PhoneNumberValidation phoneNumberValidation;
    private final CommandHandler commandHandler; // Новый класс для обработки команд


    private final Map<Long, String> userStates = new HashMap<>();

    @Autowired
    public BonusCheckBot(@Value("${telegram.bot.token}") String botToken, SellerService sellerService, PhoneNumberValidation phoneNumberValidation, CommandHandler commandHandler) {
        super(botToken);
        this.sellerService = sellerService;
        this.phoneNumberValidation = phoneNumberValidation;
        this.commandHandler = commandHandler;
    }


    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = update.getMessage() != null ? update.getMessage().getChatId() : null;

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            chatId = update.getCallbackQuery().getMessage().getChatId();

            if ("SHOW_BALANCE".equals(callbackData)) {
                if (sellerService.isSellerRegistered(chatId)) {
                    BigDecimal balance = sellerService.getSellerBalance(chatId);
                    sendTextMessage(chatId, "Ваш текущий баланс: " + balance + " бонусов.");
                } else {
                    sendTextMessage(chatId, "Вы не зарегистрированы. Используйте /register для регистрации.");
                }
            } else if ("SCAN_QR_PHOTO".equals(callbackData)) {
                setUserState(chatId, "AWAITING_QR_PHOTO");
                sendTextMessage(chatId, "Пожалуйста, загрузите фото с QR-кодом.");
            } else if ("REGISTER".equals(callbackData)) {
                commandHandler.handleCommand(chatId, "/register", this);
            }
        } else if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                String userMessage = update.getMessage().getText();
                if (userStates.containsKey(chatId) && "AWAITING_PHONE".equals(userStates.get(chatId))) {
                    startRegistration(chatId, userMessage);
                } else {
                    commandHandler.handleCommand(chatId, userMessage, this);
                }
            } else if (update.getMessage().hasPhoto()) {
                if ("AWAITING_QR_PHOTO".equals(userStates.get(chatId))) {
                    try {
                        // Проверка наличия фотографий
                        if (update.getMessage().getPhoto() == null || update.getMessage().getPhoto().isEmpty()) {
                            sendTextMessage(chatId, "Ошибка: Фото не обнаружено. Попробуйте снова.");
                            return;
                        }

                        // Получаем ID файла самого высокого качества
                        String fileId = update.getMessage()
                                .getPhoto()
                                .stream()
                                .max((p1, p2) -> Integer.compare(p1.getFileSize(), p2.getFileSize()))
                                .get()
                                .getFileId();

                        // Обрабатываем QR-код
                        String qrText = processQrFromPhoto(fileId);
                        sendTextMessage(chatId, "Содержимое QR-кода: " + qrText);

                        // Сообщаем, что можно загрузить следующий QR-код
                        sendTextMessage(chatId, "Вы можете загрузить следующий QR-код.");
                    } catch (Exception e) {
                        sendTextMessage(chatId, "Не удалось обработать QR-код. Проверьте качество изображения и повторите попытку.");
                    }
                }
            }
        }
    }


    private void startRegistration(Long chatId, String userInput) {
        if (phoneNumberValidation.isPhoneNumberValid(userInput)) {
            if (sellerService.isSellerRegistered(chatId)) {
                sendTextMessage(chatId, "Вы уже зарегистрированы. Номер телефона: " + sellerService.getSellerPhoneNumber(chatId));
            } else {
                Seller seller = Seller.builder()
                        .telegramId(chatId)
                        .phoneNumber(userInput)
                        .balance(BigDecimal.ZERO)
                        .strategy(null)
                        .createdAt(LocalDateTime.now())
                        .build();

                sellerService.registerSeller(chatId, seller);

                sendTextMessage(chatId, "Спасибо! Ваш номер телефона сохранен. Теперь вы можете использовать систему.");
                userStates.remove(chatId);

                // Показываем главное меню с кнопками
                commandHandler.showMainMenu(chatId, this);
            }
        } else {
            sendTextMessage(chatId, "Это не номер телефона. Пожалуйста, введите корректный номер.");
        }
    }


    public void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "TheHIABonusCheckBot";
    }

    public void setUserState(Long chatId, String state) {
        userStates.put(chatId, state);
    }

    private String processQrFromPhoto(String fileId) throws Exception {
        try {
            // Получение файла через Telegram API
            GetFile getFileMethod = new GetFile();
            getFileMethod.setFileId(fileId);
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFileMethod);

            String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();

            // Скачивание файла
            java.io.InputStream fileStream = new java.net.URL(fileUrl).openStream();

            // Чтение изображения
            BufferedImage originalImage = ImageIO.read(fileStream);

            // Предварительная обработка изображения
            BufferedImage processedImage = preprocessImage(originalImage);

            // Декодирование QR-кода
            LuminanceSource source = new BufferedImageLuminanceSource(processedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);

            return result.getText();
        } catch (NotFoundException e) {
            throw new Exception("QR-код не найден. Попробуйте загрузить более четкое изображение.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Произошла ошибка при обработке изображения. Проверьте формат и повторите попытку.");
        }
    }

    // Метод предварительной обработки изображения
    private BufferedImage preprocessImage(BufferedImage image) {
        try {
            // Преобразование в оттенки серого
            BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D graphics = grayImage.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();

            // Повышение контрастности
            RescaleOp rescaleOp = new RescaleOp(1.5f, 15, null);
            rescaleOp.filter(grayImage, grayImage);

            // Увеличение резкости (опционально)
            // Применение фильтра может быть реализовано через OpenCV или аналогичные библиотеки

            return grayImage;
        } catch (Exception e) {
            e.printStackTrace();
            return image; // Возврат оригинального изображения в случае ошибки
        }
    }


}





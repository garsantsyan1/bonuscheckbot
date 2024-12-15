SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 1;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 1;
SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'TRADITIONAL,ALLOW_INVALID_DATES';

-- Создаем схему с именем 'bonus_check_bot'
CREATE SCHEMA IF NOT EXISTS `bonus_check_bot` DEFAULT CHARACTER SET utf8;
USE `bonus_check_bot`;

-- Таблица для стратегий
CREATE TABLE IF NOT EXISTS `strategies`
(
    `id`                      BIGINT                                            NOT NULL AUTO_INCREMENT,
    `strategy_type`           ENUM ('weekly', 'monthly', 'per_check', 'hybrid') NOT NULL,
    `plan_threshold`          INT            DEFAULT NULL, -- Порог продаж
    `bonus_per_check`         DECIMAL(10, 2) DEFAULT NULL, -- Бонус за один чек
    `monthly_bonus_threshold` INT            DEFAULT NULL, -- Порог для месячного бонуса
    `monthly_bonus_amount`    DECIMAL(10, 2) DEFAULT NULL, -- Сумма месячного бонуса
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1002;

-- Таблица для товаров
CREATE TABLE IF NOT EXISTS `products`
(
    `id`           BIGINT         NOT NULL AUTO_INCREMENT,
    `product_name` VARCHAR(255)   NOT NULL,
    `description`  TEXT DEFAULT NULL,
    `price`        DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1002;

-- Таблица для пользователей (sellers)
CREATE TABLE IF NOT EXISTS `sellers`
(
    `id`           BIGINT      NOT NULL AUTO_INCREMENT,
    `telegram_id`  BIGINT      NOT NULL UNIQUE,
    `phone_number` VARCHAR(20) NOT NULL UNIQUE,
    `balance`      DECIMAL(10, 2) DEFAULT 0.0,  -- Баланс пользователя
    `strategy_id`  BIGINT         DEFAULT NULL, -- Стратегия, к которой привязан пользователь
    `created_at`   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`strategy_id`) REFERENCES `strategies` (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1002;

-- Индексы для улучшения производительности
CREATE UNIQUE INDEX `ix_user_telegram_phone` ON `sellers` (`telegram_id`, `phone_number`);

-- Таблица для чеков (checks)
CREATE TABLE IF NOT EXISTS `checks`
(
    `id`               BIGINT NOT NULL AUTO_INCREMENT,
    `seller_id`        BIGINT NOT NULL,                          -- ID продавца (внешний ключ к sellers)
    `check_date`       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- Дата чека
    `qr_code_data`     TEXT   NOT NULL,                          -- Данные из QR-кода
    `product_quantity` INT    NOT NULL,                          -- Количество товаров в чеке
    `is_returned`      BOOLEAN        DEFAULT FALSE,             -- Был ли товар возвращён
    `return_date`      TIMESTAMP      DEFAULT NULL,              -- Дата возврата товара
    `product_id`       BIGINT NOT NULL,                          -- ID товара
    `bonus_amount`     DECIMAL(10, 2) DEFAULT 0.0,               -- Бонус, начисленный за этот чек
    `is_bonus_paid`    BOOLEAN        DEFAULT FALSE,             -- Выплачено ли вознаграждение
    PRIMARY KEY (`id`),
    FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`id`),
    FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1002;

-- Индексы для улучшения производительности
CREATE UNIQUE INDEX `ix_checks_qr_code_data` ON `checks` (`qr_code_data`(255));
-- Указание длины для TEXT столбца

-- Таблица для администраторов
CREATE TABLE IF NOT EXISTS `admins`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,   -- Уникальный ID администратора
    `telegram_id`  BIGINT       NOT NULL UNIQUE,           -- Уникальный идентификатор администратора в Telegram
    `phone_number` VARCHAR(20)  NOT NULL UNIQUE,           -- Номер телефона администратора
    `first_name`   VARCHAR(255) NOT NULL,                  -- Имя администратора
    `last_name`    VARCHAR(255) DEFAULT NULL,              -- Фамилия администратора (необязательное поле)
    `created_at`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP, -- Дата создания записи
    PRIMARY KEY (`id`)                                     -- Уникальный ключ
) ENGINE = InnoDB
  AUTO_INCREMENT = 1002;

-- Индексы для улучшения производительности
CREATE UNIQUE INDEX `ix_admins_telegram_phone` ON `admins` (`telegram_id`, `phone_number`);

SET SQL_MODE = @OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS;

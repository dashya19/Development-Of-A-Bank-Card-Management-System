# 🏦 Система управления банковскими картами

![Java](https://img.shields.io/badge/Java-17-red.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-green.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![Liquibase](https://img.shields.io/badge/Liquibase-4.25.0-yellow.svg)
![JWT](https://img.shields.io/badge/JWT-0.11.5-orange.svg)
![Swagger](https://img.shields.io/badge/Swagger-2.2.0-lightgreen.svg)
![Lombok](https://img.shields.io/badge/Lombok-1.18.30-pink.svg)
![MapStruct](https://img.shields.io/badge/MapStruct-1.5.5-blueviolet.svg)
![Docker](https://img.shields.io/badge/Docker-20.10-lightblue.svg)

## 📌 О проекте

Современное backend-приложение для безопасного управления банковскими картами с разделением прав доступа. Система предоставляет REST API для выполнения операций с картами и обеспечивает высокий уровень безопасности данных.

## ✨ Возможности

### 🔐 Аутентификация и авторизация
- JWT-токены для безопасного доступа
- Две роли пользователей: Администратор и Пользователь
- Шифрование конфиденциальных данных

### 👨‍💻 Администратор
- Полный CRUD для управления картами
- Просмотр всех карт в системе
- Блокировка/активация карт
- Управление пользователями

### 👤 Пользователь
- Просмотр своих карт с пагинацией
- Переводы между своими картами
- Запрос на блокировку карты
- Проверка баланса

### 🛡️ Безопасность
- Маскирование номеров карт (**** **** **** 1234)
- Шифрование данных карт в БД
- Валидация всех входящих данных
- Подробные сообщения об ошибках

## 🛠 Технологии

- **Backend**: Java 17, Spring Boot 3.1
- **Безопасность**: Spring Security, JWT
- **База данных**: PostgreSQL 15
- **Миграции**: Liquibase
- **Документация**: Swagger/OpenAPI 3.0
- **Контейнеризация**: Docker, Docker Compose
- **Логирование**: SLF4J с записью в файл

## 🚀 Запуск проекта

### Требования
- Docker 20.10+
- Docker Compose 2.0+

### Инструкция по запуску
1. Создайте файл `.env` в корне проекта на основе `.env.example`
2. Запустите сервисы:
   ```bash
   docker-compose up -d

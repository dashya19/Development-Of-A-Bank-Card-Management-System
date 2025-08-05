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

## 📝 О проекте

Backend-приложение для безопасного управления банковскими картами с разделением прав доступа. Система предоставляет REST API для выполнения операций с картами и обеспечивает безопасность данных.

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
1. Команды для запуска:
   ```bash
   mvn clean package             # Сборка проекта
   docker-compose up -d          # Запуск в фоновом режиме
   docker-compose up --build     # Пересборка и запуск

2. Доступ к БД:
   ```bash
   docker exec -it bank_cards_postgres psql -U postgres -d bank_cards_db
   
## 📌 Документация API

Регистрация пользователя

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json
{
    "username": "user1",
    "password": "password1",
    "email": "user1@example.com"
}

Ответ: 200 OK

Авторизация пользователя:

POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "username": "user1",
    "password": "password1"
}

Ответ: 200 OK
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX1VTRVIiXSwic3ViIjoidXNlcjEiLCJpYXQiOjE3NTQzOTk4NDgsImV4cCI6MTc1NDQ4NjI0OH0.GxEslS-6CKT31uHe15DHo8US2ylix3cD7oU6XSdm5Is"
}

Авторизация администратора:

POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "password"
}

Ответ: 200 OK
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzU0Mzk5ODg5LCJleHAiOjE3NTQ0ODYyODl9.Nuy34K-LNrxURj8WPbFJ9z7CsiPyklLnFqUfVoK7BAM"
}

Создать новую карту (администратор создает для пользователя):

POST http://localhost:8080/api/admin/cards?userId=2
Content-Type: application/json
Authorization: Bearer <токен>

{
    "cardNumber": "1234567812345678",
    "cardHolder": "UserTwo",
    "expiryDate": "08/26"
}

Ответ: 200 OK
{
    "id": 1,
    "cardNumber": "**** **** **** 5678",
    "cardHolder": "UserTwo",
    "expiryDate": "08/26",
    "status": "ACTIVE",
    "balance": 0
}

Создать вторую новую карту (администратор создает для пользователя):

POST http://localhost:8080/api/admin/cards?userId=2
Content-Type: application/json
Authorization: Bearer <токен>

{
    "cardNumber": "2234567812345678",
    "cardHolder": "UserTwo",
    "expiryDate": "08/26"
}

Ответ: 200 OK
{
    "id": 2,
    "cardNumber": "**** **** **** 5678",
    "cardHolder": "UserTwo",
    "expiryDate": "08/26",
    "status": "ACTIVE",
    "balance": 0
}

Получить все карты (с пагинацией, запрос делает администратор):

GET http://localhost:8080/api/admin/cards?page=0&size=10
Authorization: Bearer <токен>

Ответ: 200 OK
{
    "content": [
        {
            "id": 1,
            "cardNumber": "**** **** **** 5678",
            "cardHolder": "UserTwo",
            "expiryDate": "08/26",
            "status": "ACTIVE",
            "balance": 0.00
        },
        {
            "id": 2,
            "cardNumber": "**** **** **** 5678",
            "cardHolder": "UserTwo",
            "expiryDate": "08/26",
            "status": "ACTIVE",
            "balance": 0.00
        },
        {
            "id": 3,
            "cardNumber": "**** **** **** 5678",
            "cardHolder": "UserThree",
            "expiryDate": "04/26",
            "status": "ACTIVE",
            "balance": 0.00
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 3,
        "sort": {
            "sorted": false,
            "unsorted": true,
            "empty": true
        },
        "offset": 0,
        "unpaged": false,
        "paged": true
    },
    "totalPages": 2,
    "totalElements": 4,
    "last": false,
    "numberOfElements": 3,
    "size": 3,
    "number": 0,
    "sort": {
        "sorted": false,
        "unsorted": true,
        "empty": true
    },
    "first": true,
    "empty": false
}

Получить все карты пользователя (без пагинации, запрос делает администратор):

GET http://localhost:8080/api/admin/cards
Authorization: Bearer <токен>

Ответ: 200 OK
{
    "content": [
        {
            "id": 1,
            "cardNumber": "**** **** **** 5678",
            "cardHolder": "UserTwo",
            "expiryDate": "08/26",
            "status": "ACTIVE",
            "balance": 0.00
        },
        {
            "id": 2,
            "cardNumber": "**** **** **** 5678",
            "cardHolder": "UserTwo",
            "expiryDate": "08/26",
            "status": "ACTIVE",
            "balance": 0.00
        },
        {
            "id": 3,
            "cardNumber": "**** **** **** 5678",
            "cardHolder": "UserThree",
            "expiryDate": "04/26",
            "status": "ACTIVE",
            "balance": 0.00
        },
        {
            "id": 4,
            "cardNumber": "**** **** **** 5678",
            "cardHolder": "UserThree",
            "expiryDate": "09/26",
            "status": "ACTIVE",
            "balance": 0.00
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 20,
        "sort": {
            "sorted": false,
            "unsorted": true,
            "empty": true
        },
        "offset": 0,
        "unpaged": false,
        "paged": true
    },
    "totalPages": 1,
    "totalElements": 4,
    "last": true,
    "numberOfElements": 4,
    "size": 20,
    "number": 0,
    "sort": {
        "sorted": false,
        "unsorted": true,
        "empty": true
    },
    "first": true,
    "empty": false
}

Получить конкретную карту (запрос делает администратор):

GET http://localhost:8080/api/cards/1
Authorization: Bearer <токен>

Ответ: 200 OK
{
    "id": 1,
    "cardNumber": "**** **** **** 5678",
    "cardHolder": "UserTwo",
    "expiryDate": "08/26",
    "status": "ACTIVE",
    "balance": 0.00
}

Получить все карты пользователя (без пагинации, запрос делает пользователь):

GET http://localhost:8080/api/cards/all
Authorization: Bearer <токен>

Ответ: 200 OK
[
    {
        "id": 1,
        "cardNumber": "**** **** **** 5678",
        "cardHolder": "UserTwo",
        "expiryDate": "08/26",
        "status": "ACTIVE",
        "balance": 0.00
    },
    {
        "id": 2,
        "cardNumber": "**** **** **** 5678",
        "cardHolder": "UserTwo",
        "expiryDate": "08/26",
        "status": "ACTIVE",
        "balance": 0.00
    }
]

Получить конкретную карту:

GET http://localhost:8080/api/cards/1
Authorization: Bearer <токен>

Ответ: 200 OK
{
    "id": 1,
    "cardNumber": "**** **** **** 5678",
    "cardHolder": "UserTwo",
    "expiryDate": "08/26",
    "status": "ACTIVE",
    "balance": 0.00
}

Пополним первую карту (делает пользователь):

POST http://localhost:8080/api/cards/top-up
Content-Type: application/json
Authorization: Bearer <user_токен>

{
    "cardId": 1,
    "amount": 2000.00
}

Ответ: 200 OK
{
    "id": 1,
    "cardNumber": "**** **** **** 5678",
    "cardHolder": "UserTwo",
    "expiryDate": "08/26",
    "status": "ACTIVE",
    "balance": 2000.00
}

Переведём средства между картами (делает пользователь):

POST http://localhost:8080/api/cards/transfer
Content-Type: application/json
Authorization: Bearer <user_токен>

{
    "fromCardId": 1,
    "toCardId": 2,
    "amount": 500.00
}

Ответ: 200 OK

Получить все карты пользователя для проверки перевода денежных средств между картами (без пагинации, запрос делает пользователь):

GET http://localhost:8080/api/cards/all
Authorization: Bearer <токен>

Ответ: 200 OK
[
    {
        "id": 1,
        "cardNumber": "**** **** **** 5678",
        "cardHolder": "UserTwo",
        "expiryDate": "08/26",
        "status": "ACTIVE",
        "balance": 1500.00
    },
    {
        "id": 2,
        "cardNumber": "**** **** **** 5678",
        "cardHolder": "UserTwo",
        "expiryDate": "08/26",
        "status": "ACTIVE",
        "balance": 500.00
    }

Заблокировать карту (запрос на блокировку, запрос пользователя):

POST http://localhost:8080/api/cards/request-block/1
Authorization: Bearer <токен>

Ответ: 200 OK

Заблокировать карту (запрос делает администратор):

PUT http://localhost:8080/api/admin/cards/1/block
Authorization: Bearer <токен>

Ответ: 200 OK
{
    "id": 1,
    "cardNumber": "**** **** **** 5678",
    "cardHolder": "UserTwo",
    "expiryDate": "08/26",
    "status": "BLOCKED",
    "balance": 0.00
}

Активировать карту (запрос делает администратор):

PUT http://localhost:8080/api/admin/cards/1/activate
Authorization: Bearer <токен>

Ответ: 200 OK
{
    "id": 1,
    "cardNumber": "**** **** **** 5678",
    "cardHolder": "UserTwo",
    "expiryDate": "08/26",
    "status": "ACTIVE",
    "balance": 0.00
}

Удалить карту (запрос делает администратор):

DELETE http://localhost:8080/api/admin/cards/1
Authorization: Bearer <токен>

Ответ: 204 No Content

Получить всех пользователей (запрос делает администратор):

GET http://localhost:8080/api/admin/users
Authorization: Bearer <токен>

Ответ: 200 OK
[
    {
        "id": 1,
        "username": "admin",
        "password": "$2a$10$hU8VQ0KdcP9R/9s/oAxwpu5Q9fFKN4lFWJpp8arx9XFowy9Pk2Wry",
        "email": "admin@example.com",
        "createdAt": "2025-08-05T13:15:22.581923",
        "roles": [
            {
                "id": 1,
                "name": "ROLE_ADMIN",
                "authority": "ROLE_ADMIN"
            }
        ],
        "cards": [],
        "authorities": [
            {
                "id": 1,
                "name": "ROLE_ADMIN",
                "authority": "ROLE_ADMIN"
            }
        ],
        "accountNonLocked": true,
        "credentialsNonExpired": true,
        "accountNonExpired": true,
        "enabled": true
    },
    {
        "id": 2,
        "username": "user1",
        "password": "$2a$10$WckJopvN3CYnHwtCn5HMHeriFTh6XAcifT5UyCg06I3O6NUFBaR06",
        "email": "user1@example.com",
        "createdAt": "2025-08-05T13:15:51.595601",
        "roles": [
            {
                "id": 2,
                "name": "ROLE_USER",
                "authority": "ROLE_USER"
            }
        ],
        "cards": [],
        "authorities": [
            {
                "id": 2,
                "name": "ROLE_USER",
                "authority": "ROLE_USER"
            }
        ],
        "accountNonLocked": true,
        "credentialsNonExpired": true,
        "accountNonExpired": true,
        "enabled": true
    },
    {
        "id": 3,
        "username": "user2",
        "password": "$2a$10$A1nYxSVaN5FLTDok5iZlA.azxHBJeDoQ6ssyjV3cfm0e27NEVD09a",
        "email": "user2@example.com",
        "createdAt": "2025-08-05T13:16:27.217116",
        "roles": [
            {
                "id": 2,
                "name": "ROLE_USER",
                "authority": "ROLE_USER"
            }
        ],
        "cards": [],
        "authorities": [
            {
                "id": 2,
                "name": "ROLE_USER",
                "authority": "ROLE_USER"
            }
        ],
        "accountNonLocked": true,
        "credentialsNonExpired": true,
        "accountNonExpired": true,
        "enabled": true
    },
    {
        "id": 4,
        "username": "user3",
        "password": "$2a$10$Vqi0zPVef2s3f3D7WcwM8.SGt9GTQCYG1vUq77qyBP3HZptzxwiOi",
        "email": "user3@example.com",
        "createdAt": "2025-08-05T13:16:31.9942",
        "roles": [
            {
                "id": 2,
                "name": "ROLE_USER",
                "authority": "ROLE_USER"
            }
        ],
        "cards": [],
        "authorities": [
            {
                "id": 2,
                "name": "ROLE_USER",
                "authority": "ROLE_USER"
            }
        ],
        "accountNonLocked": true,
        "credentialsNonExpired": true,
        "accountNonExpired": true,
        "enabled": true
    },
    {
        "id": 5,
        "username": "user4",
        "password": "$2a$10$/vNG74wjmg.yNerZ2KCvnOcogih8fAYB8VjOGBKX5rcwA62Gh7iTu",
        "email": "user4@example.com",
        "createdAt": "2025-08-05T13:16:36.382257",
        "roles": [
            {
                "id": 2,
                "name": "ROLE_USER",
                "authority": "ROLE_USER"
            }
        ],
        "cards": [],
        "authorities": [
            {
                "id": 2,
                "name": "ROLE_USER",
                "authority": "ROLE_USER"
            }
        ],
        "accountNonLocked": true,
        "credentialsNonExpired": true,
        "accountNonExpired": true,
        "enabled": true
    },
    {
        "id": 6,
        "username": "user5",
        "password": "$2a$10$b..oubPQ8PXZS/tjg1Wdq.34Z76k5pVueK0gMjbQvynazmdFqJTLa",
        "email": "user5@example.com",
        "createdAt": "2025-08-05T13:16:42.888163",
        "roles": [
            {
                "id": 2,
                "name": "ROLE_USER",
                "authority": "ROLE_USER"
            }
        ],
        "cards": [],
        "authorities": [
            {
                "id": 2,
                "name": "ROLE_USER",
                "authority": "ROLE_USER"
            }
        ],
        "accountNonLocked": true,
        "credentialsNonExpired": true,
        "accountNonExpired": true,
        "enabled": true
    }
]

Получить конкретного пользователя (запрос делает администратор):

GET http://localhost:8080/api/admin/users/1
Authorization: Bearer <токен>

Ответ: 200 OK
{
    "id": 1,
    "username": "admin",
    "password": "$2a$10$hU8VQ0KdcP9R/9s/oAxwpu5Q9fFKN4lFWJpp8arx9XFowy9Pk2Wry",
    "email": "admin@example.com",
    "createdAt": "2025-08-05T13:15:22.581923",
    "roles": [
        {
            "id": 1,
            "name": "ROLE_ADMIN",
            "authority": "ROLE_ADMIN"
        }
    ],
    "cards": [],
    "authorities": [
        {
            "id": 1,
            "name": "ROLE_ADMIN",
            "authority": "ROLE_ADMIN"
        }
    ],
    "accountNonLocked": true,
    "credentialsNonExpired": true,
    "accountNonExpired": true,
    "enabled": true
}

   


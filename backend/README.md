# Backend в Scrollic

Здесь представлена кратка сводка по устройству backend'а, более подробную информацию по компонентам можно найти в соответствующих директориях.

---
# Описание микросервисов

| Название | Язык | Назначение |
|----------|------|------------|
| APIGateway | Java (Spring Cloud Gateway) | Единая точка входа, аутентификация, rate limiting, маршрутизация |
| UserService | Python (FastAPI) | Управление пользователями и темами |
| FeedService | Python (FastAPI) | Формирование ленты новостей на основе пользовательских весов |
| ActionService | Python (FastAPI) | Обработка действий пользователя, расчет весов пользователя | 
| NewsFetcherService | Java (Spring Boot) | Опрос внешней API, получение новостей, постановка новостей в очередь на тегирование |
| NewsHandlerService | Java (Spring Boot) | Чтение новостей из очереди и их многопоточная обработка |

---
# Работа с данными

| Инструмент | Роль | Какие микросервисы взаимодействуют | Почему выбран |
|------------|------|------------------------------------|---------------|
| Cassandra | Основное хранилище. Хранит новости и действия пользователей. | NewsHandlerService заполняет новостями <br>-> FeedService поставляет новости клиенту <br>-> ActionService записывает действия и веса пользователя | Сумасшедшая скорость на запись, хорошая на чтение, отказоустойчивость, горизонтальное масштабирование |
| PostgreSQL | База данных пользователей и тегов | UserService записывает новых пользователей | Изначально был основной БД, но после перехода на Cassandra остался как надежный справочник |
| Redis | Сессии, rate limiting, cache | При логине UserService записывает токен <br>-> APIGateway проводит валидацию токена и rate limiting | Молниеносная скорость доступа в памяти, автоудаления устаревших записей |
| Kafka | Асинхронная очередь новостей на обработку | NewsFetcherService производит записи <br>-> NewsHandlerService потребляет | Огромная пропускная способность, нет нужды в гибкой маршрутизации |

---
# Диаграмма компонентов

![Low Level Design](../arch/low_level_design.svg)

---
# Карта сервисов в docker compose

| Сервис | Имя контейнера | Порт наружу | Порт в контейнере |
|--------|----------------|-------------|-------------------|
| API Gateway | `scrollic-api-gateway` | `${GATEWAY_PORT}` | 8080 |
| UserService | `scrollic-user-service` | `${USER_SERVICE_PORT}` | 8001 |
| Cassandra | `scrollic-cassandra` | `${CASSANDRA_PORT}` | 9042 |
| PostgreSQL | `scrollic-postgres` | `${POSTGRES_PORT}` | 5432 |
| Redis | `scrollic-redis` | `${REDIS_PORT}` | 6379 |
| Kafka | `scrollic-kafka` | `${KAFKA_PORT}` | 9092 |
| KafkaUI | `scrollic-kafka-ui` | `${KAFKA_UI_PORT}` | 8080 |
| Adminer | `scrollic-adminer` | 8081 | 8080 |

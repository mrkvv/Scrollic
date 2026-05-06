# NewsHandlerService

Микросервис на Java Spring Boot, отвечающий за тегирование новостей и сохранение их в Cassandra.

## Технологический стек
| Технология | Назначение |
|------------|------------|
| Java 17 | Язык программирования |
| Spring Boot 4.0.5 | Основа микросервиса |
| Spring Kafka | Стартер для работы с Apache Kafka |
| Spring Data Cassandra Reactive | Стартер для реактивной работы с Apache Cassandra |
| Jackson | Сериализация/десериализация JSON |

## Особенности
### 1. Многопоточная обработка сообщений
- Отдельный поток слушатель для каждой партиции топика Kafka
- Ручное управление offset'ами

### 2. Реактивная работа с Cassandra
- Асинхронная запись во все таблицы через `Mono.when()`
- Высокая пропускная способность за счёт неблокирующего I/O

### 3. Идемпотентность на уровне БД
- UUID новости генерируется из URL (MD5-хеш), который одинаков для одинаковых URL


## Структура проекта
```
NewsHandlerService/
├── build.gradle.kts
├── Dockerfile
└── src/main/
    ├── java/scrollic/news_handler_service/
    │   ├── config/
    │   │   └── KafkaConfig.java           # Топик, полная конфигурация Kafka Consumer'а
    │   ├── consumer/
    │   │   └── NewsConsumer.java          # @KafkaListener, приём сообщений
    │   ├── dto/
    │   │   └── NewsArticle.java           # DTO для получения новости из Kafka
    │   ├── entity/                        # Сущности для записи в таблицы Cassandra
    │   │   ├── NewsByDateEntity.java
    │   │   ├── NewsByThemeAndPopularityEntity.java
    │   │   └── NewsEntity.java
    │   ├── repository/                    # Реактивные репозитории Cassandra
    │   │   ├── NewsByDateRepository.java
    │   │   ├── NewsByThemeAndPopularityRepository.java
    │   │   └── NewsRepository.java
    │   ├── service/
    │   │   ├── NewsProcessorService.java  # Маппинг DTO -> Entity, тегирование, сохранение
    │   └── NewsHandlerServiceApplication.java
    └── resources/
        └── application.yaml
```

## Конфигурация (application.yaml)
```yaml
spring:
  kafka:
    topic:
      news: ${KAFKA_NEWS_TOPIC_NAME:news-topic}         # Название топика в Kafka
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: news-handler-group
  cassandra:
    contact-points: ${CASSANDRA_CONTACT_POINTS:localhost}
    port: ${CASSANDRA_PORT:9042}
    local-datacenter: ${CASSANDRA_DC:datacenter1}
    keyspace-name: ${CASSANDRA_KEYSPACE:scrollic}
```

## Переменные окружения (.env)
| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `KAFKA_NEWS_TOPIC_NAME` | Название топика, откуда принимать новости | `news-topic` |
| `KAFKA_BOOTSTRAP_SERVERS` | Адрес Kafka | prod - `kafka:9094`, dev - `localhost:9092` |
| `CASSANDRA_CONTACT_POINTS` | Адрес Cassandra | prod - `cassandra`, dev - `localhost` |
| `CASSANDRA_PORT` | Порт Cassandra | `9042` |
| `CASSANDRA_DC` | Название ЦОД Cassandra | `datacenter1` |
| `CASSANDRA_KEYSPACE` | Пространство ключей в Cassandra | `scrollic` |

## TODO / Идеи для развития
- Добавление Dead Letter Queue для битых новостей
- Circuit Breaker для Cassandra

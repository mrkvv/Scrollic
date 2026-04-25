# NewsFetcherService

Микросервис на Java Spring Boot, отвечающий за получение новостей из внешнего API (GNews) и публикацию их в Kafka для дальнейшей обработки.

## Технологический стек
| Технология | Назначение |
|------------|------------|
| Java 17 | Язык программирования |
| Spring Boot 4.0.5 | Основа микросервиса |
| Spring Kafka | Стартер для работы с Apache Kafka |
| Spring WebFlux (WebClient) | Реактивный HTTP-клиент для опроса внешней API |
| Jackson | Сериализация/десериализация JSON |

## Особенности
### 1. Применение шаблона Retry (Short Polling)
- Фильтрация retryable ошибок (5xx, 429, сетевые ошибки)
- Настраиваемые параметры для количества попыток и ожидания

### 2. Scheduling
- Приложение работает по расписанию
- Запуск пайплайна раз в час (опросить GNews по всем категориям -> опубликовать в Kafka)
- Очистка кеша каждый день в 3 часа ночи

### 3. Дедупликация новостей
- Сохраняет url'ы новостей в in-memory кеш (`ConcurrentHashMap.newKeySet()`)
- Если при обработке новости, её url уже встречался, то дальше новость не проходит
- Кеш автоматически очищается раз в сутки

### 4. Идемпотентный Kafka producer
- `enable.idempotence: true` - гарантия exactly-once доставки
- Ключ сообщения - url новости


## Конфигурация (application.yaml)
```
spring:
  kafka:
    topic:
      news: ${KAFKA_NEWS_TOPIC_NAME:news-topic}   # Название топика в Kafka 
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JacksonJsonSerializer
      properties:
        enable.idempotence: true            # Идемпотентный Producer

news:
  fetch-interval: ${FETCHING_INTERVAL_MS}   # Время в мс между запусками пайплайна
  apikey: ${GNEWS_APIKEY}
  retry:
    max-attempts: 5                         # Максимальное количество попыток retry
    initial-delay-ms: 10000                 # Начальное время ожидания (будет экспоненциально расти с каждой попыткой)
```

## Переменные окружения (.env)
| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `KAFKA_NEWS_TOPIC_NAME` | Название топика, куда отсылать новости | `news-topic` |
| `KAFKA_BOOTSTRAP_SERVERS` | Адрес Kafka | prod - `kafka:9094`, dev - `localhost:9092` |
| `FETCHING_INTERVAL_MS` | Интервал опроса в мс | `3600000` |
| `GNEWS_APIKEY` | API ключ GNews | - |






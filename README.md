# Мобильное приложение "Лента новостей Scrollic"

Это проект в рамках учебной дисциплины "Архитектура программных систем". Его основная задача - решение возникающих архитектурных вопросов. Второстепенная задача - разработка мобильного приложения ленты новостей Scrollic.

---
# Команда проекта

|  | Участник | GitHub |
|----------|------|--------|
| <img src="https://github.com/ArtemAbrosimov1.png" width="50" height="50" style="border-radius: 50%;">  | **Абросимов Артем Дмитриевич** <br> Mobile | [![GitHub](https://img.shields.io/badge/-ArtemAbrosimov1-181717?style=flat&logo=github)](https://github.com/ArtemAbrosimov1) |
| <img src="https://github.com/kirillbaykin.png" width="50" height="50" style="border-radius: 50%;">  | **Байкин Кирилл Александрович** <br> Backend | [![GitHub](https://img.shields.io/badge/-kirillbaykin-181717?style=flat&logo=github)](https://github.com/kirillbaykin) |
| <img src="https://github.com/polinapup.png" width="50" height="50" style="border-radius: 50%;">  | **Калашникова Полина Олеговна** <br> TeamLead / Arch / Design | [![GitHub](https://img.shields.io/badge/-polinapup-181717?style=flat&logo=github)](https://github.com/polinapup) |
| <img src="https://github.com/mrkvv.png" width="50" height="50" style="border-radius: 50%;">  | **Марков Леонид Александрович** <br> DevOps | [![GitHub](https://img.shields.io/badge/-mrkvv-181717?style=flat&logo=github)](https://github.com/mrkvv) |


---
# Технологический стек

| Технология | Назначение |
|------------|------------|
| [![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org) | Основной язык мобильного приложения |
| [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.10.4-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose) | UI фреймворк для Android |
| [![Python](https://img.shields.io/badge/Python-3.14-3776AB?logo=python&logoColor=white)](https://www.python.org) | Язык бэкенда |
| [![FastAPI](https://img.shields.io/badge/FastAPI-0.134.0-009688?logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com) | Бэкенд фреймворк |
| [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org) | Основная база данных |
| [![Redis](https://img.shields.io/badge/Redis-8.0-DC382D?logo=redis&logoColor=white)](https://redis.io) | Кэширование и очереди |
| [![Docker](https://img.shields.io/badge/Docker-29.2.1-2496ED?logo=docker&logoColor=white)](https://docker.com) | Контейнеризация |
| [![Docker Compose](https://img.shields.io/badge/Docker%20Compose-5.0.2-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/) | Оркестрация контейнеров |
| [![Figma](https://img.shields.io/badge/Figma-126.0.4-A259FF?logo=figma&logoColor=white)](https://www.figma.com) | Дизайн |

---
# Подъем docker compose
Как окружение для подъема рекоменду WSL + Docker Desktop. Устанавливаете WSL, устанавливаете Docker Desktop, врубаете галочку на интеграцию с вашей WSL и готово!

1. В WSL перейти в каталог проекта `/Scrollic`.
2. Выполнить команду `docker compose up -d`. Первый запуск займет время, последующие будут быстрее.
3. Для проверки воспользоваться `docker ps` и (или) `docker compose logs <название сервиса>`.

Для просмотра состояния БД открыть в браузере ссылку `http://localhost:8081`.

Для остановки контейнеров используйте `docker compose down` в корне проекта. Если хотите очистить все хранилища (PostgreSQL, Cassandra, Redis), то добавьте флаг `-v`. Если хотите очистить конкретное хранилище, то после `-v` укажите название сервиса.

К пункту 2: Если вы изменили что-то в backend'е, то добавьте флаг для пересборки образов `docker compose up -d --build` - тоже займет время.

---
# Карта сервисов
| Сервис | Имя контейнера | Порт наружу | Порт в контейнере |
|--------|----------------|-------------|------------------------|
| API Gateway | `scrollic-api-gateway` | `${GATEWAY_PORT}` | 8080 |
| Test Service | `scrollic-test-service` | `${TEST_SERVICE_PORT}` | 8090 |
| PostgreSQL | `scrollic-postgres` | `${POSTGRES_PORT}` | 5432 |
| Redis | `scrollic-redis` | `${REDIS_PORT}` | 6379 |
| Cassandra | `scrollic-cassandra` | `${CASSANDRA_PORT}` | 9042 |
| Adminer | `scrollic-adminer` | 8081 | 8080 |

---
# Работа с ветками
1) Перед началом работы переходим в ветку main и качаем ее:
```
git switch main
git pull
```
2) Для КАЖДОЙ задачи у нас отдельная ветка, начинающаяся с feature/: `git checkout -b feature/<название_ветки>`
3) Работаем в этой ветке: `git add | git commit | git push`. Когда делаем коммит не забываем про Commit Conventions!
4) Когда нам нужно сделать PR (или просто есть необходимость) - синхронизируемся с main веткой:
```
git checkout feature/<название_ветки>
git fetch origin main
git rebase origin/main
(если будут конфликты, решаете их. Очень удобно открыть проект в Android Studio и разрешать конфликты ресурсами IDE)
git add <..>
git rebase --continue
git push -u origin feature/<название_ветки> --force-with-lease
```
5) Создаем сам PR: `Contribute` -> `Open pull request` -> оформляем его -> Выбираем либо `create pull request`, если это финальная версия, либо `create draft pull request`, если еще будут какие-то доработки.
После создания линкуем PR с его таской через раздел `Development`.
(опционально) назначаем `Reviewers`
6) Если вы принимаете PR: после аппрува кода делаете НЕ просто `MERGE`, а `SQUASH + MERGE`! Поля, которые вы заполните, будут описанием всех изменений этого PR, и внесутся в main как обычный ОДИН коммит.
7) Созданная ветка удаляется спустя какое-то время, после вливания PR, через:
```
git checkout main
git push origin --delete feature/<название_ветки>
git branch -D feature/<название_ветки>
```

### Возможные проблемы
- после `git push` гитхаб не знает куда влить ваши изменения - укажите явно `git push -u origin feature/<название_ветки>`. На force пуши это тоже работает.





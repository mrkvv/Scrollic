# Scrollic

---
# Подъем docker compose
Как окружение для подъема рекоменду WSL + Docker Desktop. Устанавливаете WSL, устанавливаете Docker Desktop, врубаете галочку на интеграцию с вашей WSL и готово!

1. В WSL перейти в каталог проекта `/Scrollic`.
2. Выполнить команду `docker compose up -d --build`.
3. Для проверки воспользоваться `docker ps` и (или) `docker compose logs`.

Для просмотра состояния БД открыть в браузере ссылку `http://localhost:8081`.
# GSM Tracker

Учебный backend GPS/GSM-трекера для автомобиля.

Старый Android-телефон в машине шлёт координаты (через приложение
[GPSLogger](https://github.com/mendhak/gpslogger)) на этот сервис по REST.
Точки хранятся в PostgreSQL, трек и последняя позиция показываются на карте
(Leaflet + OpenStreetMap), которую отдаёт сам Spring Boot.

## Стек

- Java 21, Spring Boot 3.3 (Web, Data JPA, Validation, Actuator)
- PostgreSQL 16, миграции через Flyway
- Сборка: Maven
- Фронтенд: статический `index.html` с Leaflet (отдаётся из `static/`)

## Архитектура

```
GPSLogger (телефон) ──POST /api/v1/positions──► Spring Boot ──► PostgreSQL
Браузер ──GET /──────────────────────────────► index.html (Leaflet)
        ──GET /api/v1/devices/{id}/track─────► GeoJSON ──► трек на карте
```

## Запуск (локально)

1. Поднять базу:

```bash
docker compose up -d postgres
```

2. Запустить приложение (профиль `dev` активен по умолчанию):

```bash
mvn spring-boot:run
```

3. Открыть карту: http://localhost:8080/
4. Health-check: http://localhost:8080/actuator/health

Миграции Flyway применяются автоматически при старте. Профиль `dev` создаёт
демо-устройство (см. `V3__seed_demo_device.sql`):

- `deviceId = 1`
- `token = demo-token-123`

## Публичный доступ для телефона (этап разработки)

```bash
cloudflared tunnel --url http://localhost:8080
```

Полученный `https://<...>.trycloudflare.com` вписать в GPSLogger.

## Настройка GPSLogger

Логирование на пользовательский URL:

- URL: `https://<host>/api/v1/positions`
- Метод: `POST`, тип `application/json`
- Заголовок: `Authorization: Bearer demo-token-123`
- Тело:

```json
{"recordedAt":"%TIME","lat":%LAT,"lon":%LON,"speed":%SPD,"accuracy":%ACC,"altitude":%ALT,"bearing":%DIR,"battery":%BATT}
```

## REST API

| Метод | Путь | Описание | Авторизация |
|---|---|---|---|
| POST | `/api/v1/positions` | Принять одну точку | Bearer |
| POST | `/api/v1/positions/batch` | Принять пачку точек | Bearer |
| GET | `/api/v1/devices` | Список устройств | — |
| GET | `/api/v1/devices/{id}/positions/latest` | Последняя позиция | — |
| GET | `/api/v1/devices/{id}/positions?from=&to=&limit=` | История точек | — |
| GET | `/api/v1/devices/{id}/track?from=&to=` | Трек в GeoJSON | — |

`from` / `to` — ISO-8601 (например `2026-07-21T00:00:00Z`). По умолчанию — последние 24 часа.

## Профили

- `dev` (по умолчанию) — локальный PostgreSQL из `docker-compose.yml`, подробный SQL-лог, seed демо-устройства.
- `prod` — параметры БД из переменных окружения (`.env`), без seed. Активируется `SPRING_PROFILES_ACTIVE=prod`.

## Деплой на VPS (cloud.ru)

Продакшн-стек: Caddy (авто-TLS) -> Spring Boot (профиль `prod`) -> PostgreSQL.
Файлы: `Dockerfile`, `docker-compose.prod.yml`, `Caddyfile`, `.env.example`.

### 1. Создать ВМ
- Образ: Ubuntu 22.04 LTS; 2 vCPU / 4 ГБ RAM / ~30 ГБ диск.
- Добавить свой SSH-ключ.
- Security group (firewall): открыть входящие **22 (SSH), 80 (HTTP), 443 (HTTPS)**.
- Запомнить публичный IP.

### 2. Домен (DuckDNS)
- На duckdns.org создать поддомен, напр. `gsm-xxx.duckdns.org`.
- Прописать его A-запись на публичный IP ВМ.

### 3. Установить Docker на ВМ
```bash
curl -fsSL https://get.docker.com | sh
```

### 4. Доставить код и запустить
```bash
git clone <repo-url> gsm-tracker && cd gsm-tracker   # или scp папку проекта
cp .env.example .env && nano .env                    # задать SITE_ADDRESS и пароль БД
docker compose -f docker-compose.prod.yml up -d --build
```

### 5. Создать устройство и получить токен
Профиль `prod` не создаёт демо-устройство. Добавить своё:
```bash
docker exec -it gsm-postgres psql -U gsm -d gsmtracker \
  -c "INSERT INTO device (name, token) VALUES ('Моя машина', 'ПРИДУМАЙ-ДЛИННЫЙ-ТОКЕН') RETURNING id;"
```

### 6. Проверить и настроить телефон
- Карта: `https://gsm-xxx.duckdns.org/`
- В GPSLogger URL: `https://gsm-xxx.duckdns.org/api/v1/positions`, заголовок `Authorization: Bearer <ваш токен>`.

> Кредиты cloud.ru ограничены и по сумме, и по сроку — не забудьте остановить/удалить ВМ (`docker compose -f docker-compose.prod.yml down`), когда закончите эксперименты.

## Статус (скелет)

Реализован сквозной каркас: приём точек, дедупликация по `(device_id, recorded_at)`,
выдача истории/трека, карта. Отложено (см. комментарии в коде): полноценный Spring Security,
хэширование токенов, rate-limiting, пагинация, gRPC-транспорт.

-- Dev-only seed: demo positions for device 1 that form THREE trips.
-- Anchored to CURRENT_DATE so the data falls into the default "last 24h" window.
-- Trips are separated by pauses > 5 min (parking); inside trip 2 there is a
-- short traffic-light stop (speed 0, < gap) and one noisy point (accuracy 90).
-- speed is in m/s (GPSLogger %SPD); accuracy/altitude in meters.

INSERT INTO position (device_id, lat, lon, speed, accuracy, altitude, bearing, battery, recorded_at)
VALUES
    -- ===== Trip 1: 10:00–10:12 =====
    (1, 55.7500, 37.6100,  0.0,  8, 150, NULL, 95, (CURRENT_DATE + TIME '10:00:00') AT TIME ZONE 'UTC'),
    (1, 55.7520, 37.6135, 12.0,  6, 150, NULL, 95, (CURRENT_DATE + TIME '10:02:00') AT TIME ZONE 'UTC'),
    (1, 55.7545, 37.6172, 14.0,  5, 151, NULL, 94, (CURRENT_DATE + TIME '10:04:00') AT TIME ZONE 'UTC'),
    (1, 55.7568, 37.6210, 13.0,  6, 151, NULL, 94, (CURRENT_DATE + TIME '10:06:00') AT TIME ZONE 'UTC'),
    (1, 55.7590, 37.6250, 15.0,  5, 152, NULL, 93, (CURRENT_DATE + TIME '10:08:00') AT TIME ZONE 'UTC'),
    (1, 55.7610, 37.6285, 11.0,  7, 152, NULL, 93, (CURRENT_DATE + TIME '10:10:00') AT TIME ZONE 'UTC'),
    (1, 55.7625, 37.6300,  2.0,  8, 150, NULL, 92, (CURRENT_DATE + TIME '10:12:00') AT TIME ZONE 'UTC'),

    -- ===== pause ~33 min (parking) -> new trip =====

    -- ===== Trip 2: 10:45–11:00 (with a light stop + one noisy point) =====
    (1, 55.7625, 37.6300,  0.0,  8, 150, NULL, 92, (CURRENT_DATE + TIME '10:45:00') AT TIME ZONE 'UTC'),
    (1, 55.7650, 37.6340, 13.0,  6, 151, NULL, 91, (CURRENT_DATE + TIME '10:47:00') AT TIME ZONE 'UTC'),
    (1, 55.7675, 37.6380, 14.0,  5, 151, NULL, 91, (CURRENT_DATE + TIME '10:49:00') AT TIME ZONE 'UTC'),
    (1, 55.7690, 37.6410,  0.0,  6, 151, NULL, 90, (CURRENT_DATE + TIME '10:51:00') AT TIME ZONE 'UTC'), -- светофор
    (1, 55.7692, 37.6412,  0.0,  7, 151, NULL, 90, (CURRENT_DATE + TIME '10:53:00') AT TIME ZONE 'UTC'), -- всё ещё стоим (пауза < gap)
    (1, 55.7715, 37.6450, 12.0,  6, 152, NULL, 89, (CURRENT_DATE + TIME '10:55:00') AT TIME ZONE 'UTC'),
    (1, 55.7740, 37.6490, 15.0,  5, 152, NULL, 89, (CURRENT_DATE + TIME '10:57:00') AT TIME ZONE 'UTC'),
    (1, 55.7760, 37.6520, 10.0, 90, 153, NULL, 88, (CURRENT_DATE + TIME '10:59:00') AT TIME ZONE 'UTC'), -- шумная точка (accuracy 90)
    (1, 55.7770, 37.6535,  3.0,  8, 150, NULL, 88, (CURRENT_DATE + TIME '11:00:00') AT TIME ZONE 'UTC'),

    -- ===== pause ~40 min (parking) -> new trip =====

    -- ===== Trip 3: 11:40–11:50 =====
    (1, 55.7770, 37.6535,  0.0,  8, 150, NULL, 87, (CURRENT_DATE + TIME '11:40:00') AT TIME ZONE 'UTC'),
    (1, 55.7745, 37.6500, 13.0,  6, 151, NULL, 87, (CURRENT_DATE + TIME '11:42:00') AT TIME ZONE 'UTC'),
    (1, 55.7720, 37.6465, 14.0,  5, 151, NULL, 86, (CURRENT_DATE + TIME '11:44:00') AT TIME ZONE 'UTC'),
    (1, 55.7695, 37.6430, 13.0,  6, 151, NULL, 86, (CURRENT_DATE + TIME '11:46:00') AT TIME ZONE 'UTC'),
    (1, 55.7670, 37.6395, 12.0,  6, 152, NULL, 85, (CURRENT_DATE + TIME '11:48:00') AT TIME ZONE 'UTC'),
    (1, 55.7650, 37.6360,  2.0,  7, 150, NULL, 85, (CURRENT_DATE + TIME '11:50:00') AT TIME ZONE 'UTC');
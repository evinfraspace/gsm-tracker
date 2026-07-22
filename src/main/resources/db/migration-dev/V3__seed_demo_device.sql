-- Dev-only seed: a demo device so GPSLogger can start sending immediately.
INSERT INTO device (id, name, token)
VALUES (1, 'Демо-авто', 'demo-token-123');

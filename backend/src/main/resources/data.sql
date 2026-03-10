-- COUNTRIES
INSERT INTO countries (name, code) VALUES
                                       ('Serbia', 'RS'),
                                       ('Germany', 'DE'),
                                       ('France', 'FR'),
                                       ('Italy', 'IT'),
                                       ('Spain', 'ES'),
                                       ('Poland', 'PL'),
                                       ('Netherlands', 'NL'),
                                       ('Belgium', 'BE'),
                                       ('Austria', 'AT'),
                                       ('Czech Republic', 'CZ'),
                                       ('Hungary', 'HU'),
                                       ('Romania', 'RO'),
                                       ('Bulgaria', 'BG'),
                                       ('Greece', 'GR'),
                                       ('Portugal', 'PT'),
                                       ('Sweden', 'SE'),
                                       ('Denmark', 'DK'),
                                       ('Finland', 'FI'),
                                       ('Norway', 'NO'),
                                       ('Switzerland', 'CH')
    ON CONFLICT (code) DO NOTHING@@

-- CITIES
-- Serbia (10 cities)
INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Belgrade', (SELECT id FROM countries WHERE code = 'RS'), 44.7866, 20.4489),
    ('Novi Sad', (SELECT id FROM countries WHERE code = 'RS'), 45.2671, 19.8335),
    ('Niš', (SELECT id FROM countries WHERE code = 'RS'), 43.3209, 21.8958),
    ('Kragujevac', (SELECT id FROM countries WHERE code = 'RS'), 44.0128, 20.9114),
    ('Subotica', (SELECT id FROM countries WHERE code = 'RS'), 46.1005, 19.6672),
    ('Zrenjanin', (SELECT id FROM countries WHERE code = 'RS'), 45.3833, 20.3833),
    ('Pančevo', (SELECT id FROM countries WHERE code = 'RS'), 44.8708, 20.6403),
    ('Čačak', (SELECT id FROM countries WHERE code = 'RS'), 43.8914, 20.3497),
    ('Kraljevo', (SELECT id FROM countries WHERE code = 'RS'), 43.7236, 20.6878),
    ('Leskovac', (SELECT id FROM countries WHERE code = 'RS'), 42.9981, 21.9461)
ON CONFLICT DO NOTHING@@

-- Germany (15 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Berlin', (SELECT id FROM countries WHERE code = 'DE'), 52.5200, 13.4050),
    ('Munich', (SELECT id FROM countries WHERE code = 'DE'), 48.1351, 11.5820),
    ('Hamburg', (SELECT id FROM countries WHERE code = 'DE'), 53.5511, 9.9937),
    ('Frankfurt', (SELECT id FROM countries WHERE code = 'DE'), 50.1109, 8.6821),
    ('Cologne', (SELECT id FROM countries WHERE code = 'DE'), 50.9375, 6.9603),
    ('Stuttgart', (SELECT id FROM countries WHERE code = 'DE'), 48.7758, 9.1829),
    ('Düsseldorf', (SELECT id FROM countries WHERE code = 'DE'), 51.2277, 6.7735),
    ('Dortmund', (SELECT id FROM countries WHERE code = 'DE'), 51.5136, 7.4653),
    ('Essen', (SELECT id FROM countries WHERE code = 'DE'), 51.4556, 7.0116),
    ('Leipzig', (SELECT id FROM countries WHERE code = 'DE'), 51.3397, 12.3731),
    ('Bremen', (SELECT id FROM countries WHERE code = 'DE'), 53.0793, 8.8017),
    ('Dresden', (SELECT id FROM countries WHERE code = 'DE'), 51.0504, 13.7373),
    ('Hanover', (SELECT id FROM countries WHERE code = 'DE'), 52.3759, 9.7320),
    ('Nuremberg', (SELECT id FROM countries WHERE code = 'DE'), 49.4521, 11.0767),
    ('Duisburg', (SELECT id FROM countries WHERE code = 'DE'), 51.4344, 6.7623)
ON CONFLICT DO NOTHING@@

-- France (12 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Paris', (SELECT id FROM countries WHERE code = 'FR'), 48.8566, 2.3522),
    ('Lyon', (SELECT id FROM countries WHERE code = 'FR'), 45.7640, 4.8357),
    ('Marseille', (SELECT id FROM countries WHERE code = 'FR'), 43.2965, 5.3698),
    ('Toulouse', (SELECT id FROM countries WHERE code = 'FR'), 43.6047, 1.4442),
    ('Nice', (SELECT id FROM countries WHERE code = 'FR'), 43.7102, 7.2620),
    ('Nantes', (SELECT id FROM countries WHERE code = 'FR'), 47.2184, -1.5536),
    ('Strasbourg', (SELECT id FROM countries WHERE code = 'FR'), 48.5734, 7.7521),
    ('Montpellier', (SELECT id FROM countries WHERE code = 'FR'), 43.6108, 3.8767),
    ('Bordeaux', (SELECT id FROM countries WHERE code = 'FR'), 44.8378, -0.5792),
    ('Lille', (SELECT id FROM countries WHERE code = 'FR'), 50.6292, 3.0573),
    ('Rennes', (SELECT id FROM countries WHERE code = 'FR'), 48.1173, -1.6778),
    ('Reims', (SELECT id FROM countries WHERE code = 'FR'), 49.2583, 4.0317)
ON CONFLICT DO NOTHING@@

-- Italy (12 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Rome', (SELECT id FROM countries WHERE code = 'IT'), 41.9028, 12.4964),
    ('Milan', (SELECT id FROM countries WHERE code = 'IT'), 45.4642, 9.1900),
    ('Naples', (SELECT id FROM countries WHERE code = 'IT'), 40.8518, 14.2681),
    ('Turin', (SELECT id FROM countries WHERE code = 'IT'), 45.0703, 7.6869),
    ('Palermo', (SELECT id FROM countries WHERE code = 'IT'), 38.1157, 13.3615),
    ('Genoa', (SELECT id FROM countries WHERE code = 'IT'), 44.4056, 8.9463),
    ('Bologna', (SELECT id FROM countries WHERE code = 'IT'), 44.4949, 11.3426),
    ('Florence', (SELECT id FROM countries WHERE code = 'IT'), 43.7696, 11.2558),
    ('Venice', (SELECT id FROM countries WHERE code = 'IT'), 45.4408, 12.3155),
    ('Verona', (SELECT id FROM countries WHERE code = 'IT'), 45.4384, 10.9916),
    ('Bari', (SELECT id FROM countries WHERE code = 'IT'), 41.1171, 16.8719),
    ('Catania', (SELECT id FROM countries WHERE code = 'IT'), 37.5079, 15.0830)
ON CONFLICT DO NOTHING@@

-- Spain (10 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Madrid', (SELECT id FROM countries WHERE code = 'ES'), 40.4168, -3.7038),
    ('Barcelona', (SELECT id FROM countries WHERE code = 'ES'), 41.3851, 2.1734),
    ('Valencia', (SELECT id FROM countries WHERE code = 'ES'), 39.4699, -0.3763),
    ('Seville', (SELECT id FROM countries WHERE code = 'ES'), 37.3891, -5.9845),
    ('Zaragoza', (SELECT id FROM countries WHERE code = 'ES'), 41.6488, -0.8891),
    ('Málaga', (SELECT id FROM countries WHERE code = 'ES'), 36.7213, -4.4214),
    ('Murcia', (SELECT id FROM countries WHERE code = 'ES'), 37.9922, -1.1307),
    ('Palma', (SELECT id FROM countries WHERE code = 'ES'), 39.5696, 2.6502),
    ('Bilbao', (SELECT id FROM countries WHERE code = 'ES'), 43.2630, -2.9350),
    ('Alicante', (SELECT id FROM countries WHERE code = 'ES'), 38.3452, -0.4810)
ON CONFLICT DO NOTHING@@

-- Poland (8 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Warsaw', (SELECT id FROM countries WHERE code = 'PL'), 52.2297, 21.0122),
    ('Krakow', (SELECT id FROM countries WHERE code = 'PL'), 50.0647, 19.9450),
    ('Wrocław', (SELECT id FROM countries WHERE code = 'PL'), 51.1079, 17.0385),
    ('Poznań', (SELECT id FROM countries WHERE code = 'PL'), 52.4064, 16.9252),
    ('Gdańsk', (SELECT id FROM countries WHERE code = 'PL'), 54.3520, 18.6466),
    ('Szczecin', (SELECT id FROM countries WHERE code = 'PL'), 53.4285, 14.5528),
    ('Łódź', (SELECT id FROM countries WHERE code = 'PL'), 51.7592, 19.4560),
    ('Katowice', (SELECT id FROM countries WHERE code = 'PL'), 50.2649, 19.0238)
ON CONFLICT DO NOTHING@@

-- Netherlands (6 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Amsterdam', (SELECT id FROM countries WHERE code = 'NL'), 52.3676, 4.9041),
    ('Rotterdam', (SELECT id FROM countries WHERE code = 'NL'), 51.9225, 4.4792),
    ('The Hague', (SELECT id FROM countries WHERE code = 'NL'), 52.0705, 4.3007),
    ('Utrecht', (SELECT id FROM countries WHERE code = 'NL'), 52.0907, 5.1214),
    ('Eindhoven', (SELECT id FROM countries WHERE code = 'NL'), 51.4416, 5.4697),
    ('Groningen', (SELECT id FROM countries WHERE code = 'NL'), 53.2194, 6.5665)
ON CONFLICT DO NOTHING@@

-- Belgium (5 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Brussels', (SELECT id FROM countries WHERE code = 'BE'), 50.8503, 4.3517),
    ('Antwerp', (SELECT id FROM countries WHERE code = 'BE'), 51.2194, 4.4025),
    ('Ghent', (SELECT id FROM countries WHERE code = 'BE'), 51.0543, 3.7174),
    ('Charleroi', (SELECT id FROM countries WHERE code = 'BE'), 50.4108, 4.4446),
    ('Liège', (SELECT id FROM countries WHERE code = 'BE'), 50.6326, 5.5797)
ON CONFLICT DO NOTHING@@

-- Austria (5 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Vienna', (SELECT id FROM countries WHERE code = 'AT'), 48.2082, 16.3738),
    ('Graz', (SELECT id FROM countries WHERE code = 'AT'), 47.0707, 15.4395),
    ('Linz', (SELECT id FROM countries WHERE code = 'AT'), 48.3069, 14.2858),
    ('Salzburg', (SELECT id FROM countries WHERE code = 'AT'), 47.8095, 13.0550),
    ('Innsbruck', (SELECT id FROM countries WHERE code = 'AT'), 47.2692, 11.4041)
ON CONFLICT DO NOTHING@@

-- Czech Republic (5 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Prague', (SELECT id FROM countries WHERE code = 'CZ'), 50.0755, 14.4378),
    ('Brno', (SELECT id FROM countries WHERE code = 'CZ'), 49.1951, 16.6068),
    ('Ostrava', (SELECT id FROM countries WHERE code = 'CZ'), 49.8209, 18.2625),
    ('Plzeň', (SELECT id FROM countries WHERE code = 'CZ'), 49.7477, 13.3775),
    ('Liberec', (SELECT id FROM countries WHERE code = 'CZ'), 50.7663, 15.0543)
ON CONFLICT DO NOTHING@@

-- Hungary (5 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Budapest', (SELECT id FROM countries WHERE code = 'HU'), 47.4979, 19.0402),
    ('Debrecen', (SELECT id FROM countries WHERE code = 'HU'), 47.5316, 21.6273),
    ('Szeged', (SELECT id FROM countries WHERE code = 'HU'), 46.2530, 20.1414),
    ('Miskolc', (SELECT id FROM countries WHERE code = 'HU'), 48.1035, 20.7784),
    ('Pécs', (SELECT id FROM countries WHERE code = 'HU'), 46.0727, 18.2328)
ON CONFLICT DO NOTHING@@

-- Romania (5 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Bucharest', (SELECT id FROM countries WHERE code = 'RO'), 44.4268, 26.1025),
    ('Cluj-Napoca', (SELECT id FROM countries WHERE code = 'RO'), 46.7712, 23.6236),
    ('Timișoara', (SELECT id FROM countries WHERE code = 'RO'), 45.7489, 21.2087),
    ('Iași', (SELECT id FROM countries WHERE code = 'RO'), 47.1585, 27.6014),
    ('Constanța', (SELECT id FROM countries WHERE code = 'RO'), 44.1598, 28.6348)
ON CONFLICT DO NOTHING@@

-- Bulgaria (4 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Sofia', (SELECT id FROM countries WHERE code = 'BG'), 42.6977, 23.3219),
    ('Plovdiv', (SELECT id FROM countries WHERE code = 'BG'), 42.1354, 24.7453),
    ('Varna', (SELECT id FROM countries WHERE code = 'BG'), 43.2141, 27.9147),
    ('Burgas', (SELECT id FROM countries WHERE code = 'BG'), 42.5048, 27.4626)
ON CONFLICT DO NOTHING@@

-- Greece (5 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Athens', (SELECT id FROM countries WHERE code = 'GR'), 37.9838, 23.7275),
    ('Thessaloniki', (SELECT id FROM countries WHERE code = 'GR'), 40.6401, 22.9444),
    ('Patras', (SELECT id FROM countries WHERE code = 'GR'), 38.2466, 21.7346),
    ('Heraklion', (SELECT id FROM countries WHERE code = 'GR'), 35.3387, 25.1442),
    ('Larissa', (SELECT id FROM countries WHERE code = 'GR'), 39.6390, 22.4191)
ON CONFLICT DO NOTHING@@

-- Portugal (4 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Lisbon', (SELECT id FROM countries WHERE code = 'PT'), 38.7223, -9.1393),
    ('Porto', (SELECT id FROM countries WHERE code = 'PT'), 41.1579, -8.6291),
    ('Braga', (SELECT id FROM countries WHERE code = 'PT'), 41.5454, -8.4265),
    ('Coimbra', (SELECT id FROM countries WHERE code = 'PT'), 40.2033, -8.4103)
ON CONFLICT DO NOTHING@@

-- Sweden (4 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Stockholm', (SELECT id FROM countries WHERE code = 'SE'), 59.3293, 18.0686),
    ('Gothenburg', (SELECT id FROM countries WHERE code = 'SE'), 57.7089, 11.9746),
    ('Malmö', (SELECT id FROM countries WHERE code = 'SE'), 55.6050, 13.0038),
    ('Uppsala', (SELECT id FROM countries WHERE code = 'SE'), 59.8586, 17.6389)
ON CONFLICT DO NOTHING@@

-- Denmark (3 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Copenhagen', (SELECT id FROM countries WHERE code = 'DK'), 55.6761, 12.5683),
    ('Aarhus', (SELECT id FROM countries WHERE code = 'DK'), 56.1629, 10.2039),
    ('Odense', (SELECT id FROM countries WHERE code = 'DK'), 55.4038, 10.4024)
ON CONFLICT DO NOTHING@@

-- Finland (3 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Helsinki', (SELECT id FROM countries WHERE code = 'FI'), 60.1699, 24.9384),
    ('Espoo', (SELECT id FROM countries WHERE code = 'FI'), 60.2055, 24.6559),
    ('Tampere', (SELECT id FROM countries WHERE code = 'FI'), 61.4978, 23.7610)
ON CONFLICT DO NOTHING@@

-- Norway (3 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Oslo', (SELECT id FROM countries WHERE code = 'NO'), 59.9139, 10.7522),
    ('Bergen', (SELECT id FROM countries WHERE code = 'NO'), 60.3913, 5.3221),
    ('Trondheim', (SELECT id FROM countries WHERE code = 'NO'), 63.4305, 10.3951)
ON CONFLICT DO NOTHING@@

-- Switzerland (5 cities)
    INSERT INTO cities (name, country_id, latitude, longitude) VALUES
    ('Zurich', (SELECT id FROM countries WHERE code = 'CH'), 47.3769, 8.5417),
    ('Geneva', (SELECT id FROM countries WHERE code = 'CH'), 46.2044, 6.1432),
    ('Basel', (SELECT id FROM countries WHERE code = 'CH'), 47.5596, 7.5886),
    ('Bern', (SELECT id FROM countries WHERE code = 'CH'), 46.9480, 7.4474),
    ('Lausanne', (SELECT id FROM countries WHERE code = 'CH'), 46.5197, 6.6323)
ON CONFLICT DO NOTHING@@

-- Vehicle Makes
    INSERT INTO vehicle_makes (name) VALUES
    ('Toyota'),
    ('Honda'),
    ('Ford'),
    ('Chevrolet'),
    ('BMW'),
    ('Mercedes-Benz'),
    ('Audi'),
    ('Nissan')
ON CONFLICT DO NOTHING@@

-- Vehicle Models
    INSERT INTO vehicle_models (name, make_id) VALUES
                                                   -- Toyota (1)
    ('Corolla', 1),
    ('Camry', 1),
    ('RAV4', 1),

                                                   -- Honda (2)
    ('Civic', 2),
    ('Accord', 2),
    ('CR-V', 2),

                                                   -- Ford (3)
    ('F-150', 3),
    ('Mustang', 3),
    ('Explorer', 3),

                                                   -- Chevrolet (4)
    ('Silverado', 4),
    ('Malibu', 4),
    ('Equinox', 4),

                                                   -- BMW (5)
    ('3 Series', 5),
    ('5 Series', 5),
    ('X5', 5),

                                                   -- Mercedes-Benz (6)
    ('C-Class', 6),
    ('E-Class', 6),
    ('GLE', 6),

                                                   -- Audi (7)
    ('A4', 7),
    ('Q5', 7),
    ('A6', 7),

                                                   -- Nissan (8)
    ('Altima', 8),
    ('Sentra', 8),
    ('Rogue', 8)
ON CONFLICT DO NOTHING@@

    INSERT INTO vehicles
(registration_number, weight_limit, make_id, model_id, version, created_at, updated_at,
 is_online, last_latitude, last_longitude)
VALUES
    ('NS-101-AA', 1200, 1, 1, 0, NOW(), NOW(), false, 45.2671, 19.8335),
    ('NS-102-AB', 1300, 1, 2, 0, NOW(), NOW(), false, 45.2517, 19.8369),
    ('NS-103-AC', 1400, 1, 3, 0, NOW(), NOW(), false, 45.2400, 19.8200),

    ('BG-201-BA', 1100, 2, 4, 0, NOW(), NOW(), false, 44.7866, 20.4489),
    ('BG-202-BB', 1250, 2, 5, 0, NOW(), NOW(), false, 44.8000, 20.4700),
    ('BG-203-BC', 1350, 2, 6, 0, NOW(), NOW(), false, 44.7750, 20.4300),

    ('NI-301-CA', 2000, 3, 7, 0, NOW(), NOW(), false, 43.3209, 21.8958),
    ('NI-302-CB', 1500, 3, 8, 0, NOW(), NOW(), false, 43.3150, 21.8800),
    ('NI-303-CC', 1800, 3, 9, 0, NOW(), NOW(), false, 43.3300, 21.9100),

    ('KG-401-DA', 2100, 4, 10, 0, NOW(), NOW(), false, 44.0110, 20.9110),
    ('KG-402-DB', 1400, 4, 11, 0, NOW(), NOW(), false, 44.0200, 20.9000),
    ('KG-403-DC', 1600, 4, 12, 0, NOW(), NOW(), false, 44.0050, 20.9200),

    ('SU-501-EA', 1700, 5, 13, 0, NOW(), NOW(), false, 43.8500, 19.8500),
    ('SU-502-EB', 1900, 5, 14, 0, NOW(), NOW(), false, 43.8600, 19.8700),
    ('SU-503-EC', 2200, 5, 15, 0, NOW(), NOW(), false, 43.8400, 19.8300),

    ('PA-601-FA', 1750, 6, 16, 0, NOW(), NOW(), false, 44.8700, 20.6500),
    ('PA-602-FB', 1950, 6, 17, 0, NOW(), NOW(), false, 44.8600, 20.6400),
    ('PA-603-FC', 2300, 6, 18, 0, NOW(), NOW(), false, 44.8800, 20.6600),

    ('ZR-701-GA', 1650, 7, 19, 0, NOW(), NOW(), false, 45.3833, 20.3900),
    ('ZR-702-GB', 1850, 7, 20, 0, NOW(), NOW(), false, 45.3700, 20.4000),
    ('ZR-703-GC', 2050, 7, 21, 0, NOW(), NOW(), false, 45.3900, 20.3800),

    ('SM-801-HA', 1500, 8, 22, 0, NOW(), NOW(), false, 44.5300, 19.2200),
    ('SM-802-HB', 1300, 8, 23, 0, NOW(), NOW(), false, 44.5400, 19.2300),
    ('SM-803-HC', 1700, 8, 24, 0, NOW(), NOW(), false, 44.5200, 19.2100),
    ('SM-804-HD', 1750, 8, 24, 0, NOW(), NOW(), false, 44.5250, 19.2150)
ON CONFLICT DO NOTHING@@;


-- CUSTOMERS
    INSERT INTO customer (first_name, last_name, username, email, password, role, profile_image, created_at, active) VALUES
    ('Marko', 'Markovic', 'markom', 'marko.markovic@example.com', '$2a$10$drXKnsYYWZCEdldriz6e6OFyDnMJpqlmz3p2JUxo8LlzMANHeTVVO', 'CUSTOMER', 'http://localhost:8080/static/user-avatar.webp', NOW(), true),
    ('Ana', 'Jovanovic', 'anaj', 'ana.jovanovic@example.com', '$2a$10$drXKnsYYWZCEdldriz6e6OFyDnMJpqlmz3p2JUxo8LlzMANHeTVVO', 'CUSTOMER', 'http://localhost:8080/static/user-avatar.webp', NOW(), true),
    ('Marina', 'Ivanovic', 'mivanovic', 'ivanovic.marina.003@gmail.com', '$2a$10$drXKnsYYWZCEdldriz6e6OFyDnMJpqlmz3p2JUxo8LlzMANHeTVVO', 'CUSTOMER', 'http://localhost:8080/static/user-avatar.webp', NOW(), true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO companies (status, name, address, city_id, country_id, customer_id, latitude, longitude) VALUES
                                                                                                         ('APPROVED', 'Moja Firma DOO', 'Bulevar Oslobodjenja 12', (SELECT id FROM cities WHERE name = 'Novi Sad'), (SELECT id FROM countries WHERE name = 'Serbia'), (SELECT id FROM customer WHERE username = 'mivanovic'), 45.2671, 19.8335),
                                                                                                         ('PENDING', 'Test Transport', 'Kralja Petra 45', (SELECT id FROM cities WHERE name = 'Belgrade'), (SELECT id FROM countries WHERE name = 'Serbia'), (SELECT id FROM customer WHERE username = 'markom'), 44.7866, 20.4489),
                                                                                                         ('APPROVED', 'Ana Logistics', 'Cara Dusana 8', (SELECT id FROM cities WHERE name = 'Niš'), (SELECT id FROM countries WHERE name = 'Serbia'), (SELECT id FROM customer WHERE username = 'anaj'), 43.3209, 21.8958);


-- MANAGERS
-- INSERT INTO manager (first_name, last_name, username, email, password, role, profile_image, created_at, active, is_blocked, is_supermanager, reset_password) VALUES
--  ('Peter', 'Jan', 'peterjan', 'peter.jan@smartfactory.com', '$2a$10$drXKnsYYWZCEdldriz6e6OFyDnMJpqlmz3p2JUxo8LlzMANHeTVVO', 'MANAGER', 'http://localhost:8080/static/defaults/user-avatar.webp', NOW(), true, false, false, true),
--  ('John', 'Doe', 'johndoe', 'john.doe@smartfactory.com', '$2a$10$drXKnsYYWZCEdldriz6e6OFyDnMJpqlmz3p2JUxo8LlzMANHeTVVO', 'MANAGER', 'http://localhost:8080/static/defaults/user-avatar.webp', NOW(), true, false, false, true),
--  ('Jane', 'Smith', 'janesmith', 'jane.smith@smartfactory.com', '$2a$10$drXKnsYYWZCEdldriz6e6OFyDnMJpqlmz3p2JUxo8LlzMANHeTVVO', 'MANAGER', 'http://localhost:8080/static/defaults/user-avatar.webp', NOW(), true, false, false, false)
--     ON CONFLICT (email) DO NOTHING@@
--
-- -- PRODUCTS
-- INSERT INTO products (sku, name, description, category, price, weight, image_url, is_available, created_at, updated_at, created_by_manager_id, version, is_deleted) VALUES
-- -- Soft Drinks (SOFT-XXX)
--     ('SOFT-001', 'Coca-Cola Classic 500ml', 'Classic Coca-Cola carbonated drink', 'SOFT_DRINKS', 1.99, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('SOFT-002', 'Sprite Lemon-Lime 500ml', 'Refreshing lemon-lime soda', 'SOFT_DRINKS', 1.89, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('SOFT-003', 'Fanta Orange 500ml', 'Orange flavored carbonated drink', 'SOFT_DRINKS', 1.89, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('SOFT-004', 'Pepsi Cola 500ml', 'Pepsi carbonated cola drink', 'SOFT_DRINKS', 1.95, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('SOFT-005', '7UP Lemon-Lime 500ml', 'Lemon-lime flavored soda', 'SOFT_DRINKS', 1.85, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('SOFT-006', 'Dr Pepper 500ml', 'Unique blend of 23 flavors', 'SOFT_DRINKS', 2.05, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('SOFT-007', 'Mountain Dew 500ml', 'Citrus flavored energy soda', 'SOFT_DRINKS', 2.10, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('SOFT-008', 'Schweppes Tonic Water 500ml', 'Classic tonic water', 'SOFT_DRINKS', 2.20, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--
-- -- Juices (JUICE-XXX)
--     ('JUICE-001', 'Minute Maid Orange Juice 1L', '100% pure orange juice', 'JUICES', 3.99, 1.05, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--     ('JUICE-002', 'Tropicana Apple Juice 1L', 'Fresh pressed apple juice', 'JUICES', 3.79, 1.05, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--     ('JUICE-003', 'Del Monte Pineapple Juice 1L', 'Tropical pineapple juice', 'JUICES', 3.89, 1.05, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--     ('JUICE-004', 'Minute Maid Grape Juice 1L', 'Sweet grape juice', 'JUICES', 4.05, 1.05, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--     ('JUICE-005', 'Ocean Spray Cranberry Juice 1L', 'Tart cranberry juice', 'JUICES', 4.50, 1.05, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--     ('JUICE-006', 'Tropicana Mango Nectar 1L', 'Sweet mango nectar', 'JUICES', 4.20, 1.05, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--
-- -- Water (WATER-XXX)
--     ('WATER-001', 'Dasani Purified Water 500ml', 'Purified drinking water', 'WATER', 0.99, 0.50, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith'),0, false),
--     ('WATER-002', 'Aquafina Water 500ml', 'Pure drinking water', 'WATER', 0.95, 0.50, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith'),0, false),
--     ('WATER-003', 'Evian Natural Spring Water 500ml', 'Natural spring water from Alps', 'WATER', 2.50, 0.50, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith'),0, false),
--     ('WATER-004', 'Perrier Sparkling Water 500ml', 'Natural carbonated mineral water', 'WATER', 2.80, 0.50, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith'),0, false),
--     ('WATER-005', 'Fiji Natural Artesian Water 500ml', 'Artesian water from Fiji', 'WATER', 3.20, 0.50, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith'),0, false),
--     ('WATER-006', 'SmartWater Vapor Distilled 1L', 'Vapor distilled electrolyte water', 'WATER', 1.99, 1.00, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith'),0, false),
--
-- -- Sports & Energy Drinks (SPORT-XXX)
--     ('SPORT-001', 'Red Bull Energy Drink 250ml', 'Energy drink with caffeine', 'SPORTS_DRINKS', 2.99, 0.25, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('SPORT-002', 'Monster Energy Green 500ml', 'High caffeine energy drink', 'SPORTS_DRINKS', 3.50, 0.50, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('SPORT-003', 'Gatorade Lemon-Lime 500ml', 'Sports hydration drink', 'SPORTS_DRINKS', 2.50, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('SPORT-004', 'Powerade Mountain Blast 500ml', 'Electrolyte sports drink', 'SPORTS_DRINKS', 2.40, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('SPORT-005', 'Rockstar Energy Drink 500ml', 'Energy drink for active lifestyle', 'SPORTS_DRINKS', 3.30, 0.50, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('SPORT-006', 'Lucozade Sport Orange 500ml', 'Isotonic sports drink', 'SPORTS_DRINKS', 2.60, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--
-- -- Coffee (COFFEE-XXX)
--     ('COFFEE-001', 'Starbucks Frappuccino Mocha 250ml', 'Iced coffee drink', 'COFFEE', 3.99, 0.28, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--     ('COFFEE-002', 'Nescafe Cold Brew Latte 250ml', 'Ready-to-drink cold brew', 'COFFEE', 3.50, 0.28, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--     ('COFFEE-003', 'Starbucks Doubleshot Espresso 200ml', 'Espresso and cream', 'COFFEE', 3.75, 0.22, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--     ('COFFEE-004', 'Costa Coffee Latte 250ml', 'Smooth coffee latte', 'COFFEE', 3.80, 0.28, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--     ('COFFEE-005', 'Dunkin Iced Coffee Vanilla 330ml', 'Vanilla flavored iced coffee', 'COFFEE', 3.60, 0.35, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--
-- -- Tea (TEA-XXX)
--     ('TEA-001', 'Lipton Iced Tea Lemon 500ml', 'Refreshing lemon iced tea', 'TEA', 2.20, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith'),0, false),
--     ('TEA-002', 'Arizona Green Tea with Honey 500ml', 'Green tea with natural honey', 'TEA', 2.40, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith'),0, false),
--     ('TEA-003', 'Nestea Peach Iced Tea 500ml', 'Peach flavored iced tea', 'TEA', 2.30, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith'),0, false),
--     ('TEA-004', 'Pure Leaf Unsweetened Black Tea 500ml', 'Brewed black tea', 'TEA', 2.50, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith'),0, false),
--     ('TEA-005', 'Snapple Raspberry Tea 500ml', 'Raspberry flavored tea', 'TEA', 2.45, 0.55, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith'),0, false),
--
-- -- Plant-Based Drinks (PLANT-XXX)
--     ('PLANT-001', 'Almond Breeze Unsweetened 1L', 'Almond milk beverage', 'PLANT_BASED', 3.50, 1.05, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('PLANT-002', 'Oatly Oat Milk Original 1L', 'Oat-based milk alternative', 'PLANT_BASED', 3.80, 1.05, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('PLANT-003', 'Silk Soy Milk Vanilla 1L', 'Soy milk with vanilla flavor', 'PLANT_BASED', 3.60, 1.05, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('PLANT-004', 'Coconut Dream Original 1L', 'Coconut milk beverage', 'PLANT_BASED', 3.90, 1.05, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--     ('PLANT-005', 'Califia Farms Almond Milk Barista 1L', 'Barista blend almond milk', 'PLANT_BASED', 4.20, 1.05, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'),0, false),
--
-- -- Dairy Products (DAIRY-XXX)
--     ('DAIRY-001', 'Chobani Greek Yogurt Drink Strawberry 300ml', 'Greek yogurt smoothie', 'DAIRY', 2.80, 0.32, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--     ('DAIRY-002', 'Danone Activia Probiotic Drink 200ml', 'Probiotic yogurt drink', 'DAIRY', 2.50, 0.22, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--     ('DAIRY-003', 'Yoplait Smoothie Blueberry 250ml', 'Yogurt smoothie drink', 'DAIRY', 2.60, 0.27, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--     ('DAIRY-004', 'Muller Corner Yogurt Drink Vanilla 330ml', 'Creamy yogurt drink', 'DAIRY', 2.70, 0.35, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false),
--     ('DAIRY-005', 'Yakult Probiotic Drink 5-pack', 'Fermented milk drink', 'DAIRY', 3.50, 0.40, NULL, true, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'),0, false)
-- ON CONFLICT (sku) DO NOTHING@@
--
-- -- FACTORIES
-- INSERT INTO factories (name, address, city_id, country_id, latitude, longitude, is_online, last_heartbeat, version, created_at, updated_at, created_by_manager_id) VALUES
--     ('Belgrade Beverage Plant', 'Bulevar Kralja Aleksandra 73', (SELECT id FROM cities WHERE name='Belgrade'), (SELECT id FROM countries WHERE code='RS'), 44.7866, 20.4489, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan')),
--     ('NoviSad Soft Drinks Factory', 'Futoška 2', (SELECT id FROM cities WHERE name='Novi Sad'), (SELECT id FROM countries WHERE code='RS'), 45.2671, 19.8335, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan')),
--     ('Berlin Beverage Manufacturing', 'Friedrichstraße 100', (SELECT id FROM cities WHERE name='Berlin'), (SELECT id FROM countries WHERE code='DE'), 52.5200, 13.4050, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe')),
--     ('Munich Drinks Production', 'Industriestraße 45', (SELECT id FROM cities WHERE name='Munich'), (SELECT id FROM countries WHERE code='DE'), 48.1351, 11.5820, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe')),
--     ('Paris Beverage Center', 'Rue de la Paix 25', (SELECT id FROM cities WHERE name='Paris'), (SELECT id FROM countries WHERE code='FR'), 48.8566, 2.3522, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith')),
--     ('Lyon Soft Drinks Factory', 'Avenue Jean Jaurès 80', (SELECT id FROM cities WHERE name='Lyon'), (SELECT id FROM countries WHERE code='FR'), 45.7640, 4.8357, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith')),
--     ('Rome Beverage Plant', 'Via Nazionale 150', (SELECT id FROM cities WHERE name='Rome'), (SELECT id FROM countries WHERE code='IT'), 41.9028, 12.4964, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan')),
--     ('Milan Drinks Hub', 'Corso Buenos Aires 40', (SELECT id FROM cities WHERE name='Milan'), (SELECT id FROM countries WHERE code='IT'), 45.4642, 9.1900, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan')),
--     ('Madrid Beverage Factory', 'Calle Gran Vía 50', (SELECT id FROM cities WHERE name='Madrid'), (SELECT id FROM countries WHERE code='ES'), 40.4168, -3.7038, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe')),
--     ('Barcelona Drinks Center', 'Passeig de Gràcia 100', (SELECT id FROM cities WHERE name='Barcelona'), (SELECT id FROM countries WHERE code='ES'), 41.3851, 2.1734, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe')),
--     ('Warsaw Beverage Plant', 'Aleje Jerozolimskie 65', (SELECT id FROM cities WHERE name='Warsaw'), (SELECT id FROM countries WHERE code='PL'), 52.2297, 21.0122, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith')),
--     ('Krakow Soft Drinks Production', 'ul. Floriańska 30', (SELECT id FROM cities WHERE name='Krakow'), (SELECT id FROM countries WHERE code='PL'), 50.0647, 19.9450, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith')),
--     ('Amsterdam Beverage Factory', 'Damrak 50', (SELECT id FROM cities WHERE name='Amsterdam'), (SELECT id FROM countries WHERE code='NL'), 52.3676, 4.9041, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan')),
--     ('Rotterdam Drinks Plant', 'Coolsingel 100', (SELECT id FROM cities WHERE name='Rotterdam'), (SELECT id FROM countries WHERE code='NL'), 51.9225, 4.4792, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan')),
--     ('Brussels Beverage Hub', 'Rue de la Loi 75', (SELECT id FROM cities WHERE name='Brussels'), (SELECT id FROM countries WHERE code='BE'), 50.8503, 4.3517, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe')),
--     ('Vienna Drinks Manufacturing', 'Kärntner Straße 20', (SELECT id FROM cities WHERE name='Vienna'), (SELECT id FROM countries WHERE code='AT'), 48.2082, 16.3738, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe')),
--     ('Prague Beverage Center', 'Wenceslas Square 10', (SELECT id FROM cities WHERE name='Prague'), (SELECT id FROM countries WHERE code='CZ'), 50.0755, 14.4378, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith')),
--     ('Budapest Drinks Facility', 'Andrássy Avenue 40', (SELECT id FROM cities WHERE name='Budapest'), (SELECT id FROM countries WHERE code='HU'), 47.4979, 19.0402, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith')),
--     ('Bucharest Beverage Plant', 'Calea Victoriei 120', (SELECT id FROM cities WHERE name='Bucharest'), (SELECT id FROM countries WHERE code='RO'), 44.4268, 26.1025, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan')),
--     ('Sofia Soft Drinks Factory', 'Vitosha Boulevard 50', (SELECT id FROM cities WHERE name='Sofia'), (SELECT id FROM countries WHERE code='BG'), 42.6977, 23.3219, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan'))
-- ON CONFLICT DO NOTHING;
--
-- -- PRODUCT-FACTORY RELATIONS
-- -- Belgrade Beverage Plant (Soft Drinks + Juices)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='SOFT-001'), (SELECT id FROM factories WHERE name='Belgrade Beverage Plant')),
--     ((SELECT id FROM products WHERE sku='SOFT-002'), (SELECT id FROM factories WHERE name='Belgrade Beverage Plant')),
--     ((SELECT id FROM products WHERE sku='SOFT-003'), (SELECT id FROM factories WHERE name='Belgrade Beverage Plant')),
--     ((SELECT id FROM products WHERE sku='JUICE-001'), (SELECT id FROM factories WHERE name='Belgrade Beverage Plant')),
--     ((SELECT id FROM products WHERE sku='JUICE-002'), (SELECT id FROM factories WHERE name='Belgrade Beverage Plant'))
-- ON CONFLICT DO NOTHING@@
--
-- -- NoviSad Soft Drinks Factory (Soft Drinks + Energy)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='SOFT-004'), (SELECT id FROM factories WHERE name='NoviSad Soft Drinks Factory')),
--     ((SELECT id FROM products WHERE sku='SOFT-005'), (SELECT id FROM factories WHERE name='NoviSad Soft Drinks Factory')),
--     ((SELECT id FROM products WHERE sku='SPORT-001'), (SELECT id FROM factories WHERE name='NoviSad Soft Drinks Factory')),
--     ((SELECT id FROM products WHERE sku='SPORT-002'), (SELECT id FROM factories WHERE name='NoviSad Soft Drinks Factory'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Berlin Beverage Manufacturing (Water + Juices)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='WATER-001'), (SELECT id FROM factories WHERE name='Berlin Beverage Manufacturing')),
--     ((SELECT id FROM products WHERE sku='WATER-002'), (SELECT id FROM factories WHERE name='Berlin Beverage Manufacturing')),
--     ((SELECT id FROM products WHERE sku='WATER-003'), (SELECT id FROM factories WHERE name='Berlin Beverage Manufacturing')),
--     ((SELECT id FROM products WHERE sku='JUICE-003'), (SELECT id FROM factories WHERE name='Berlin Beverage Manufacturing')),
--     ((SELECT id FROM products WHERE sku='JUICE-004'), (SELECT id FROM factories WHERE name='Berlin Beverage Manufacturing'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Munich Drinks Production (Energy + Sports Drinks)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='SPORT-003'), (SELECT id FROM factories WHERE name='Munich Drinks Production')),
--     ((SELECT id FROM products WHERE sku='SPORT-004'), (SELECT id FROM factories WHERE name='Munich Drinks Production')),
--     ((SELECT id FROM products WHERE sku='SPORT-005'), (SELECT id FROM factories WHERE name='Munich Drinks Production')),
--     ((SELECT id FROM products WHERE sku='SPORT-006'), (SELECT id FROM factories WHERE name='Munich Drinks Production'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Paris Beverage Center (Soft Drinks + Coffee)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='SOFT-006'), (SELECT id FROM factories WHERE name='Paris Beverage Center')),
--     ((SELECT id FROM products WHERE sku='SOFT-007'), (SELECT id FROM factories WHERE name='Paris Beverage Center')),
--     ((SELECT id FROM products WHERE sku='COFFEE-001'), (SELECT id FROM factories WHERE name='Paris Beverage Center')),
--     ((SELECT id FROM products WHERE sku='COFFEE-002'), (SELECT id FROM factories WHERE name='Paris Beverage Center')),
--     ((SELECT id FROM products WHERE sku='COFFEE-003'), (SELECT id FROM factories WHERE name='Paris Beverage Center'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Lyon Soft Drinks Factory (Tea + Soft Drinks)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='TEA-001'), (SELECT id FROM factories WHERE name='Lyon Soft Drinks Factory')),
--     ((SELECT id FROM products WHERE sku='TEA-002'), (SELECT id FROM factories WHERE name='Lyon Soft Drinks Factory')),
--     ((SELECT id FROM products WHERE sku='TEA-003'), (SELECT id FROM factories WHERE name='Lyon Soft Drinks Factory')),
--     ((SELECT id FROM products WHERE sku='SOFT-008'), (SELECT id FROM factories WHERE name='Lyon Soft Drinks Factory'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Rome Beverage Plant (Coffee + Dairy)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='COFFEE-004'), (SELECT id FROM factories WHERE name='Rome Beverage Plant')),
--     ((SELECT id FROM products WHERE sku='COFFEE-005'), (SELECT id FROM factories WHERE name='Rome Beverage Plant')),
--     ((SELECT id FROM products WHERE sku='DAIRY-001'), (SELECT id FROM factories WHERE name='Rome Beverage Plant')),
--     ((SELECT id FROM products WHERE sku='DAIRY-002'), (SELECT id FROM factories WHERE name='Rome Beverage Plant')),
--     ((SELECT id FROM products WHERE sku='DAIRY-003'), (SELECT id FROM factories WHERE name='Rome Beverage Plant'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Milan Drinks Hub (Plant-Based + Dairy)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='PLANT-001'), (SELECT id FROM factories WHERE name='Milan Drinks Hub')),
--     ((SELECT id FROM products WHERE sku='PLANT-002'), (SELECT id FROM factories WHERE name='Milan Drinks Hub')),
--     ((SELECT id FROM products WHERE sku='PLANT-003'), (SELECT id FROM factories WHERE name='Milan Drinks Hub')),
--     ((SELECT id FROM products WHERE sku='DAIRY-004'), (SELECT id FROM factories WHERE name='Milan Drinks Hub'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Madrid Beverage Factory (Juices + Water)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='JUICE-005'), (SELECT id FROM factories WHERE name='Madrid Beverage Factory')),
--     ((SELECT id FROM products WHERE sku='JUICE-006'), (SELECT id FROM factories WHERE name='Madrid Beverage Factory')),
--     ((SELECT id FROM products WHERE sku='WATER-004'), (SELECT id FROM factories WHERE name='Madrid Beverage Factory')),
--     ((SELECT id FROM products WHERE sku='WATER-005'), (SELECT id FROM factories WHERE name='Madrid Beverage Factory'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Barcelona Drinks Center (Plant-Based + Tea)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='PLANT-004'), (SELECT id FROM factories WHERE name='Barcelona Drinks Center')),
--     ((SELECT id FROM products WHERE sku='PLANT-005'), (SELECT id FROM factories WHERE name='Barcelona Drinks Center')),
--     ((SELECT id FROM products WHERE sku='TEA-004'), (SELECT id FROM factories WHERE name='Barcelona Drinks Center')),
--     ((SELECT id FROM products WHERE sku='TEA-005'), (SELECT id FROM factories WHERE name='Barcelona Drinks Center'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Warsaw Beverage Plant (Soft Drinks + Water)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='SOFT-001'), (SELECT id FROM factories WHERE name='Warsaw Beverage Plant')),
--     ((SELECT id FROM products WHERE sku='SOFT-004'), (SELECT id FROM factories WHERE name='Warsaw Beverage Plant')),
--     ((SELECT id FROM products WHERE sku='WATER-006'), (SELECT id FROM factories WHERE name='Warsaw Beverage Plant'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Krakow Soft Drinks Production (Energy + Soft Drinks)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='SPORT-001'), (SELECT id FROM factories WHERE name='Krakow Soft Drinks Production')),
--     ((SELECT id FROM products WHERE sku='SPORT-002'), (SELECT id FROM factories WHERE name='Krakow Soft Drinks Production')),
--     ((SELECT id FROM products WHERE sku='SOFT-005'), (SELECT id FROM factories WHERE name='Krakow Soft Drinks Production'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Amsterdam Beverage Factory (Coffee + Tea)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='COFFEE-001'), (SELECT id FROM factories WHERE name='Amsterdam Beverage Factory')),
--     ((SELECT id FROM products WHERE sku='COFFEE-003'), (SELECT id FROM factories WHERE name='Amsterdam Beverage Factory')),
--     ((SELECT id FROM products WHERE sku='TEA-001'), (SELECT id FROM factories WHERE name='Amsterdam Beverage Factory')),
--     ((SELECT id FROM products WHERE sku='TEA-003'), (SELECT id FROM factories WHERE name='Amsterdam Beverage Factory'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Rotterdam Drinks Plant (Water + Juices)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='WATER-001'), (SELECT id FROM factories WHERE name='Rotterdam Drinks Plant')),
--     ((SELECT id FROM products WHERE sku='WATER-003'), (SELECT id FROM factories WHERE name='Rotterdam Drinks Plant')),
--     ((SELECT id FROM products WHERE sku='JUICE-001'), (SELECT id FROM factories WHERE name='Rotterdam Drinks Plant'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Brussels Beverage Hub (Dairy + Soft Drinks)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='DAIRY-005'), (SELECT id FROM factories WHERE name='Brussels Beverage Hub')),
--     ((SELECT id FROM products WHERE sku='DAIRY-001'), (SELECT id FROM factories WHERE name='Brussels Beverage Hub')),
--     ((SELECT id FROM products WHERE sku='SOFT-002'), (SELECT id FROM factories WHERE name='Brussels Beverage Hub'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Vienna Drinks Manufacturing (Coffee + Plant-Based)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='COFFEE-004'), (SELECT id FROM factories WHERE name='Vienna Drinks Manufacturing')),
--     ((SELECT id FROM products WHERE sku='COFFEE-005'), (SELECT id FROM factories WHERE name='Vienna Drinks Manufacturing')),
--     ((SELECT id FROM products WHERE sku='PLANT-001'), (SELECT id FROM factories WHERE name='Vienna Drinks Manufacturing'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Prague Beverage Center (Energy + Water)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='SPORT-003'), (SELECT id FROM factories WHERE name='Prague Beverage Center')),
--     ((SELECT id FROM products WHERE sku='SPORT-005'), (SELECT id FROM factories WHERE name='Prague Beverage Center')),
--     ((SELECT id FROM products WHERE sku='WATER-002'), (SELECT id FROM factories WHERE name='Prague Beverage Center'))
-- ON CONFLICT DO NOTHING@@
--
-- -- Budapest Drinks Facility (Sports + Tea)
--     INSERT INTO product_factory (product_id, factory_id) VALUES
--     ((SELECT id FROM products WHERE sku='SPORT-004'), (SELECT id FROM factories WHERE name='Budapest Drinks Facility')),
--     ((SELECT id FROM products WHERE sku='SPORT-006'), (SELECT id FROM factories WHERE name='Budapest Drinks Facility')),
--     ((SELECT id FROM products WHERE sku='TEA-002'), (SELECT id FROM factories WHERE name='Budapest Drinks Facility'))
-- ON CONFLICT DO NOTHING@@

-- ============================================
-- WAREHOUSES
-- ============================================

INSERT INTO warehouses (name, address, city_id, country_id, latitude, longitude, is_online, last_heartbeat, version, created_at, updated_at, created_by_manager_id)
VALUES
-- Serbia
('Belgrade Central Warehouse', 'Autoput 18', (SELECT id FROM cities WHERE name='Belgrade'), (SELECT id FROM countries WHERE code='RS'), 44.7866, 20.4489, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan')),
('Novi Sad Distribution Center', 'Futoška 32', (SELECT id FROM cities WHERE name='Novi Sad'), (SELECT id FROM countries WHERE code='RS'), 45.2671, 19.8335, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan')),

-- Germany
('Berlin Logistics Hub', 'Warschauer Straße 70', (SELECT id FROM cities WHERE name='Berlin'), (SELECT id FROM countries WHERE code='DE'), 52.5200, 13.4050, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe')),
('Munich Storage Facility', 'Landsberger Straße 150', (SELECT id FROM cities WHERE name='Munich'), (SELECT id FROM countries WHERE code='DE'), 48.1351, 11.5820, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe')),

-- France
('Paris Regional Warehouse', 'Rue de Bercy 45', (SELECT id FROM cities WHERE name='Paris'), (SELECT id FROM countries WHERE code='FR'), 48.8566, 2.3522, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith')),
('Lyon Distribution Point', 'Rue de la République 88', (SELECT id FROM cities WHERE name='Lyon'), (SELECT id FROM countries WHERE code='FR'), 45.7640, 4.8357, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith')),

-- Italy
('Rome Central Storage', 'Via Tuscolana 200', (SELECT id FROM cities WHERE name='Rome'), (SELECT id FROM countries WHERE code='IT'), 41.9028, 12.4964, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan')),
('Milan Logistics Center', 'Viale Monza 180', (SELECT id FROM cities WHERE name='Milan'), (SELECT id FROM countries WHERE code='IT'), 45.4642, 9.1900, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan')),

-- Spain
('Madrid Warehouse Complex', 'Calle de Alcalá 200', (SELECT id FROM cities WHERE name='Madrid'), (SELECT id FROM countries WHERE code='ES'), 40.4168, -3.7038, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe')),
('Barcelona Storage Hub', 'Carrer de Provença 150', (SELECT id FROM cities WHERE name='Barcelona'), (SELECT id FROM countries WHERE code='ES'), 41.3851, 2.1734, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe')),

-- Poland
('Warsaw Distribution Center', 'Aleje Jerozolimskie 120', (SELECT id FROM cities WHERE name='Warsaw'), (SELECT id FROM countries WHERE code='PL'), 52.2297, 21.0122, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith')),
('Krakow Regional Warehouse', 'ul. Wielicka 50', (SELECT id FROM cities WHERE name='Krakow'), (SELECT id FROM countries WHERE code='PL'), 50.0647, 19.9450, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='janesmith')),

-- Netherlands
('Amsterdam Logistics Center', 'Haarlemmerweg 333', (SELECT id FROM cities WHERE name='Amsterdam'), (SELECT id FROM countries WHERE code='NL'), 52.3676, 4.9041, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan')),
('Rotterdam Port Warehouse', 'Maasboulevard 100', (SELECT id FROM cities WHERE name='Rotterdam'), (SELECT id FROM countries WHERE code='NL'), 51.9225, 4.4792, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='peterjan')),

-- Austria
('Vienna Central Warehouse', 'Gudrunstraße 155', (SELECT id FROM cities WHERE name='Vienna'), (SELECT id FROM countries WHERE code='AT'), 48.2082, 16.3738, false, NULL, 0, NOW(), NOW(), (SELECT id FROM manager WHERE username='johndoe'))

    ON CONFLICT DO NOTHING;


-- ============================================
-- SECTORS (2-4 per warehouse)
-- ============================================

-- Belgrade Central Warehouse (4 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Deep Freeze Zone A', 'Ultra-low temperature storage -25°C to -30°C for frozen goods', (SELECT id FROM warehouses WHERE name='Belgrade Central Warehouse'), NULL, NULL, 0, NOW(), NOW()),
    ('Cold Storage Zone B', 'Refrigerated storage 0°C to 4°C for perishables', (SELECT id FROM warehouses WHERE name='Belgrade Central Warehouse'), NULL, NULL, 0, NOW(), NOW()),
    ('Dry Storage Zone C', 'Ambient temperature storage 18°C to 22°C', (SELECT id FROM warehouses WHERE name='Belgrade Central Warehouse'), NULL, NULL, 0, NOW(), NOW()),
    ('High-Value Secure Zone', 'Climate-controlled secure area 15°C to 20°C', (SELECT id FROM warehouses WHERE name='Belgrade Central Warehouse'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Novi Sad Distribution Center (3 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Freezer Section', 'Frozen goods storage -18°C to -22°C', (SELECT id FROM warehouses WHERE name='Novi Sad Distribution Center'), NULL, NULL, 0, NOW(), NOW()),
    ('Chilled Section', 'Fresh products 2°C to 6°C', (SELECT id FROM warehouses WHERE name='Novi Sad Distribution Center'), NULL, NULL, 0, NOW(), NOW()),
    ('Ambient Section', 'Room temperature goods 18°C to 24°C', (SELECT id FROM warehouses WHERE name='Novi Sad Distribution Center'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Berlin Logistics Hub (4 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Arctic Storage Unit', 'Deep freeze -28°C to -32°C', (SELECT id FROM warehouses WHERE name='Berlin Logistics Hub'), NULL, NULL, 0, NOW(), NOW()),
    ('Fresh Zone', 'Refrigerated 1°C to 5°C', (SELECT id FROM warehouses WHERE name='Berlin Logistics Hub'), NULL, NULL, 0, NOW(), NOW()),
    ('Dry Goods Area', 'Standard temperature 16°C to 22°C', (SELECT id FROM warehouses WHERE name='Berlin Logistics Hub'), NULL, NULL, 0, NOW(), NOW()),
    ('Loading Dock Zone', 'Temperature-controlled dock 10°C to 15°C', (SELECT id FROM warehouses WHERE name='Berlin Logistics Hub'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Munich Storage Facility (2 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Cold Chain Zone', 'Continuous cold chain 0°C to 4°C', (SELECT id FROM warehouses WHERE name='Munich Storage Facility'), NULL, NULL, 0, NOW(), NOW()),
    ('General Storage', 'Multi-purpose storage 15°C to 25°C', (SELECT id FROM warehouses WHERE name='Munich Storage Facility'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Paris Regional Warehouse (3 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Congélateur Principal', 'Main freezer -20°C to -24°C', (SELECT id FROM warehouses WHERE name='Paris Regional Warehouse'), NULL, NULL, 0, NOW(), NOW()),
    ('Réfrigéré Zone', 'Refrigerated zone 2°C to 7°C', (SELECT id FROM warehouses WHERE name='Paris Regional Warehouse'), NULL, NULL, 0, NOW(), NOW()),
    ('Stockage Sec', 'Dry storage 18°C to 23°C', (SELECT id FROM warehouses WHERE name='Paris Regional Warehouse'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Lyon Distribution Point (2 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Frigo Secteur A', 'Cooled sector A 3°C to 8°C', (SELECT id FROM warehouses WHERE name='Lyon Distribution Point'), NULL, NULL, 0, NOW(), NOW()),
    ('Sec Secteur B', 'Dry sector B 17°C to 22°C', (SELECT id FROM warehouses WHERE name='Lyon Distribution Point'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Rome Central Storage (4 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Congelatore Profondo', 'Deep freezer -22°C to -26°C', (SELECT id FROM warehouses WHERE name='Rome Central Storage'), NULL, NULL, 0, NOW(), NOW()),
    ('Refrigerazione Standard', 'Standard refrigeration 1°C to 5°C', (SELECT id FROM warehouses WHERE name='Rome Central Storage'), NULL, NULL, 0, NOW(), NOW()),
    ('Zona Temperatura Ambiente', 'Ambient temperature zone 19°C to 24°C', (SELECT id FROM warehouses WHERE name='Rome Central Storage'), NULL, NULL, 0, NOW(), NOW()),
    ('Deposito Speciale', 'Special storage 12°C to 16°C', (SELECT id FROM warehouses WHERE name='Rome Central Storage'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Milan Logistics Center (3 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Freddo Intenso', 'Intense cold -18°C to -23°C', (SELECT id FROM warehouses WHERE name='Milan Logistics Center'), NULL, NULL, 0, NOW(), NOW()),
    ('Fresco Zona', 'Fresh zone 2°C to 6°C', (SELECT id FROM warehouses WHERE name='Milan Logistics Center'), NULL, NULL, 0, NOW(), NOW()),
    ('Magazzino Generale', 'General warehouse 16°C to 21°C', (SELECT id FROM warehouses WHERE name='Milan Logistics Center'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Madrid Warehouse Complex (4 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Congelación Profunda', 'Deep freeze -25°C to -28°C', (SELECT id FROM warehouses WHERE name='Madrid Warehouse Complex'), NULL, NULL, 0, NOW(), NOW()),
    ('Refrigeración Estándar', 'Standard refrigeration 0°C to 5°C', (SELECT id FROM warehouses WHERE name='Madrid Warehouse Complex'), NULL, NULL, 0, NOW(), NOW()),
    ('Almacén Seco', 'Dry storage 17°C to 23°C', (SELECT id FROM warehouses WHERE name='Madrid Warehouse Complex'), NULL, NULL, 0, NOW(), NOW()),
    ('Zona de Expedición', 'Shipping zone 10°C to 14°C', (SELECT id FROM warehouses WHERE name='Madrid Warehouse Complex'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Barcelona Storage Hub (2 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Frío Industrial', 'Industrial cold -20°C to -24°C', (SELECT id FROM warehouses WHERE name='Barcelona Storage Hub'), NULL, NULL, 0, NOW(), NOW()),
    ('Almacén Normal', 'Normal storage 18°C to 24°C', (SELECT id FROM warehouses WHERE name='Barcelona Storage Hub'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Warsaw Distribution Center (3 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Zamrażarka Główna', 'Main freezer -19°C to -23°C', (SELECT id FROM warehouses WHERE name='Warsaw Distribution Center'), NULL, NULL, 0, NOW(), NOW()),
    ('Chłodnia Świeżych Produktów', 'Fresh products cooler 1°C to 5°C', (SELECT id FROM warehouses WHERE name='Warsaw Distribution Center'), NULL, NULL, 0, NOW(), NOW()),
    ('Magazyn Suchy', 'Dry warehouse 16°C to 22°C', (SELECT id FROM warehouses WHERE name='Warsaw Distribution Center'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Krakow Regional Warehouse (3 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Mroźnia Sektor A', 'Freezer sector A -21°C to -25°C', (SELECT id FROM warehouses WHERE name='Krakow Regional Warehouse'), NULL, NULL, 0, NOW(), NOW()),
    ('Chłodnia Sektor B', 'Cooler sector B 2°C to 7°C', (SELECT id FROM warehouses WHERE name='Krakow Regional Warehouse'), NULL, NULL, 0, NOW(), NOW()),
    ('Magazyn Standardowy', 'Standard warehouse 18°C to 23°C', (SELECT id FROM warehouses WHERE name='Krakow Regional Warehouse'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Amsterdam Logistics Center (4 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Vriescel Diepvries', 'Deep freeze cell -24°C to -28°C', (SELECT id FROM warehouses WHERE name='Amsterdam Logistics Center'), NULL, NULL, 0, NOW(), NOW()),
    ('Koelruimte Vers', 'Fresh cooler space 1°C to 4°C', (SELECT id FROM warehouses WHERE name='Amsterdam Logistics Center'), NULL, NULL, 0, NOW(), NOW()),
    ('Droge Opslag', 'Dry storage 17°C to 22°C', (SELECT id FROM warehouses WHERE name='Amsterdam Logistics Center'), NULL, NULL, 0, NOW(), NOW()),
    ('Expeditie Zone', 'Expedition zone 12°C to 16°C', (SELECT id FROM warehouses WHERE name='Amsterdam Logistics Center'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Rotterdam Port Warehouse (2 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Koeling Sectie', 'Cooling section 0°C to 5°C', (SELECT id FROM warehouses WHERE name='Rotterdam Port Warehouse'), NULL, NULL, 0, NOW(), NOW()),
    ('Algemene Opslag', 'General storage 16°C to 24°C', (SELECT id FROM warehouses WHERE name='Rotterdam Port Warehouse'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- Vienna Central Warehouse (3 sectors)
INSERT INTO sectors (name, description, warehouse_id, last_temperature, last_temperature_reading_at, version, created_at, updated_at)
VALUES
    ('Tiefkühlzone', 'Deep freeze zone -22°C to -26°C', (SELECT id FROM warehouses WHERE name='Vienna Central Warehouse'), NULL, NULL, 0, NOW(), NOW()),
    ('Kühlzone Frisch', 'Fresh cooling zone 2°C to 6°C', (SELECT id FROM warehouses WHERE name='Vienna Central Warehouse'), NULL, NULL, 0, NOW(), NOW()),
    ('Trockenlager Standard', 'Standard dry storage 18°C to 23°C', (SELECT id FROM warehouses WHERE name='Vienna Central Warehouse'), NULL, NULL, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;


SELECT setval('seq_sku_soft_drinks', 8);
SELECT setval('seq_sku_juices', 6);
SELECT setval('seq_sku_water', 6);
SELECT setval('seq_sku_sports_drinks', 6);
SELECT setval('seq_sku_coffee', 5);
SELECT setval('seq_sku_tea', 5);
SELECT setval('seq_sku_plant_based', 5);
SELECT setval('seq_sku_dairy', 5);
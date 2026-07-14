INSERT INTO PRODUCTS (name, description, price, category, created_at, updated_at) VALUES
('iPhone 15', 'Apple smartphone 128GB OLED', 999.99, 'SMARTPHONE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('iPhone 15 Plus', 'Apple smartphone 256GB OLED large screen', 1199.99, 'SMARTPHONE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('iPhone 15 Pro', 'Apple smartphone Pro Titanium camera', 1399.99, 'SMARTPHONE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Samsung Galaxy S24', 'Samsung Android smartphone AI camera', 899.99, 'SMARTPHONE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Samsung Galaxy S24 Ultra', 'Samsung premium Android smartphone stylus', 1299.99, 'SMARTPHONE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Google Pixel 9', 'Google Android AI smartphone camera', 949.99, 'SMARTPHONE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Xiaomi 14', 'Android smartphone Leica camera', 849.99, 'SMARTPHONE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

('MacBook Air M3', 'Apple ultrabook laptop 13 inch lightweight', 1399.99, 'LAPTOP', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('MacBook Pro M3', 'Apple professional laptop powerful performance', 2499.99, 'LAPTOP', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Dell XPS 13', 'Dell ultrabook Windows laptop compact', 1299.99, 'LAPTOP', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Lenovo ThinkPad X1', 'Business laptop lightweight durable', 1599.99, 'LAPTOP', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('HP Spectre x360', 'Convertible laptop touchscreen Windows', 1499.99, 'LAPTOP', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

('AirPods Pro', 'Apple wireless earbuds noise cancellation', 279.99, 'AUDIO', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Galaxy Buds 3', 'Samsung wireless earbuds noise cancellation', 199.99, 'AUDIO', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Sony WH1000XM5', 'Sony noise cancelling wireless headset', 399.99, 'AUDIO', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('JBL Charge 5', 'Bluetooth speaker portable waterproof', 179.99, 'AUDIO', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

('Apple Watch Series 10', 'Apple connected smartwatch health sport', 499.99, 'WATCH', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Galaxy Watch 7', 'Samsung smartwatch health sport Android', 399.99, 'WATCH', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

('iPad Air', 'Apple tablet lightweight touch screen', 799.99, 'TABLET', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Galaxy Tab S10', 'Samsung Android tablet large screen', 749.99, 'TABLET', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());


INSERT INTO SALES (id, total, sale_date) VALUES
(1, 1779.97, CURRENT_TIMESTAMP()),
(2, 1279.98, CURRENT_TIMESTAMP()),
(3, 2199.98, CURRENT_TIMESTAMP()),
(4, 1479.98, CURRENT_TIMESTAMP()),
(5, 1699.98, CURRENT_TIMESTAMP()),
(6, 1499.97, CURRENT_TIMESTAMP()),
(7, 1499.98, CURRENT_TIMESTAMP()),
(8, 1679.98, CURRENT_TIMESTAMP()),
(9, 2899.98, CURRENT_TIMESTAMP()),
(10, 1699.98, CURRENT_TIMESTAMP()),
(11, 2179.97, CURRENT_TIMESTAMP()),
(12, 1349.98, CURRENT_TIMESTAMP()),
(13, 1079.98, CURRENT_TIMESTAMP()),
(14, 949.98, CURRENT_TIMESTAMP()),
(15, 1649.98, CURRENT_TIMESTAMP());

INSERT INTO SALE_ITEMS (sale_id, product_id, quantity, unit_price, line_total) VALUES
(1, 1, 1, 999.99, 999.99),
(1, 13, 1, 279.99, 279.99),
(1, 17, 1, 499.99, 499.99),

(2, 1, 1, 999.99, 999.99),
(2, 13, 1, 279.99, 279.99),

(3, 1, 1, 999.99, 999.99),
(3, 2, 1, 1199.99, 1199.99),

(4, 2, 1, 1199.99, 1199.99),
(4, 13, 1, 279.99, 279.99),

(5, 2, 1, 1199.99, 1199.99),
(5, 17, 1, 499.99, 499.99),

(6, 4, 1, 899.99, 899.99),
(6, 14, 1, 199.99, 199.99),
(6, 18, 1, 399.99, 399.99),

(7, 5, 1, 1299.99, 1299.99),
(7, 14, 1, 199.99, 199.99),

(8, 8, 1, 1399.99, 1399.99),
(8, 13, 1, 279.99, 279.99),

(9, 9, 1, 2499.99, 2499.99),
(9, 15, 1, 399.99, 399.99),

(10, 10, 1, 1299.99, 1299.99),
(10, 15, 1, 399.99, 399.99),

(11, 3, 1, 1399.99, 1399.99),
(11, 13, 1, 279.99, 279.99),
(11, 17, 1, 499.99, 499.99),

(12, 6, 1, 949.99, 949.99),
(12, 15, 1, 399.99, 399.99),

(13, 19, 1, 799.99, 799.99),
(13, 13, 1, 279.99, 279.99),

(14, 20, 1, 749.99, 749.99),
(14, 14, 1, 199.99, 199.99),

(15, 4, 1, 899.99, 899.99),
(15, 20, 1, 749.99, 749.99);
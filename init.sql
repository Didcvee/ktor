CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    reset_token TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories(
    id serial PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS sneakers(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    count INTEGER NOT NULL,
    discount INTEGER NOT NULL CHECK (discount >= 0 AND discount <= 100),
    photo VARCHAR(255) NOT NULL,
    gender CHAR(1) NOT NULL CHECK (gender IN ('M', 'F', 'U')),
    bootSize INTEGER NOT NULL,
    categoryid INTEGER NOT NULL,
    FOREIGN KEY (categoryid) REFERENCES categories(id) ON DELETE CASCADE
);

INSERT INTO categories (name, description)
VALUES ('tennis', 'for tennis');

INSERT INTO categories (name, description)
VALUES ('outdoor', 'for walking');

INSERT INTO categories (name, description)
VALUES ('football', 'for football');

INSERT INTO sneakers (name, description, cost, count, discount, photo, gender, bootSize, categoryid)
VALUES ('Adidas', 'fast', 120.50, 6, 10, 'https://images.footlocker.com/is/image/FLEU/314312107304_01?wid=500&hei=500&fmt=png-alpha', 'M', 42, 3);

INSERT INTO sneakers (name, description, cost, count, discount, photo, gender, bootSize, categoryid)
VALUES ('Nike', 'Comfortable running shoes', 140, 8, 40, 'https://static.nike.com/a/images/t_default/c4d6bfc9-f44f-467a-8e27-d9d46cfec67e/NIKE+AIR+MAX+270+GS.png', 'F', 39, 2);

INSERT INTO sneakers (name, description, cost, count, discount, photo, gender, bootSize, categoryid)
VALUES ('Puma', 'fast too', 80.12, 4, 80, 'https://www.xxl.se/filespin/63b77c174b30493397b4c328300252ef', 'U', 41, 1);

INSERT INTO sneakers (name, description, cost, count, discount, photo, gender, bootSize, categoryid)
VALUES ('Puma 2', 'fast too', 80.12, 4, 80, 'https://russiangolfer.ru/15127-large_default/krossovki-puma-gs-fast.jpg', 'U', 41, 1);

INSERT INTO sneakers (name, description, cost, count, discount, photo, gender, bootSize, categoryid)
VALUES ('Adidas 2', 'fast too', 80.12, 4, 80, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRiEqdFHbi0oc5NxX7UW5y-3HokGmMc70MkWw&s', 'U', 41, 1);

INSERT INTO sneakers (name, description, cost, count, discount, photo, gender, bootSize, categoryid)
VALUES ('Nike 2', 'fast too', 80.12, 4, 80, 'https://jordan-nike.ru/image/cache/catalog/!!!!!!!!!!!!!!!!!!!!!!!!!!111111111111111111111111111111111111111111111/_2022-12-29_102143143-300x350.png', 'U', 41, 1);

INSERT INTO sneakers (name, description, cost, count, discount, photo, gender, bootSize, categoryid)
VALUES ('Puma 3', 'fast too', 80.12, 4, 80, 'https://slamdunk.shop/wp-content/uploads/2024/09/la-france-untouchable-310865-01.webp', 'U', 41, 1);

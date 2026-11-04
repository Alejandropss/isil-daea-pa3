-- PA3: crea la base y el usuario de laboratorio para MySQL.
-- Ejecutar una vez como root:  mysql -u root -p < database/Script_BD_usuario_MySql.sql
-- La clave es solo de laboratorio y coincide con el valor por defecto de application.properties.
-- Fuera del laboratorio se cambia y se pasa con las variables DB_URL, DB_USERNAME y DB_PASSWORD.

CREATE DATABASE IF NOT EXISTS pedidosdb CHARACTER SET utf8mb4;

CREATE USER IF NOT EXISTS 'spedidos_app'@'localhost' IDENTIFIED BY 'userapp_2026!';

-- Hibernate crea la tabla pedido (ddl-auto=update), por eso también necesita CREATE y ALTER.
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON pedidosdb.* TO 'spedidos_app'@'localhost';

FLUSH PRIVILEGES;

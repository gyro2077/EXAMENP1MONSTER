-- ============================================================
-- TicketPremium - Esquema Completo de Base de Datos
-- Base de datos: ticketpremium_db
-- Generado: 2026-05-19
-- ============================================================

-- -----------------------------------------------------------
-- Tabla: USUARIO
-- Almacena las credenciales de acceso al sistema
-- -----------------------------------------------------------
CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

-- Seed data: usuario de prueba
INSERT INTO usuario (username, password) VALUES ('MONSTER', 'MONSTER9');

-- Seed data: cliente asociado al usuario MONSTER
INSERT INTO cliente (usuario_id, cedula, nombre, correo) VALUES (1, '1712345678', 'Monster User', 'monster@test.com');

-- -----------------------------------------------------------
-- Tabla: CLIENTE
-- Almacena los datos personales del comprador
-- Relación 1:1 hacia USUARIO
-- -----------------------------------------------------------
CREATE TABLE cliente (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER UNIQUE,
    cedula VARCHAR(20) UNIQUE,
    nombre VARCHAR(100),
    correo VARCHAR(100),
    CONSTRAINT fk_cliente_usuario_id FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- -----------------------------------------------------------
-- Tabla: FACTURA
-- Cabecera de la compra
-- -----------------------------------------------------------
CREATE TABLE factura (
    id SERIAL PRIMARY KEY,
    cliente_id INTEGER,
    fecha_compra TIMESTAMP NOT NULL,
    subtotal NUMERIC NOT NULL,
    iva NUMERIC NOT NULL,
    total NUMERIC NOT NULL,
    CONSTRAINT fk_factura_cliente_id FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

-- -----------------------------------------------------------
-- Tabla: DETALLE_FACTURA
-- Detalle de los boletos comprados
-- -----------------------------------------------------------
CREATE TABLE detalle_factura (
    id SERIAL PRIMARY KEY,
    factura_id INTEGER NOT NULL,
    codigo_partido INTEGER NOT NULL,
    nombre_partido VARCHAR(200),
    codigo_localidad VARCHAR(50) NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario NUMERIC NOT NULL,
    total_linea NUMERIC NOT NULL,
    CONSTRAINT fk_detalle_factura_factura_id FOREIGN KEY (factura_id) REFERENCES factura(id)
);

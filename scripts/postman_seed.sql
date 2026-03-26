\connect userdb

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO roles (id, nombre)
VALUES
    (1, 'USER'),
    (2, 'ADMIN')
ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre;

INSERT INTO users (
    id, nombre, apellido, email, password, telefono, direccion, estado, fecha_creacion
)
VALUES
    (
        1,
        'Admin',
        'Demo',
        'denilson@test.com',
        crypt('123456', gen_salt('bf')),
        '3000000001',
        'Bogota',
        true,
        NOW()
    ),
    (
        2,
        'User',
        'Demo',
        'user@test.com',
        crypt('123456', gen_salt('bf')),
        '3000000002',
        'Bogota',
        true,
        NOW()
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
VALUES
    (1, 2),
    (2, 1)
ON CONFLICT (user_id, role_id) DO NOTHING;

\connect productdb

INSERT INTO categorias (id, nombre)
VALUES
    (1, 'Smartphones'),
    (2, 'Accesorios')
ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre;

INSERT INTO marcas (id, nombre)
VALUES
    (1, 'Samsung'),
    (2, 'Xiaomi'),
    (3, 'Apple')
ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre;

INSERT INTO productos (
    id, nombre, descripcion, precio, stock, estado, fecha_creacion, categoria_id, marca_id
)
VALUES
    (
        1,
        'Samsung Galaxy A55 5G',
        '128GB, 8GB RAM',
        6999.00,
        25,
        true,
        NOW(),
        1,
        1
    ),
    (
        2,
        'Xiaomi Redmi Note 13',
        '256GB, 8GB RAM',
        5299.00,
        18,
        true,
        NOW(),
        1,
        2
    ),
    (
        3,
        'Apple iPhone 14',
        '128GB',
        12999.00,
        7,
        true,
        NOW(),
        1,
        3
    )
ON CONFLICT (id) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    precio = EXCLUDED.precio,
    stock = EXCLUDED.stock,
    estado = EXCLUDED.estado,
    fecha_creacion = EXCLUDED.fecha_creacion,
    categoria_id = EXCLUDED.categoria_id,
    marca_id = EXCLUDED.marca_id;

\connect promotiondb

INSERT INTO promotions (id, code, discount_percent, active)
VALUES
    (1, 'DESC10', 10.0, true),
    (2, 'DESC20', 20.0, true),
    (3, 'INACTIVA', 15.0, false)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    discount_percent = EXCLUDED.discount_percent,
    active = EXCLUDED.active;

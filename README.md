# Microservices eCommerce

Este proyecto implementa un ecommerce con microservicios Spring Boot, usando:

- `api-gateway` para enrutar y validar JWT
- `eureka-server` para descubrimiento de servicios
- `rabbitmq` para eventos
- Postgres por servicio para persistencia

Servicios incluidos:

- `user-service`
- `product-service`
- `cart-service`
- `order-service`
- `promotion-service`
- `favotite-service`

## Requisitos

- Docker / Docker Desktop
- Opcionalmente Java 17 + Maven Wrapper si quieres ejecutar localmente sin Docker

## Levantar todo

Desde la raíz `microservices`:

```bash
docker compose up --build
```

## Puertos

- `api-gateway`: `http://localhost:8080`
- `eureka-server`: `http://localhost:8761`
- `rabbitmq`: `http://localhost:15672`
- `product-service`: `http://localhost:8081`
- `user-service`: `http://localhost:8082`
- `cart-service`: `http://localhost:8083`
- `order-service`: `http://localhost:8084`
- `promotion-service`: `http://localhost:8085`
- `favotite-service`: `http://localhost:8086`

Las llamadas de negocio deben hacerse vía `api-gateway`, usando rutas `http://localhost:8080/api/**`.

## Auth

Rutas públicas:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

Ruta protegida:

- `GET /api/auth/profile`

El gateway exige `Authorization: Bearer <token>` para las rutas protegidas y además inyecta:

- `X-User-Id`
- `X-User-Role`

Ejemplo de login:

```bash
curl -X POST "http://localhost:8080/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@test.com\",\"password\":\"123456\"}"
```

Ejemplo de perfil:

```bash
curl -X GET "http://localhost:8080/api/auth/profile" ^
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

## Productos

Rutas principales:

- `GET /api/products/{id}`
- `GET /api/products/buscar`
- `POST /api/products` (`ADMIN`)
- `PUT /api/products/{id}` (`ADMIN`)
- `DELETE /api/products/{id}` (`ADMIN`)

Ejemplo de búsqueda con filtros:

```bash
curl "http://localhost:8080/api/products/buscar?categoriaId=1&marcaId=2&precioMin=100&precioMax=500&sortBy=precio&direction=asc"
```

## Categorías y marcas

Categorías:

- `GET /api/products/categorias`
- `POST /api/products/categorias` (`ADMIN`)
- `PUT /api/products/categorias/{id}` (`ADMIN`)
- `DELETE /api/products/categorias/{id}` (`ADMIN`)

Marcas:

- `GET /api/products/marcas`
- `POST /api/products/marcas` (`ADMIN`)
- `PUT /api/products/marcas/{id}` (`ADMIN`)
- `DELETE /api/products/marcas/{id}` (`ADMIN`)

Rango de precios:

- `GET /api/products/precios/rango`

Ejemplo para crear categoría:

```bash
curl -X POST "http://localhost:8080/api/products/categorias" ^
  -H "Authorization: Bearer <ADMIN_TOKEN>" ^
  -H "Content-Type: application/json" ^
  -d "{\"nombre\":\"Laptops\"}"
```

## Carrito

Rutas:

- `POST /api/carts/items`
- `GET /api/carts`
- `PUT /api/carts/items/{productId}`
- `DELETE /api/carts/items/{productId}`
- `POST /api/carts/promotion`
- `DELETE /api/carts/promotion`

Ejemplo de agregar producto:

```bash
curl -X POST "http://localhost:8080/api/carts/items" ^
  -H "Authorization: Bearer <ACCESS_TOKEN>" ^
  -H "Content-Type: application/json" ^
  -d "{\"productId\":1,\"quantity\":2}"
```

Ejemplo de aplicar promoción:

```bash
curl -X POST "http://localhost:8080/api/carts/promotion" ^
  -H "Authorization: Bearer <ACCESS_TOKEN>" ^
  -H "Content-Type: application/json" ^
  -d "{\"code\":\"VIP10\"}"
```

`GET /api/carts` devuelve:

- `subtotal`
- `discountAmount`
- `total`
- `promotionCode`
- `appliedPromotion`

Reglas de promociones en carrito:

- se validan desde `cart-service`
- el descuento se refleja antes del checkout
- se respeta fecha, estado, monto mínimo y `customerUsable`
- cada promo tiene límite global de usos
- cada usuario solo puede usar una promo una vez por cuenta

## Órdenes

Rutas:

- `POST /api/orders/checkout`
- `POST /api/orders/{orderId}/confirm-online`
- `GET /api/orders/{orderId}`

Ejemplo checkout efectivo:

```bash
curl -X POST "http://localhost:8080/api/orders/checkout" ^
  -H "Authorization: Bearer <ACCESS_TOKEN>" ^
  -H "Content-Type: application/json" ^
  -d "{\"paymentMethod\":\"CASH\"}"
```

Si el carrito ya tiene una promoción aplicada, `order-service` la toma desde el carrito aunque no la envíes otra vez.

## Promociones

Rutas:

- `POST /api/promotions` (`ADMIN`)
- `GET /api/promotions` (`ADMIN`)
- `GET /api/promotions/validate?code=...&orderAmount=...`

Ejemplo de creación:

```bash
curl -X POST "http://localhost:8080/api/promotions" ^
  -H "Authorization: Bearer <ADMIN_TOKEN>" ^
  -H "Content-Type: application/json" ^
  -d "{\"code\":\"VIP10\",\"discountType\":\"PERCENTAGE\",\"discountPercent\":10,\"fixedAmount\":null,\"minimumOrderAmount\":100,\"usageLimit\":2,\"startDate\":\"2026-03-27\",\"endDate\":\"2026-03-31\",\"active\":true,\"customerUsable\":true}"
```

Ejemplo de listado:

```bash
curl -X GET "http://localhost:8080/api/promotions" ^
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

Ejemplo de validación:

```bash
curl -X GET "http://localhost:8080/api/promotions/validate?code=VIP10&orderAmount=150" ^
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

`promotion-service` registra el uso real cuando una orden queda pagada.

## Favoritos

Rutas:

- `GET /api/favorites`
- `POST /api/favorites/products/{productId}`
- `DELETE /api/favorites/products/{productId}`

## RabbitMQ

Exchange:

- `ecommerce.events`

Routing key:

- `order.paid`

Queue en `promotion-service`:

- `promotion.order.paid.queue`

Cuando un pedido pasa a `paymentStatus=PAID`, se publica el evento `order.paid` para registrar el uso de la promoción.

## Resumen rápido de endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/profile`
- `GET /api/products/{id}`
- `GET /api/products/buscar`
- `GET /api/products/categorias`
- `GET /api/products/marcas`
- `GET /api/products/precios/rango`
- `POST /api/products/{productId}/images/url`
- `POST /api/products/{productId}/images/upload`
- `POST /api/carts/items`
- `GET /api/carts`
- `POST /api/carts/promotion`
- `POST /api/orders/checkout`
- `GET /api/promotions`
- `GET /api/promotions/validate`
- `GET /api/favorites`

## Nota

- Pago online sigue siendo simulado

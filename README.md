# Microservices eCommerce (API Gateway + Eureka + RabbitMQ)

Este proyecto implementa un ecommerce con microservicios Spring Boot, usando:

- `api-gateway` (Spring Cloud Gateway) para enrutar y validar JWT
- `eureka-server` para descubrimiento de servicios
- `rabbitmq` para eventos (ej. `order.paid`)
- Postgres por servicio para persistencia

Incluye:

- `user-service` (auth JWT)
- `product-service`
- `cart-service`
- `order-service` (checkout online vs efectivo)
- `promotion-service` (validación de códigos y consumo de evento `order.paid`)
- `favotite-service` (favoritos)


## Requisitos

- Docker / Docker Desktop
- (Opcional) Java 17 + Maven/Wrapper si quieres construir sin Docker

## Levantar todo con Docker

Desde la carpeta raíz `microservices`:

```bash
docker compose up --build
```

## Puertos (en host)

- `api-gateway`: `http://localhost:8080`
- `eureka-server`: `http://localhost:8761`
- `rabbitmq`: `http://localhost:15672` (usuario/clave: `guest`/`guest`)
- Servicios:
  - `cart-service`: `http://localhost:8083`
  - `order-service`: `http://localhost:8084`
  - `promotion-service`: `http://localhost:8085`
  - `favotite-service`: `http://localhost:8086`

Las llamadas normales deben hacerse vía `api-gateway` con rutas bajo `/api/**`.

## Auth (JWT)

Las rutas `/api/auth/**` son públicas.
Para el resto de endpoints, el `api-gateway` exige `Authorization: Bearer <token>` y además inyecta:

- `X-User-Id`
- `X-User-Role`

El token se obtiene con `user-service`:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`

Ejemplo rápido de login:

```bash
curl -X POST "http://localhost:8080/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@test.com\",\"password\":\"123456\"}"
```

Luego usa `accessToken` como `Bearer`.

## Flujo Carrito + Checkout

### Agregar al carrito

`POST /api/carts/items`

Body:

```json
{ "productId": 1, "quantity": 2 }
```

Ejemplo:

```bash
curl -X POST "http://localhost:8080/api/carts/items" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <ACCESS_TOKEN>" ^
  -d "{\"productId\":1,\"quantity\":2}"
```

### Ver carrito

`GET /api/carts`

### Checkout (pago en efectivo)

`POST /api/orders/checkout`

Body:

```json
{ "paymentMethod": "CASH", "promotionCode": "VIP10" }
```

Comportamiento:

- crea el pedido
- marca `paymentStatus = PAID` y `status = COMPLETED`
- borra el carrito
- publica evento RabbitMQ `order.paid`

### Checkout (pago online)

`POST /api/orders/checkout`

Body:

```json
{ "paymentMethod": "ONLINE", "promotionCode": "VIP10" }
```

Comportamiento:

- crea el pedido
- deja `paymentStatus = PENDING`
- NO borra el carrito hasta confirmar el pago

Confirmación de pago online (simulado):

`POST /api/orders/{orderId}/confirm-online`

Body:

```json
{ "transactionId": "TX-123" }
```

Comportamiento:

- marca `paymentStatus = PAID` y `status = COMPLETED`
- borra el carrito
- publica evento RabbitMQ `order.paid`

## Promociones

Crear promoción:

`POST /api/promotions`

Body:

```json
{ "code": "VIP10", "discountPercent": 10.0, "active": true }
```

Validar promoción:

`GET /api/promotions/validate?code=VIP10`

> El `order-service` usa este endpoint durante checkout.

## Favoritos

Listar favoritos:

`GET /api/favorites`

Agregar favorito:

`POST /api/favorites/products/{productId}`

Quitar favorito:

`DELETE /api/favorites/products/{productId}`

## RabbitMQ (evento de pedido pagado)

Exchange:

- `ecommerce.events`

Routing key:

- `order.paid`

Queue (en `promotion-service`):

- `promotion.order.paid.queue`

Cuando un pedido pasa a `paymentStatus=PAID`, se publica el evento `order.paid` para que `promotion-service` registre el uso del código.

## Endpoints principales (resumen)

- `user-service` (vía gateway):
  - `POST /api/auth/register`
  - `POST /api/auth/login`
- `product-service` (vía gateway):
  - `GET /api/products/{id}`
- `cart-service` (vía gateway):
  - `POST /api/carts/items`
  - `GET /api/carts`
  - `PUT /api/carts/items/{productId}`
  - `DELETE /api/carts/items/{productId}`
- `order-service` (vía gateway):
  - `POST /api/orders/checkout`
  - `POST /api/orders/{orderId}/confirm-online`
  - `GET /api/orders/{orderId}`
- `promotion-service` (vía gateway):
  - `POST /api/promotions`
  - `GET /api/promotions/validate?code=...`
- `favotite-service` (vía gateway):
  - `GET /api/favorites`
  - `POST /api/favorites/products/{productId}`
  - `DELETE /api/favorites/products/{productId}`

## Nota

- Pago online no integra Stripe/MercadoPago: se confirma con un `transactionId` que envías al endpoint `/confirm-online`.
- El borrado del carrito depende del método de pago:
  - `CASH`: se borra en el checkout
  - `ONLINE`: se borra al confirmar pago


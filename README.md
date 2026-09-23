# TradeNest — E-commerce Backend

A Spring Boot REST API backend for an e-commerce platform, built during my Java Full Stack training at KodNest. Supports customer product browsing, cart, orders, and Razorpay payments, plus a separate admin panel for product, user, and business-analytics management — with custom JWT/cookie-based authentication and role-based access control.

## What it actually does

**Authentication**
- Custom registration/login/logout via `/api/user/*`.
- On login, issues a JWT and sets it as an **HttpOnly, Secure cookie** (`AuthToken`) rather than returning it in the response body — reduces exposure to XSS token theft compared to storing it in localStorage.
- A custom `AuthenticationFilter` (a plain Servlet `Filter`, not Spring Security's filter chain) runs on every request: validates the JWT from the cookie, loads the user, and attaches it to the request as an attribute for controllers to use.

**Role-based access control**
- Two roles: `CUSTOMER` and `ADMIN`.
- The same `AuthenticationFilter` enforces route-level access: any `/api/customer/**` request requires role `CUSTOMER`, any `/api/admin/**` request requires role `ADMIN` — requests failing this get a 403 directly from the filter, before reaching a controller.
- Passwords are hashed with `BCryptPasswordEncoder` (`SecurityConfig`).

**Customer-facing features**
- Browse products, optionally filtered by category (`/api/customer/products`).
- Cart: add, update quantity, delete item, view cart, get cart count (`/api/customer/cart/**`).
- View own order history (`/api/customer/order/details`).
- Checkout via Razorpay: create an order, then verify the payment signature server-side (`/api/customer/payment/**`).

**Admin-facing features**
- Add/delete products (`/api/admin/product/**`).
- Modify user details / look up a user by ID (`/api/admin/user/**`).
- Business analytics: daily, monthly, yearly, and all-time revenue/business summaries (`/api/admin/business/**`).

## Tech stack

- **Java**, **Spring Boot**
- **Spring Data JPA / Hibernate**
- **MySQL** (hosted on Aiven Cloud)
- **JJWT** (`jjwt-api`/`jjwt-impl`/`jjwt-jackson`) — JWT generation & validation
- **Spring Security Crypto** — `BCryptPasswordEncoder` for password hashing (note: full Spring Security filter chain isn't used — auth/authorization is handled by a custom Servlet filter, described above)
- **Razorpay Java SDK** — payment order creation and signature verification
- **Spring Boot Starter Mail** — email sending (SMTP via Gmail)
- **Maven**

## API Reference

| Area | Method | Endpoint | Role required |
|------|--------|----------|----------------|
| Auth | `POST` | `/api/user/regestration` | Public |
| Auth | `POST` | `/api/user/login` | Public |
| Auth | `POST` | `/api/user/logout` | Authenticated |
| Products | `GET` | `/api/customer/products?category=` | CUSTOMER |
| Cart | `POST` | `/api/customer/cart/add` | CUSTOMER |
| Cart | `DELETE` | `/api/customer/cart/delete` | CUSTOMER |
| Cart | `PUT` | `/api/customer/cart/update` | CUSTOMER |
| Cart | `GET` | `/api/customer/cart/items` | CUSTOMER |
| Cart | `GET` | `/api/customer/cart/count` | CUSTOMER |
| Orders | `GET` | `/api/customer/order/details` | CUSTOMER |
| Payment | `POST` | `/api/customer/payment/create` | CUSTOMER |
| Payment | `POST` | `/api/customer/payment/verify` | CUSTOMER |
| Admin — Products | `POST` | `/api/admin/product/add/products` | ADMIN |
| Admin — Products | `DELETE` | `/api/admin/product/delete/products` | ADMIN |
| Admin — Users | `POST` | `/api/admin/user/modify` | ADMIN |
| Admin — Users | `POST` | `/api/admin/user/getbyid` | ADMIN |
| Admin — Business | `GET` | `/api/admin/business/daily?date=` | ADMIN |
| Admin — Business | `GET` | `/api/admin/business/month?month=&year=` | ADMIN |
| Admin — Business | `GET` | `/api/admin/business/year?year=` | ADMIN |
| Admin — Business | `GET` | `/api/admin/business/overall` | ADMIN |

### Example: login

```http
POST /api/user/login
Content-Type: application/json

{
  "username": "jane",
  "password": "secret"
}
```

Response sets an `AuthToken` HttpOnly cookie and returns:

```json
{
  "Message": "Login successfull",
  "role": "CUSTOMER",
  "username": "jane"
}
```

*(Note: a few JSON keys and messages in the actual code have typos like `"Messgae"` and `"regestration"` — worth cleaning up before you demo this, since they're visible in the API contract.)*

## Design notes worth mentioning in an interview

- **HttpOnly cookie for JWT instead of returning it in the response body** — a deliberate choice to reduce XSS-based token theft risk versus the common localStorage pattern.
- **Single filter enforcing both authentication and route-based RBAC** — keeps authorization logic in one place rather than scattering role checks across every controller method.
- **Server-side Razorpay signature verification** — payment success isn't trusted from the client; the backend independently verifies the Razorpay signature before marking a payment complete.

## Running locally

⚠️ Do this first: the credentials currently in `application.properties` (DB, Gmail SMTP, Razorpay, JWT secret) are committed to git history — rotate all of them and move to environment variables before running or sharing this repo further.

```bash
git clone https://github.com/ABISHEK-H-11/tradnest-backend.git
cd tradnest-backend
```

Set these as environment variables (or in a local, git-ignored `application.properties`):

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000

spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_APP_PASSWORD}

Razorpay.key_id=${RAZORPAY_KEY_ID}
Razorpay.key_secret=${RAZORPAY_KEY_SECRET}
```

Then:

```bash
mvn clean install
mvn spring-boot:run
```

The API is configured to accept requests only from `https://tradnest-frontend.vercel.app` (see `@CrossOrigin` on each controller) — update this if running the frontend locally on a different origin.

## What this project demonstrates

End-to-end e-commerce backend design: custom cookie-based JWT auth, role-based route protection, third-party payment integration with server-side verification, and a full customer + admin API surface — built as the capstone project of my Java Full Stack training at KodNest.

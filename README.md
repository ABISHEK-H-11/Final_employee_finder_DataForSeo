# Employee Finder - DataForSEO

![Java CI](https://github.com/ABISHEK-H-11/Final_employee_finder_DataForSeo/actions/workflows/ci.yml/badge.svg)

A Spring Boot application that retrieves and manages employee LinkedIn profiles using DataForSEO.
# 🔎 Employee Finder API

A Spring Boot backend application that searches employee profile data based on company names and manages retrieved profiles using a MySQL database.

The project focuses on **API integration, authentication, quota management, caching, persistence, pagination and automated testing**.

---

## 🚀 Tech Stack

| Technology | Usage |
|---|---|
| Java 21 | Backend development |
| Spring Boot | REST API |
| Spring Security | Authentication & authorization |
| JWT | Secure authentication |
| MySQL | Persistent storage |
| JPA / Hibernate | ORM |
| Maven | Build management |
| Docker | Containerization |
| GitHub Actions | CI |
| DataForSEO | External search API |

---

## ✨ Key Features

- 🔐 JWT-based authentication
- 👤 Account registration and login
- 📊 Daily profile usage quota
- 🔎 Company-based employee search
- 📄 Pagination
- ♻️ Duplicate profile prevention
- ⚡ Database-backed caching/search state
- 🗄️ MySQL persistence
- 🧪 Automated tests
- 🐳 Docker support
- 🔄 GitHub Actions CI
- 🔒 Environment-variable based secrets

---

## 🏗️ Architecture

```text
Client
  │
  ▼
REST Controller
  │
  ▼
Spring Security / JWT
  │
  ▼
Service Layer
  │
  ├── Quota Service
  │
  ├── Employee Search Service
  │
  └── External API Client
  │
  ▼
Repository Layer
  │
  ▼
MySQL
```

External search flow:

```text
Client
  │
  ▼
Employee API
  │
  ▼
Check Authentication
  │
  ▼
Check Daily Quota
  │
  ▼
Check Existing Profiles
  │
  ▼
Search External API
  │
  ▼
Store Profiles
  │
  ▼
Return Paginated Results
```

---

## 🔐 Authentication

The application uses Spring Security and JWT authentication.

Authentication flow:

```text
Register
   ↓
Login
   ↓
JWT Generated
   ↓
Authenticated Request
   ↓
JWT Validation
   ↓
Protected API Access
```

JWT secrets and external API credentials are stored using environment variables rather than committed to source control.

---

## 📊 Daily Quota

The application implements account-level daily profile usage tracking.

Example:

```text
Daily Limit: 10,000 profiles

Request
   ↓
Check usage
   ↓
Calculate remaining quota
   ↓
Process request
   ↓
Increase usage
```

The quota is reset according to the configured application time zone.

---

## 🗄️ Database

The application uses MySQL for persistent storage.

Main entities include:

- Account
- AccountUsage
- CompanySearchState
- DiscoveredProfile

The database is used to maintain authentication data, usage information, search state and discovered profiles.

---

## 🧪 Testing

The project contains automated tests for backend functionality.

Run tests with:

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

Build:

```powershell
.\mvnw.cmd clean package
```

---

## 🐳 Running with Docker

Build the application:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Build the Docker image:

```bash
docker build -t employee-finder .
```

Run the container:

```bash
docker run -p 8080:8080 employee-finder
```

Configure required environment variables before running the application.

---

## ⚙️ Environment Variables

Do not commit credentials to GitHub.

Example:

```text
DATAFORSEO_LOGIN=
DATAFORSEO_PASSWORD=
MYSQL_PASSWORD=
JWT_SECRET=
```

Create your own local environment configuration.

---

## 🔌 Example API

### Register

```http
POST /api/auth/register
```

### Login

```http
POST /api/auth/login
```

### Employee Search

```http
GET /api/employees?company=Wipro
```

The employee search endpoint requires authentication.

---

## 🎯 Engineering Highlights

This project demonstrates practical backend engineering concepts including:

- REST API design
- Authentication and authorization
- JWT security
- Database persistence
- Transactional service logic
- API integration
- Caching
- Pagination
- Quota management
- Exception handling
- Automated testing
- Docker
- CI/CD

---

## 📌 Future Improvements

- Redis-based distributed caching
- Rate limiting
- API Gateway
- Microservices architecture
- AWS deployment
- Kafka-based asynchronous processing
- Observability and centralized logging

---

## 👨‍💻 Author

**Abishek H**

Java Backend Developer

[GitHub](https://github.com/ABISHEK-H-11)

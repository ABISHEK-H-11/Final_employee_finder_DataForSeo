# 🔎 LeadScope — B2B Employee & Lead Discovery Platform

![Java CI](https://github.com/ABISHEK-H-11/Final_employee_finder_DataForSeo/actions/workflows/ci.yml/badge.svg)

**LeadScope** is a Spring Boot-based B2B employee and lead discovery platform that retrieves and manages employee LinkedIn profiles based on company names.

The application integrates with **DataForSEO** to discover employee profiles and uses authentication, quota management, caching, persistence, pagination, automated testing, Docker, and GitHub Actions CI.

---

## 🚀 Tech Stack

| Technology | Usage |
|---|---|
| **Java 21** | Backend development |
| **Spring Boot** | REST API development |
| **Spring Security** | Authentication & authorization |
| **JWT** | Secure authentication |
| **MySQL** | Persistent data storage |
| **JPA / Hibernate** | ORM & database access |
| **Maven** | Build & dependency management |
| **Docker** | Application containerization |
| **GitHub Actions** | Continuous Integration |
| **DataForSEO** | External employee/profile search |

---

## ✨ Key Features

- 🔐 JWT-based authentication
- 👤 Account registration and login
- 📊 Account-level daily profile quota
- 🔎 Company-based employee discovery
- 📄 Paginated employee results
- ♻️ Duplicate profile prevention
- ⚡ Database-backed search state and caching
- 🗄️ MySQL persistence
- 🧪 Automated backend testing
- 🐳 Docker support
- 🔄 GitHub Actions CI
- 🔒 Environment-variable based secret management
- 🌐 External API integration

---

## 🏗️ Architecture

```text
                         Client
                           │
                           ▼
                    REST Controllers
                           │
                           ▼
                 Spring Security / JWT
                           │
                           ▼
                     Service Layer
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
       Quota Service  Employee Search  External API
                           Service       Client
             │             │             │
             └─────────────┼─────────────┘
                           ▼
                    Repository Layer
                           │
                           ▼
                         MySQL
```

### Employee Discovery Flow

```text
Client
  │
  ▼
Employee API
  │
  ▼
Authenticate Request
  │
  ▼
Check Daily Quota
  │
  ▼
Check Existing Profiles
  │
  ├── Profiles Available ──► Return Paginated Results
  │
  └── Profiles Required
              │
              ▼
       Search DataForSEO
              │
              ▼
        Store Profiles
              │
              ▼
       Update Search State
              │
              ▼
       Return Results
```

---

## 🔐 Authentication

LeadScope uses **Spring Security with JWT authentication** to protect employee search APIs.

### Authentication Flow

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

JWT secrets and external API credentials are stored using environment variables and are **not committed to source control**.

---

## 📊 Daily Profile Quota

LeadScope implements account-level daily profile usage tracking.

The current application limit is:

```text
10,000 profiles / account / day
```

### Quota Flow

```text
Request
   ↓
Authenticate Account
   ↓
Check Current Usage
   ↓
Calculate Remaining Quota
   ↓
Process Search
   ↓
Return Profiles
   ↓
Update Usage
```

The quota is reset according to the application's configured time zone.

---

## 🗄️ Database Design

LeadScope uses **MySQL** for persistent storage.

### Main Entities

```text
Account
   │
   └── AccountUsage

CompanySearchState

DiscoveredProfile
```

The database maintains:

- User accounts
- Authentication information
- Daily usage
- Company search state
- Discovered employee profiles
- Returned/unreturned profile state
- Duplicate prevention information

---

## 🔎 Employee Search

Employees can be searched using a company name.

Example:

```http
GET /api/employees?company=Wipro
```

The request:

1. Authenticates the user
2. Checks the account's daily quota
3. Checks previously discovered profiles
4. Searches the external API when required
5. Stores newly discovered profiles
6. Prevents duplicate profiles
7. Returns paginated results
8. Updates account usage

---

## 🔌 API Endpoints

### Authentication

```http
POST /api/auth/register
```

Register a new account.

```http
POST /api/auth/login
```

Authenticate an account and establish JWT authentication.

```http
POST /api/auth/logout
```

Logout from the application.

### Employee Search

```http
GET /api/employees?company=Wipro
```

Search employee profiles for a company.

> 🔒 Employee search requires authentication.

---

## 🧪 Testing

The project includes automated backend tests covering core application functionality.

### Run Tests

Linux / macOS:

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

### Build Application

```powershell
.\mvnw.cmd clean package
```

---

## 🐳 Docker

### 1. Build the Application

```powershell
.\mvnw.cmd clean package -DskipTests
```

### 2. Build Docker Image

```bash
docker build -t leadscope .
```

### 3. Run Container

```bash
docker run -p 8080:8080 leadscope
```

Configure the required environment variables before starting the application.

---

## ⚙️ Environment Variables

Credentials should **never be committed to GitHub**.

Required environment variables include:

```text
DATAFORSEO_LOGIN=
DATAFORSEO_PASSWORD=
MYSQL_PASSWORD=
JWT_SECRET=
```

For local development, configure these variables in your environment.

---

## 🔄 CI with GitHub Actions

Every push and pull request can be validated through the project's GitHub Actions workflow.

The CI pipeline performs automated Maven builds and tests.

![Java CI](https://github.com/ABISHEK-H-11/Final_employee_finder_DataForSeo/actions/workflows/ci.yml/badge.svg)

---

## 🎯 Engineering Highlights

This project demonstrates practical backend engineering concepts:

- RESTful API design
- Spring Boot
- Spring Security
- JWT authentication
- Authentication & authorization
- MySQL database design
- JPA / Hibernate
- Transactional service logic
- External API integration
- Database-backed caching
- Pagination
- Daily quota management
- Duplicate prevention
- Exception handling
- Automated testing
- Docker containerization
- GitHub Actions CI
- Environment-based configuration

---

## 🛣️ Future Improvements

Planned improvements include:

- Redis distributed caching
- API rate limiting
- Spring Cloud API Gateway
- Microservices architecture
- AWS deployment
- Kafka-based asynchronous processing
- Centralized logging
- Application monitoring and observability
- Payment/subscription integration

---

## 👨‍💻 Author

**Abishek H**

**Java Backend Developer**

[GitHub](https://github.com/ABISHEK-H-11)

---

## ⭐ Project

If you find **LeadScope** useful or interesting, consider giving the repository a ⭐.

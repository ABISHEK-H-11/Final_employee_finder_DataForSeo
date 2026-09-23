# Employee Finder — DataForSEO Integration

A Spring Boot REST API that discovers current employees' LinkedIn profiles for a given company name. Built during my internship at Ncube Beacons. It wraps the DataForSEO SERP API, deduplicates and persists results in MySQL, and returns up to 10 unique profiles per request — using progressively broader search terms until it finds enough or exhausts its search strategy.

## What it actually does

Given a company name, the service:

1. **Normalizes the company name** into a cache key (`CompanyKeyNormalizer`) — strips legal suffixes like "Pvt Ltd", "Inc", "LLC", punctuation, and casing, so "Google Inc.", "google inc", and "Google LLC" all map to the same cache entry.
2. **Checks how many unused profiles are already cached** for that company in MySQL.
3. **If fewer than 10 are available**, it queries the DataForSEO Google SERP API (`site:linkedin.com/in/ "company" "search term"`) using an ordered list of ~60 search terms (job titles, departments, seniority levels, and Indian cities), one term at a time, until it has 10 unused profiles or runs out of terms.
4. **Stops early if a company looks "exhausted"** — after 8 consecutive search terms return zero new unique profiles, it gives up rather than burning through the remaining terms (and DataForSEO cost) for nothing.
5. **Deduplicates by normalized LinkedIn URL** per company, so the same profile is never stored twice.
6. **Classifies each result** as a likely current or former employee using text heuristics (looks for "Former", "Ex-", "previously at" vs. the company name being mentioned), and assigns a 0–100 confidence score based on signal strength.
7. **Locks per-company** (not globally) so two simultaneous requests for the same company don't double-fetch, while requests for different companies run concurrently.

## Tech stack

- **Java 21**, **Spring Boot 3.5**
- **Spring Data JPA / Hibernate** — persistence for discovered profiles and per-company search progress
- **MySQL**
- **Spring `RestClient`** — calls to the DataForSEO API
- **Maven**

## API

| Method | Endpoint | Description |
|--------|----------|--------------|
| `GET` | `/api/employees?company={companyName}` | Finds and returns up to 10 employee profiles for the given company. |

### Example request

```http
GET /api/employees?company=Acme Corp
```

### Example response

```json
[
  {
    "name": "Jane Doe",
    "jobTitle": "Software Engineer",
    "company": "Acme Corp",
    "linkedin": "https://linkedin.com/in/janedoe",
    "currentEmployee": true,
    "confidence": 90
  }
]
```

*(Field names match `EmployeeResponse.java` exactly. Swap in a real captured response from Postman if you have one — always more convincing than a hand-written example.)*

## Design notes worth mentioning in an interview

- **Cost-aware fetching**: DataForSEO charges per call. The exhaustion check (8 empty terms in a row → stop) and the cache-first lookup both exist specifically to avoid paying for searches that won't yield new results.
- **Per-company locking, not a global lock**: uses a `ConcurrentHashMap<String, Object>` of per-company lock objects, so concurrent searches for *different* companies never block each other — only two requests for the *same* company are serialized.
- **Graceful degradation**: the DataForSEO client never throws on a failed or malformed remote response — it logs and returns an empty list, so one bad API call doesn't 500 the whole request.

## Running locally

⚠️ Before running this, make sure `application.properties` no longer contains real credentials committed to git — see the setup note below.

```bash
git clone https://github.com/ABISHEK-H-11/Final_employee_finder_DataForSeo.git
cd Final_employee_finder_DataForSeo
```

Set these as environment variables (or in a local, git-ignored `application.properties`):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/employee_finder
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
dataforseo.login=${DATAFORSEO_LOGIN}
dataforseo.password=${DATAFORSEO_PASSWORD}
```

Then:

```bash
mvn clean install
mvn spring-boot:run
```

Runs on `http://localhost:8080` by default.

## What this project demonstrates

REST API design, third-party API integration with graceful failure handling, JPA/Hibernate persistence, a real caching/cost-reduction strategy (not just a buzzword), and concurrency-safe per-resource locking — all from my internship work at Ncube Beacons.

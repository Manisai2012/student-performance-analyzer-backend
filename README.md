# Student Performance Analyzer — Backend REST API

A production-ready, cloud-resilient **Spring Boot 3 REST API** powering the Student Performance Analyzer & Academic Optimization platform. Built with Java 17, Spring Security 6, stateless JWT authentication, Spring Data JPA / Hibernate, and MySQL.

---

## 🏗️ Tech Stack & Architecture

| Component | Technology |
|---|---|
| **Framework** | Spring Boot 3.2.4 |
| **Language** | Java 17 / 21 |
| **Security & Auth** | Spring Security 6, JJWT (JSON Web Token), BCrypt |
| **Persistence** | Spring Data JPA, Hibernate ORM |
| **Database** | MySQL 8.0 (Local / Cloud like Aiven, TiDB, Railway) |
| **Build Tool** | Apache Maven (Maven Wrapper included) |
| **Containerization** | Multi-stage Docker (Eclipse Temurin 17 JRE Alpine) |

---

## 📁 Project Structure

```text
student-performance-analyzer-backend/
├── .mvn/wrapper/              # Maven Wrapper binaries & configs
├── src/
│   ├── main/
│   │   ├── java/com/example/factory/
│   │   │   ├── config/        # Security, CORS, and DataSource configurations
│   │   │   ├── controller/    # REST API Controllers (Auth, Student, Diagnostics, etc.)
│   │   │   ├── model/         # JPA Entities
│   │   │   ├── repository/    # Spring Data JPA Repositories
│   │   │   ├── service/       # Business logic & JWT utilities
│   │   └── resources/
│   │       ├── application.properties # Spring Boot environment-driven config
│   │       └── ...
│   └── test/                  # Unit and integration tests
├── Dockerfile                 # Multi-stage production container build
├── mvnw & mvnw.cmd            # Maven Wrapper executable scripts
├── pom.xml                    # Project dependencies & build lifecycle
├── .env.example               # Template of required environment variables
└── README.md                  # Backend documentation
```

---

## 🚀 Getting Started Locally

### 1. Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher (`java -version`)
- **MySQL Server**: Running locally on port 3306 (or an accessible cloud database)

### 2. Configuration
The application automatically reads configuration parameters from `application.properties` with environment variable overrides. You can create a local MySQL database:
```sql
CREATE DATABASE factory_db;
```

Default local credentials:
* **Host / Port**: `localhost:3306`
* **Database**: `factory_db`
* **Username**: `root`
* **Password**: `root`

To override via environment variables:
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/factory_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="YourSecretKeyThatIsAtLeast256BitsLongForSha256HMAC2026!"
```

### 3. Run with Maven Wrapper
```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

---

## 📡 Key REST API Endpoints

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/health` | Service health status check | No |
| `POST` | `/api/auth/login` | Authenticate user & return JWT token | No |
| `POST` | `/api/auth/register` | Register a new user | No |
| `GET` | `/api/students` | Retrieve all student records | Yes (Bearer JWT) |
| `POST` | `/api/students` | Create student record & marks | Yes (Bearer JWT) |
| `GET` | `/api/students/{id}` | Retrieve specific student details | Yes (Bearer JWT) |
| `PUT` | `/api/students/{id}` | Update student details & marks | Yes (Bearer JWT) |
| `DELETE`| `/api/students/{id}` | Delete student record | Yes (Bearer JWT) |
| `GET` | `/api/diagnostics` | Retrieve subject-wise analytics | Yes (Bearer JWT) |
| `GET` | `/api/recommendations` | Get rule-based booster suggestions | Yes (Bearer JWT) |
| `GET` | `/api/reports/csv` | Download official student performance CSV | Yes (Bearer JWT) |

---

## 🐳 Docker Deployment

Build and run the Docker container locally:
```powershell
# Build image
docker build -t student-analyzer-backend .

# Run container
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://<host>:<port>/<dbname>?sslmode=require&allowPublicKeyRetrieval=true" \
  -e DB_USERNAME="<user>" \
  -e DB_PASSWORD="<password>" \
  -e JWT_SECRET="<your-256-bit-secret>" \
  student-analyzer-backend
```

---

## 🌐 Cloud Deployment (Render / Railway)

1. Connect this repository to **Render** or **Railway**.
2. Environment: **Docker** (using root `Dockerfile`) or **Native Java/Maven**.
3. Set the following Environment Variables in the cloud dashboard:
   - `PORT`: `8080`
   - `DB_URL`: Cloud MySQL JDBC connection string (e.g. Aiven / Railway MySQL)
   - `DB_USERNAME`: Cloud database username
   - `DB_PASSWORD`: Cloud database password
   - `JWT_SECRET`: Random 256-bit secret string
   - `HIBERNATE_DDL_AUTO`: `update`

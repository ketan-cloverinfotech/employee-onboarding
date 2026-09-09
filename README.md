# Employee Onboarding Application

A working employee onboarding application built with Java, Spring Boot, Thymeleaf, JPA, Flyway, and MySQL.

## Features

- Add, edit, view, search, filter, and delete employees.
- Automatically create six default onboarding tasks for each new employee.
- Mark onboarding tasks complete or incomplete.
- Add and delete custom tasks.
- Automatically update employee status from checklist progress.
- Dashboard counts for employees and pending tasks.
- REST API under `/api/employees`.
- MySQL schema management through Flyway migrations.
- Docker Compose setup for the app and MySQL.
- Health endpoint at `/actuator/health`.

## Technology

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.5.16 |
| UI | Thymeleaf, HTML, CSS |
| Database | MySQL 8.4 |
| Persistence | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Build | Maven |
| Containers | Docker, Docker Compose |

## Fastest way: Docker Compose

1. Copy the environment file.

```bash
# Create the local environment file.
cp .env.example .env
```

On Windows PowerShell:

```powershell
# Create the local environment file.
Copy-Item .env.example .env
```

2. Change both passwords in `.env`.

3. Start the application.

```bash
# Build the Java image and start the app with MySQL.
docker compose up --build -d
```

4. Check the containers.

```bash
# Show the application and database container status.
docker compose ps
```

5. Open the application:

```text
http://localhost:8080
```

6. Check application health:

```bash
# Verify that Spring Boot and the database connection are healthy.
curl http://localhost:8080/actuator/health
```

7. Stop the application.

```bash
# Stop containers but keep MySQL data.
docker compose down
```

To also delete all MySQL data:

```bash
# WARNING: Stop containers and permanently remove the database volume.
docker compose down -v
```

## Run without Docker

Requirements:

- Java 17 or later
- Maven 3.9+
- MySQL 8.0+

Create the database and user:

```sql
CREATE DATABASE employee_onboarding
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'onboarding_user'@'localhost'
  IDENTIFIED BY 'onboarding_password';

GRANT ALL PRIVILEGES ON employee_onboarding.*
  TO 'onboarding_user'@'localhost';

FLUSH PRIVILEGES;
```

Run the application on Linux/macOS:

```bash
# Supply database settings and start the Spring Boot application.
DB_URL='jdbc:mysql://localhost:3306/employee_onboarding?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' \
DB_USERNAME='onboarding_user' \
DB_PASSWORD='onboarding_password' \
mvn spring-boot:run
```

Run the application in Windows PowerShell:

```powershell
# Supply database settings to the current PowerShell session.
$env:DB_URL = "jdbc:mysql://localhost:3306/employee_onboarding?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "onboarding_user"
$env:DB_PASSWORD = "onboarding_password"

# Start the Spring Boot application.
mvn spring-boot:run
```

## Build and test

```bash
# Run automated tests.
mvn clean test

# Create the executable Spring Boot JAR.
mvn clean package

# Run the compiled JAR.
java -jar target/employee-onboarding-1.0.0.jar
```

## REST API

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/employees` | List or search employees |
| GET | `/api/employees/{id}` | Get employee details |
| POST | `/api/employees` | Create an employee |
| PUT | `/api/employees/{id}` | Update an employee |
| DELETE | `/api/employees/{id}` | Delete an employee |

Example create request:

```bash
# Create an employee and generate default onboarding tasks.
curl -X POST http://localhost:8080/api/employees \
  -H 'Content-Type: application/json' \
  -d '{
    "employeeCode": "EMP-1001",
    "firstName": "Amit",
    "lastName": "Patil",
    "email": "amit.patil@example.com",
    "phone": "+91 98765 43210",
    "department": "Engineering",
    "jobTitle": "DevOps Engineer",
    "managerName": "Priya Sharma",
    "joiningDate": "2026-08-17",
    "employmentType": "FULL_TIME",
    "status": "PRE_BOARDING"
  }'
```

More examples are available in `api-examples.http` for IntelliJ IDEA or VS Code REST Client.

## Main project structure

```text
src/main/java/com/example/onboarding
├── controller      Web and REST controllers
├── dto             Form and API objects
├── entity          JPA entities and enums
├── exception       Application exceptions
├── repository      Spring Data repositories
└── service         Business logic

src/main/resources
├── db/migration    Flyway SQL migrations
├── static/css      Application styling
├── templates       Thymeleaf pages
└── application.yml Runtime configuration
```

## Production notes

- Replace all default passwords before running outside a laptop.
- Do not expose MySQL port `3306` publicly. Remove the MySQL `ports` block in production if only the app needs database access.
- Put the application behind HTTPS using Nginx, Traefik, an ingress controller, or a cloud load balancer.
- Add authentication and role-based access before storing real employee data.
- Use a managed secret store instead of committing database passwords.
- Back up the MySQL volume and test database restoration.
- Set Thymeleaf cache to `true` in production.

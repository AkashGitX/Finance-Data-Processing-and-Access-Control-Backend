# 💰 Finance Data Processing & Access Control System

> A production-ready, role-based financial backend built with Java 17, Spring Boot 3, JWT Security, and Spring AI — designed for scalable data processing, access control, and intelligent financial analytics.

---
## 🎥 Project Demo Video

Watch the complete explanation and live demonstration of the project below:

👉 https://www.youtube.com/watch?v=KVjuDIOVO2M&feature=youtu.be

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [Database Design](#database-design)
- [API Documentation](#api-documentation)
- [Role-Based Access Control](#role-based-access-control)
- [Dashboard APIs](#dashboard-apis)
- [Spring AI Features](#spring-ai-features)
- [Sample Request & Response](#sample-request--response)
- [Project Flow](#project-flow)
- [Validation & Error Handling](#validation--error-handling)
- [Additional Features](#additional-features)
- [How to Run](#how-to-run)
- [Future Enhancements](#future-enhancements)
- [Author](#author)

---

## 📌 Overview

The **Finance Data Processing & Access Control System** is a fully secured, RESTful backend API that enables organizations to manage financial transactions, control data access by user role, and generate AI-powered financial insights.

The system enforces strict **role-based access control (RBAC)** across three roles — `USER`, `ANALYST`, and `ADMIN` — ensuring each actor interacts only with data and actions they are permitted to access. All authentication is stateless using **JWT tokens**, and all passwords are encrypted using **BCrypt**.

This project demonstrates enterprise-grade backend engineering with clean layered architecture, dynamic dashboard aggregation, and integration with **Spring AI** for intelligent expense and income analysis.

---

## ✨ Key Features

### 🔐 Authentication & Security
- Stateless JWT authentication — no session storage
- BCrypt password hashing for all stored passwords
- Token validation on every protected request via a custom `JwtAuthFilter`
- Role enforcement through Spring Security `@PreAuthorize` annotations

### 🛡️ Role-Based Access Control
- Three distinct roles: `USER`, `ANALYST`, `ADMIN`
- Role-prefixed route namespacing (`/api/user/`, `/api/analyst/`, `/api/admin/`)
- Cross-role access attempts return `403 Forbidden`

### 💳 Transaction Management
- Full CRUD for ADMIN: create, read, update, soft delete
- Read-only access for ANALYST (all data) and USER (own data only)
- Transactions are never hard-deleted — soft delete with `is_deleted` flag

### 🔍 Filtering & Search
- Filter transactions by `type`, `category`, `startDate`, `endDate`, `userId`
- Keyword search across `category` and `notes` fields
- Paginated results with configurable `page` and `size`

### 📊 Dashboard APIs
- Real-time aggregation — totals are always calculated fresh from the database
- Summary, category-wise breakdown, monthly/weekly trends, recent transactions
- Scoped per role: USER sees own data, ANALYST/ADMIN see all data

### 🤖 Spring AI Analytics
- Expense pattern analysis with category and monthly aggregations
- Income vs expense comparison with savings rate calculation
- AI insights generated via Spring AI (OpenAI) when API key is configured
- Graceful fallback message when AI key is not set

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17+ |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security 6, JWT (jjwt), BCrypt |
| ORM | Spring Data JPA, Hibernate 6 |
| Database | PostgreSQL 16 |
| AI Integration | Spring AI (OpenAI) |
| Build Tool | Apache Maven |
| Utilities | Lombok, Jakarta Validation |
| Runtime | GraalVM 22.3 (Java 19 compatible) |

---

## 🏗️ System Architecture

The project follows a clean, layered architecture with strict separation of concerns:

```
Request
   │
   ▼
┌─────────────────────────────┐
│       Security Layer        │  JwtAuthFilter → validates Bearer token
│   (JwtAuthFilter + RBAC)    │  @PreAuthorize → enforces role access
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│       Controller Layer      │  Receives HTTP requests, maps to DTOs
│  /api/admin  /api/analyst   │  Returns ApiResponse<T> wrapper
│  /api/user   /api/auth      │
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│        Service Layer        │  Business logic, validation, aggregation
│  AuthService, UserService   │  Calls repositories, maps entities↔DTOs
│  TransactionService, etc.   │
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│      Repository Layer       │  Spring Data JPA interfaces
│  UserRepository             │  Custom JPQL queries for filtering,
│  TransactionRepository      │  aggregation, and search
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│        Entity Layer         │  JPA Entities mapped to PostgreSQL tables
│  User, Transaction          │  Enums: Role, UserStatus, TransactionType
└─────────────────────────────┘

Supporting Packages:
├── dto/          Request/Response DTOs, ApiResponse<T> wrapper
├── security/     JwtUtil, JwtAuthFilter, UserDetailsServiceImpl
├── config/       SecurityConfig, DataInitializer, AiConfig
├── exception/    GlobalExceptionHandler, ResourceNotFoundException
└── util/         SecurityUtils (extract current user from context)
```

---

## 🗄️ Database Design

### User Entity (`users` table)

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | Auto-generated primary key |
| `name` | VARCHAR | Full name of the user |
| `email` | VARCHAR (UNIQUE) | Login email address |
| `password` | VARCHAR | BCrypt-hashed password |
| `role` | ENUM | `ADMIN`, `ANALYST`, `USER` |
| `status` | ENUM | `ACTIVE`, `INACTIVE` |
| `created_at` | TIMESTAMP | Account creation time |

### Transaction Entity (`transactions` table)

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | Auto-generated primary key |
| `amount` | DECIMAL(15,2) | Transaction amount |
| `type` | ENUM | `INCOME` or `EXPENSE` |
| `category` | VARCHAR | e.g. Salary, Rent, Food |
| `date` | DATE | Date of the transaction |
| `notes` | TEXT | Optional description |
| `user_id` | BIGINT (FK) | References `users.id` |
| `is_deleted` | BOOLEAN | Soft delete flag |
| `created_at` | TIMESTAMP | Record creation time |

### Relationship

```
User (1) ──────────── (*) Transaction
       One user can have many transactions
       Cascade: transactions are scoped to their owner
       Soft delete: is_deleted = true (never physically removed)
```

---

## 📡 API Documentation

> All protected endpoints require the header:
> `Authorization: Bearer <jwt_token>`

---

### 🔓 Auth APIs (Public)

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|-------------|
| POST | `/api/auth/login` | Login for all roles | `{"email":"...","password":"..."}` |
| POST | `/api/admin/login` | Dedicated admin login | `{"email":"...","password":"..."}` |
| GET | `/api/health` | Server health check | — |

---

### 👑 Admin APIs

#### User Management

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|-------------|
| POST | `/api/admin/users` | Create a new user | `{"name","email","password","role"}` |
| GET | `/api/admin/users` | Get all users | — |
| GET | `/api/admin/users/{id}` | Get user by ID | — |
| PUT | `/api/admin/users/{id}` | Update name/role/status | `{"name","role","status"}` |
| PUT | `/api/admin/users/{id}/role` | Update role only | `{"role":"ANALYST"}` |
| PUT | `/api/admin/users/{id}/status` | Update status only | `{"status":"INACTIVE"}` |
| DELETE | `/api/admin/users/{id}` | Delete user | — |

#### Transaction Management

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|-------------|
| POST | `/api/admin/transactions` | Create transaction | `{"amount","type","category","date","notes","userId"}` |
| GET | `/api/admin/transactions` | Get all (with filters) | Query params |
| GET | `/api/admin/transactions/{id}` | Get by ID | — |
| PUT | `/api/admin/transactions/{id}` | Update transaction | `{"amount","type","category","date","notes"}` |
| DELETE | `/api/admin/transactions/{id}` | Soft delete | — |

#### Transaction Filter Query Params (Admin & Analyst)

| Param | Example | Description |
|-------|---------|-------------|
| `type` | `?type=INCOME` | Filter by INCOME or EXPENSE |
| `category` | `?category=Salary` | Filter by category name |
| `startDate` | `?startDate=2024-01-01` | Date range start (ISO format) |
| `endDate` | `?endDate=2024-12-31` | Date range end (ISO format) |
| `userId` | `?userId=2` | Filter by specific user |
| `keyword` | `?keyword=rent` | Search in category and notes |
| `page` | `?page=0` | Page number (0-indexed) |
| `size` | `?size=10` | Results per page |

#### Admin Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/dashboard/summary` | Income, expense, balance totals |
| GET | `/api/admin/dashboard/summary?userId={id}` | Summary for specific user |
| GET | `/api/admin/dashboard/category-wise` | Breakdown by category |
| GET | `/api/admin/dashboard/trends?type=monthly` | Monthly income/expense trends |
| GET | `/api/admin/dashboard/trends?type=weekly` | Weekly income/expense trends |
| GET | `/api/admin/dashboard/recent` | 10 most recent transactions |

---

### 📊 Analyst APIs

#### Transactions (Read Only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/analyst/transactions` | All transactions (full filtering) |
| GET | `/api/analyst/transactions/{id}` | Single transaction |

#### Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/analyst/dashboard/summary` | Overall financial summary |
| GET | `/api/analyst/dashboard/summary?userId={id}` | Summary for a specific user |
| GET | `/api/analyst/dashboard/category-wise` | Category breakdown |
| GET | `/api/analyst/dashboard/trends?type=monthly` | Monthly trends |
| GET | `/api/analyst/dashboard/trends?type=weekly` | Weekly trends |
| GET | `/api/analyst/dashboard/recent` | Recent transactions |

#### AI Analytics

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/analyst/analytics/expense-pattern` | Expense analysis + AI insights |
| GET | `/api/analyst/analytics/income-vs-expense` | Income vs expense + AI insights |

---

### 👤 User (Viewer) APIs

#### Transactions (Own Only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/user/transactions` | Own transactions (limited filtering) |
| GET | `/api/user/transactions/{id}` | Own transaction by ID |

#### Filter Params (User — limited set)

| Param | Example |
|-------|---------|
| `type` | `?type=EXPENSE` |
| `category` | `?category=Food` |
| `startDate` | `?startDate=2024-01-01` |
| `endDate` | `?endDate=2024-12-31` |

#### Dashboard (Own Data Only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/user/dashboard/summary` | Own income, expense, balance |
| GET | `/api/user/dashboard/category-wise` | Own category breakdown |
| GET | `/api/user/dashboard/trends?type=monthly` | Own monthly trends |
| GET | `/api/user/dashboard/trends?type=weekly` | Own weekly trends |
| GET | `/api/user/dashboard/recent` | Own 10 most recent transactions |

---

## 🔒 Role-Based Access Control

| Feature | USER | ANALYST | ADMIN |
|---------|------|---------|-------|
| Login | ✅ | ✅ | ✅ |
| View own transactions | ✅ | ✅ | ✅ |
| View all transactions | ❌ | ✅ | ✅ |
| Filter by userId | ❌ | ✅ | ✅ |
| Keyword search | ❌ | ✅ | ✅ |
| Create transaction | ❌ | ❌ | ✅ |
| Update transaction | ❌ | ❌ | ✅ |
| Delete transaction | ❌ | ❌ | ✅ |
| Own dashboard | ✅ | ✅ | ✅ |
| All-users dashboard | ❌ | ✅ | ✅ |
| AI analytics | ❌ | ✅ | ❌ |
| Create users | ❌ | ❌ | ✅ |
| Update user role/status | ❌ | ❌ | ✅ |
| Delete users | ❌ | ❌ | ✅ |

---

## 📈 Dashboard APIs

All dashboard data is **dynamically calculated** from the database on every request — no pre-aggregated or cached values are stored.

| Endpoint | What it returns |
|----------|----------------|
| `/dashboard/summary` | `totalIncome`, `totalExpense`, `netBalance`, `totalTransactions` |
| `/dashboard/category-wise` | Array of `{category, type, totalAmount, count}` |
| `/dashboard/trends?type=monthly` | Array of `{period: "2024-01", type, totalAmount}` |
| `/dashboard/trends?type=weekly` | Array of `{period: "2024-W03", type, totalAmount}` |
| `/dashboard/recent` | Last 10 transactions ordered by creation time |

ADMIN and ANALYST can pass `?userId={id}` to any dashboard endpoint to scope results to a specific user.

---

## 🤖 Spring AI Features

Spring AI is integrated to generate natural-language insights from real financial data. The AI is called only after aggregation — it receives structured data and returns interpretive commentary.

### Expense Pattern Analysis — `/api/analyst/analytics/expense-pattern`

**Data collected:**
- Total amount spent per category
- Monthly expense totals over time

**AI Prompt:**
> *"Analyze the expense data and identify patterns, trends, anomalies, high-spending categories, and monthly growth."*

**Response includes:**
- `categoryTotals` — ranked list of expense categories
- `monthlyTotals` — expense totals per month
- `aiInsights` — natural language analysis from the AI model
- `aiEnabled` — boolean indicating if AI is active

---

### Income vs Expense Analysis — `/api/analyst/analytics/income-vs-expense`

**Data collected:**
- Total income and total expense
- Net balance and savings rate

**AI Prompt:**
> *"Compare income and expenses and provide insights on financial health, savings trends, and potential risks."*

**Response includes:**
- `totalIncome`, `totalExpense`, `netBalance`
- `savingsRate` — expressed as a percentage
- `aiInsights` — natural language commentary
- `aiEnabled` — boolean

> **Note:** When `spring.ai.openai.api-key` is not configured, the system returns a clear placeholder message and all aggregated data is still fully returned. The API never fails due to a missing AI key.

---

## 📬 Sample Request & Response

### Login

**Request:**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@financeapp.com",
  "password": "Admin@123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "userId": 1,
    "name": "System Admin",
    "email": "admin@financeapp.com",
    "role": "ADMIN"
  }
}
```

---

### Create Transaction (Admin)

**Request:**
```http
POST /api/admin/transactions
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2024-01-15",
  "notes": "January salary",
  "userId": 1
}
```

**Response:**
```json
{
  "success": true,
  "message": "Transaction created successfully",
  "data": {
    "id": 1,
    "amount": 5000.00,
    "type": "INCOME",
    "category": "Salary",
    "date": "2024-01-15",
    "notes": "January salary",
    "userId": 1,
    "userName": "System Admin",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

### Dashboard Summary (Admin)

**Request:**
```http
GET /api/admin/dashboard/summary
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Summary fetched",
  "data": {
    "totalIncome": 5000.00,
    "totalExpense": 1200.00,
    "netBalance": 3800.00,
    "totalTransactions": 2
  }
}
```

---

### Error Response

```json
{
  "success": false,
  "message": "Access denied: You do not have permission to perform this action"
}
```

---

## 🔄 Project Flow

```
Step 1 — Admin Logs In
  POST /api/admin/login
  → Receives JWT token

Step 2 — Admin Creates Users
  POST /api/admin/users  (role: USER or ANALYST)
  → Users are created with ACTIVE status
  → Passwords stored as BCrypt hash

Step 3 — Admin Records Transactions
  POST /api/admin/transactions
  → Assigns each transaction to a user
  → Type: INCOME or EXPENSE

Step 4 — User Views Their Own Data
  GET /api/user/transactions
  GET /api/user/dashboard/summary
  → Only sees transactions assigned to them
  → Dashboard shows personal income/expense/balance

Step 5 — Analyst Generates Insights
  GET /api/analyst/transactions?keyword=rent
  GET /api/analyst/dashboard/summary?userId=2
  GET /api/analyst/analytics/expense-pattern
  → Read-only access to all users' data
  → AI generates natural language insights

Step 6 — Admin Manages Access
  PUT /api/admin/users/{id}/role    → Promote/demote user
  PUT /api/admin/users/{id}/status  → Activate/deactivate
  DELETE /api/admin/users/{id}      → Remove user
```

---

## ✅ Validation & Error Handling

### Input Validation
- All request bodies use `@Valid` with Jakarta Validation annotations
- `@NotBlank`, `@NotNull`, `@Email`, `@Size` applied on DTOs
- Validation errors return `400 Bad Request` with field-level messages

### Global Exception Handling
A `@ControllerAdvice` class (`GlobalExceptionHandler`) handles all exceptions consistently:

| Exception | HTTP Status | Example |
|-----------|------------|---------|
| `ResourceNotFoundException` | 404 Not Found | User/transaction not found |
| `BadRequestException` | 400 Bad Request | Email already registered |
| `MethodArgumentNotValidException` | 400 Bad Request | Validation failures |
| `AccessDeniedException` | 403 Forbidden | Role not permitted |
| `AuthenticationException` | 401 Unauthorized | Invalid credentials |
| `RuntimeException` (unhandled) | 500 Internal Server Error | Unexpected errors |

### Response Format
Every response — success or error — uses the same wrapper:
```json
{
  "success": true | false,
  "message": "Human-readable description",
  "data": { ... } | null
}
```

---

## ⚙️ Additional Features

### Pagination
All list endpoints support pagination via query params:
```
?page=0&size=10
```
Response includes Spring Data `Page` metadata: `totalElements`, `totalPages`, `number`, `size`.

### Soft Delete
Transactions are never permanently deleted. Instead, `is_deleted = true` is set:
- Deleted transactions are excluded from all queries
- Audit trail is preserved in the database
- Can be restored by a database admin if needed

### Logging
`@Slf4j` (Lombok) is used across all service and controller classes:
- Every major operation is logged at `INFO` level
- Errors and exceptions logged at `ERROR` level
- Structured log messages include entity IDs and user context

---

## 🚀 How to Run

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### Step 1 — Clone the Repository

```bash
git clone https://github.com/AkashGitX/Finance-Data-Processing-and-Access-Control-Backend
cd finance-backend
```

### Step 2 — Configure the Database

Create a PostgreSQL database:
```sql
CREATE DATABASE financedb;
```

Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/financedb
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### Step 3 — (Optional) Enable Spring AI

```properties
spring.ai.openai.api-key=sk-your-openai-key
```

Leave it empty or absent to run without AI — the app handles this gracefully.

### Step 4 — Build and Run

```bash
mvn clean package -DskipTests
java -jar target/finance-backend-0.0.1-SNAPSHOT.jar
```

The server starts on **port 8080**.

### Step 5 — Default Admin Account (Auto-Seeded)

On first startup, the system automatically creates:

| Field | Value |
|-------|-------|
| Email | `admin@financeapp.com` |
| Password | `Admin@123` |
| Role | `ADMIN` |

Login immediately with these credentials to get your JWT token.

---



## 👨‍💻 Author

**Akash Sutradhar**

Built with a focus on clean architecture, security best practices, and real-world enterprise patterns. This project demonstrates full-stack backend engineering capability including authentication, authorization, data aggregation, AI integration, and production-quality error handling.


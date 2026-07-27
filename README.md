# 💸 Spendly

> A modern personal finance and subscription management application built with Angular, Spring Boot, and PostgreSQL.

<p align="center">
  <strong>Track your money. Manage your subscriptions. Understand your spending.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Status-In%20Development-orange" alt="Project status">
  <img src="https://img.shields.io/badge/Java-25-blue" alt="Java 25">
  <img src="https://img.shields.io/badge/Spring%20Boot-Backend-6DB33F" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Angular-Frontend-DD0031" alt="Angular">
  <img src="https://img.shields.io/badge/PostgreSQL-Database-4169E1" alt="PostgreSQL">
</p>

---

## 📖 About the Project

**Spendly** is a full-stack portfolio project for managing personal finances, recurring expenses, subscriptions, and monthly budgets.

The application is designed to help users:

- record income and expenses;
- organize transactions into categories;
- monitor monthly budgets;
- manage recurring subscriptions;
- review their current financial situation from a dashboard;
- generate financial summaries and reports.

The project focuses on clean architecture, secure authentication, database migrations, isolated automated testing, and maintainable frontend and backend structure.

---

## 🎯 Project Goals

The main goals of Spendly are:

- building a production-oriented full-stack application;
- applying secure JWT-based authentication;
- designing a clean and extensible financial data model;
- creating a well-structured REST API;
- practising database versioning with Flyway;
- implementing isolated backend tests;
- building a responsive Angular user interface;
- documenting the application through OpenAPI and Swagger UI;
- preparing the project for containerized deployment.

---

## 🚀 MVP Scope

The first release will focus on the essential functionality required for personal finance tracking.

### 👤 Authentication and User Account

- user registration;
- user login;
- JWT-based authentication;
- authenticated user profile;
- secure password storage;
- login rate limiting;
- safe and consistent API error responses.

### 💳 Transactions

- create income transactions;
- create expense transactions;
- update transactions;
- delete transactions;
- store transaction date, amount, description, and category;
- list the authenticated user's transactions;
- filter transactions;
- sort and paginate transaction results.

### 🗂️ Categories

- default income categories;
- default expense categories;
- custom user categories;
- separation of income and expense categories.

### 🎯 Monthly Budgets

- create monthly budgets by category;
- display the budgeted amount;
- calculate the amount already spent;
- calculate the remaining amount;
- indicate when a budget has been exceeded.

### 🔁 Subscriptions

- create recurring subscriptions;
- support monthly and yearly billing periods;
- store the next billing date;
- mark subscriptions as active or cancelled;
- list upcoming subscription charges.

### 📊 Dashboard

- current balance;
- total monthly income;
- total monthly expenses;
- spending breakdown by category;
- monthly budget status;
- upcoming subscription payments.

### 📄 Reports

- monthly financial summary;
- custom date-range summary;
- CSV export.

---

## 🛠️ Technology Stack

### Backend

- Java 25
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT authentication
- PostgreSQL
- Flyway
- Jakarta Validation
- Springdoc OpenAPI
- Maven Wrapper

### Backend Testing

- JUnit 5
- Mockito
- MockMvc
- H2 isolated test database

> Automated tests must never modify or delete data from the real PostgreSQL database.

### Frontend

- Angular
- TypeScript
- Angular Router
- Reactive Forms
- Angular HTTP Client
- JWT HTTP interceptor
- Responsive user interface

A charting library may be introduced after the core dashboard functionality is complete.

### DevOps

- Git
- GitHub
- Docker Compose
- GitHub Actions planned for a later phase
- separate frontend and backend deployment

---

## 🏗️ Planned Architecture

Spendly will use a separated frontend and backend architecture.

```text
Angular Client
      │
      │ HTTPS / JSON
      ▼
Spring Boot REST API
      │
      │ JPA / Hibernate
      ▼
PostgreSQL Database
```

The backend will be responsible for:

- authentication and authorization;
- business rules;
- input validation;
- financial calculations;
- database access;
- API documentation.

The frontend will be responsible for:

- user interaction;
- form validation;
- navigation;
- authenticated API communication;
- dashboard presentation;
- responsive layout.

---

## 🔐 Security Principles

Spendly will follow these security rules:

- passwords will never be stored in plain text;
- authentication will use signed JWT access tokens;
- users will only be able to access their own financial data;
- login attempts will be rate-limited;
- API errors will not expose sensitive implementation details;
- secrets and passwords will be provided through environment variables;
- real credentials will never be committed to Git;
- example configuration will only contain safe placeholder values.

---

## 🧪 Testing Strategy

Backend tests will use isolated test data and a dedicated test configuration.

Planned test levels:

- unit tests for business logic;
- repository integration tests;
- controller tests with MockMvc;
- security and authorization tests;
- validation and error-handling tests.

The H2 database will only be used in the isolated test environment.

The real PostgreSQL development database must not be used for destructive automated tests.

---

## ⚙️ Local Development

Local development instructions will be added after the backend and frontend project structures have been initialized.

The final setup is expected to use environment variables similar to:

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
JWT_SECRET
```

No real credentials should be placed in this README or committed to the repository.

---

## 📚 API Documentation

The backend API will be documented using Springdoc OpenAPI.

Swagger UI will be available in the local development environment after the backend configuration is complete.

The exact URL will be documented once the application configuration has been finalized.

---

## 📁 Repository Structure

The final repository structure will be documented after the frontend and backend projects have been initialized.

A possible high-level organization is:

```text
spendly/
├── backend/
├── frontend/
├── docker-compose.yml
└── README.md
```

This structure is provisional and may change during the project planning phase.

## 🌐 Live Demo

A live demo link will be added after deployment.

---

<p align="center">
  Built with ☕ Java, 🅰️ Angular, and 🐘 PostgreSQL.
</p>

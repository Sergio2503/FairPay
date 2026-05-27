# FairPay

FairPay is a full-stack expense sharing application designed to simplify group payments and balance tracking.

Built with a modern architecture using Kotlin, Ktor, Angular and PostgreSQL.

---

# Features

## Authentication

* JWT authentication
* Secure login system
* User registration

## Groups

* Create groups
* Join groups
* Manage group members

## Expenses

* Add shared expenses
* Split expenses between participants
* Track payments

## Balances

* Automatic balance calculation
* Debt simplification
* Real-time group expense overview

---

# Tech Stack

## Backend

* Kotlin
* Ktor
* Exposed ORM
* PostgreSQL
* JWT Authentication
* Gradle

## Frontend

* Angular 20
* TypeScript
* SCSS
* Standalone Components

---

# Project Structure

```text
FairPay/
 ├── backend-fairpay/
 ├── frontend-fairpay/
 ├── docker-compose.yml
 ├── .gitignore
 └── README.md
```

---

# Requirements

Before running the project locally, install:

* Java 17+
* Node.js LTS
* PostgreSQL
* Git
* Docker (optional)

---

# Backend Setup

## 1. Open backend project

```bash
cd backend-fairpay
```

## 2. Configure PostgreSQL

Create a database named:

```text
fairpay
```

Update database credentials in:

```text
src/main/resources/application.conf
```

## 3. Run backend

```bash
./gradlew run
```

Backend will run on:

```text
http://localhost:8080
```

---

# Frontend Setup

## 1. Open frontend project

```bash
cd frontend-fairpay
```

## 2. Install dependencies

```bash
npm install
```

## 3. Run Angular application

```bash
ng serve
```

Frontend will run on:

```text
http://localhost:4200
```

---

# Docker Setup

Run the complete stack using Docker:

```bash
docker-compose up --build
```

This starts:

* PostgreSQL
* Ktor backend
* Angular frontend

---

# Development Notes

## Backend

The backend architecture is domain-based:

```text
auth/
groups/
expenses/
balances/
database/
config/
```

## Frontend

Frontend structure:

```text
src/app/
 ├── core/
 ├── features/
 │   ├── auth/
 │   ├── dashboard/
 │   ├── expenses/
 │   ├── groups/
 │   └── settings/
```

---

# API

Main backend API:

```text
http://localhost:8080
```

Example endpoints:

```text
POST /auth/register
POST /auth/login
GET /groups
POST /expenses
GET /groups/{groupId}/expenses
```

---

# Current Status

 - Authentication system complete

 - Groups management complete

 - Expenses system complete

 - Balance calculation complete

 - PostgreSQL integration complete

 - Angular frontend in development

---

# Future Improvements

* Real-time updates
* Mobile application
* Notifications
* Multi-currency support
* Dark/light themes
* Receipt scanning

---

# Author

Sergio Muñoz Ferrer

---

# License

This project is licensed under the MIT License.

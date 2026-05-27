# Backend Fairpay

## API
### Ktor + PostgreSQL + JWT

API de autenticación desarrollada con Kotlin usando Ktor, Exposed ORM y PostgreSQL
Implementando registro y autenticación con UUID como identificador primario.

## Tecnologías

- Kotlin

- Ktor

- PostgreSQL

- Exposed ORM

- JWT Authentication

- BCrypt (hash de contraseñas)

## Arquitectura

src/

├── routes/

│     └── AuthRoutes.kt

├── models/

│     └── UsersTable.kt

├── security/

│     ├── JwtConfig.kt

│     └── PasswordHasher.kt

├── database/

│     └── DatabaseFactory.kt

└── Application.kt

## Cómo usar la aplicación

A continuación se detallan los pasos para ejecutar y probar la API correctamente.

### Iniciar PostgreSQL

Asegúrate de que el servidor PostgreSQL esté en ejecución.

### Verificar conexión:

SELECT version();

### Si no tienes creada la base de datos:

CREATE DATABASE auth_db;

### Verificar configuración

En application.conf confirma que los datos coincidan con tu entorno:

database {
url = "jdbc:postgresql://localhost:5432/auth_db"
driver = "org.postgresql.Driver"
user = "postgres"
password = "tu_password"
}

### Crear la tabla users

Ejecutar en PostgreSQL:

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
username VARCHAR(50) NOT NULL,
email VARCHAR(100) NOT NULL UNIQUE,
password_hash VARCHAR(255) NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

### Ejecutar la aplicación

Desde la terminal:

./gradlew run

O ejecutar Application.kt desde IntelliJ.

La API se iniciará en:

http://localhost:8080

### Probar endpoints con Postman
Registro

Método: POST

URL: http://localhost:8080/auth/register

Body → raw → JSON:

{
"username": "nombre",
"email": "nombre@example.com",
"password": "123456"
}

### Login

Método: POST

URL: http://localhost:8080/auth/login

Body → raw → JSON:

{
"email": "nombre@example.com",
"password": "123456"
}
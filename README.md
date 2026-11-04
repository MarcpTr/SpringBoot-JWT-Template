
# JWT Refresh Authentication API

### Spring Boot 3 • Spring Security 6 • JWT • MariaDB
A production-ready, secure RESTful API implementing **JWT Authentication (Access + Refresh Tokens)** using **Spring Boot 3** and **Spring Security 6**.
[Try it](https://api-playground.marcpericot.es/)
[![playground](https://marcpericot.es/images/projects/jwt.webp "playground")](https://api-playground.marcpericot.es/)
The project demonstrates:
* Stateless authentication
* Token revocation and persistence
* Role-Based Access Control (RBAC)
* User-scoped resources (TODO Notes)
* Clean, layered architecture
# Tech Stack

* **Java 17+**
* **Spring Boot 3.x**
* **Spring Security 6**
* **JWT**
* **MariaDB**
* **Spring Data JPA**
* **Maven**


# Features

## Authentication & Authorization

* User registration and login
* JWT-based authentication:
  * Short-lived **Access Tokens**
  * Long-lived **Refresh Tokens**
* Refresh token endpoint
* Token revocation stored in database
* Role-Based Access Control (RBAC)
* Stateless security configuration (no HTTP sessions)
## User authentication
Base path:
```
/api/auth
```
| Endpoint | HTTP Method | Access Level  | Description                                  |
| -------- | ----------- | ------------- | -------------------------------------------- |
| `/register`      | POST        | Public | Register a new user account |
| `/login`      | POST        |  Public| Authenticate a user and return an access token      |
| `/refresh`      | POST        | Public| Generate new access and refresh tokens using a refresh token      |

---
## Sessions
Base path:
```
/api/sessions
```
| Endpoint | HTTP Method | Access Level  | Description                                  |
| -------- | ----------- | ------------- | -------------------------------------------- |
| `/`      | GET| Authenticated | List all active sessions |
| `/`      | DELETE|  Authenticated |Remove all active sessions      |
| `/{id}`      | DELETE        | Authenticated | Remove a specific session    |

## Admin 
Base path:
```
/api/admin
```
| Endpoint | HTTP Method | Access Level  | Description                                  |
| -------- | ----------- | ------------- | -------------------------------------------- |
| `/security/max-sessions`      | PUT| ROLE_ADMIN | Set the maximum number of active sessions per user |

## Notes Module (User-Scoped Resource)

Authenticated users can:
* Create personal notes
* Retrieve only their own notes
* Access protected endpoints using a valid Access Token

Base path:
```
/api/notes
```
| Endpoint | HTTP Method | Access Level  | Description                                  |
| -------- | ----------- | ------------- | -------------------------------------------- |
| `/`      | POST        | Authenticated | Create a new note for the authenticated user |
| `/`      | GET         | Authenticated | Retrieve all notes of the authenticated user      |



## RBAC Demonstration Endpoints

Base path:

```
/api/test
```

| Endpoint  | Access Level  | Description                          |
| --------- | ------------- | ------------------------------------ |
| `/public` | Public        | No authentication required           |
| `/user`   | USER / ADMIN  | Requires `ROLE_USER` or `ROLE_ADMIN` |
| `/admin`  | ADMIN only    | Requires `ROLE_ADMIN`                |
| `/whoami` | Authenticated | Returns username and role            |

These endpoints demonstrate fine-grained authorization using Spring Security method-level security.
# JWT Token Structure

The API uses JSON Web Tokens (JWT) following the standard JWT claims, along with a few custom claims to support authentication and token rotation.

## Common Claims

| Claim | Description |
|-------|-------------|
| `role` | User role (`ROLE_USER` or `ROLE_ADMIN`) used by Spring Security for authorization. |
| `type` | Token type. Can be `ACCESS` or `REFRESH`. This prevents using a refresh token to access protected endpoints. |
| `jti` | Unique identifier of the token (JWT ID). Used to uniquely identify and revoke individual tokens. |
| `iss` | Issuer of the token. Identifies the application that generated the JWT. |
| `sub` | Subject of the token. Contains the authenticated user's username. |
| `aud` | Intended audience of the token. Identifies which application or service the token is meant for. |
| `iat` | Issued At timestamp (Unix time). Indicates when the token was generated. |
| `exp` | Expiration timestamp (Unix time). After this time the token is no longer valid. |

## Access Token Additional Claim

Access tokens include an additional claim:

| Claim | Description |
|-------|-------------|
| `refresh_jti` | Contains the `jti` of the refresh token that generated the access token. This links both tokens together, making it possible to invalidate all access tokens derived from a revoked refresh token and support secure token rotation. |

## Example Access Token Payload

```json
{
  "role": "ROLE_USER",
  "type": "ACCESS",
  "jti": "3b1c4db5-ff8c-4d8f-8e5b-78b19d57d59a",
  "refresh_jti": "f6db8471-7b1c-4d1d-a8cf-2e2dbd3d1844",
  "iss": "jwt-refresh-auth-api",
  "sub": "john_doe",
  "aud": "jwt-refresh-auth-client",
  "iat": 1721000000,
  "exp": 1721000900
}
```

## Example Refresh Token Payload

```json
{
  "role": "ROLE_USER",
  "type": "REFRESH",
  "jti": "f6db8471-7b1c-4d1d-a8cf-2e2dbd3d1844",
  "iss": "jwt-refresh-auth-api",
  "sub": "john_doe",
  "aud": "jwt-refresh-auth-client",
  "iat": 1721000000,
  "exp": 1721604800
}
```

### Notes

- Every token has a unique `jti`, allowing individual revocation.
- The `type` claim ensures that access and refresh tokens cannot be used interchangeably.
- The `refresh_jti` claim is only present in access tokens and creates a secure relationship between an access token and the refresh token that issued it.
- Authorization decisions are based on the `role` claim together with Spring Security's RBAC configuration.
# Architecture

Layered architecture following clean separation of concerns:

```
src/main/java/com/example/jwt_demo
│
├── config        → DataInitializer, Security configuration & JWT filter
├── controller    → AuthController, NoteController, TestController
├── dto           → Authentication & request/response models
├── model         → User, Token, Note, TokenType
├── exception     → BusinessValidationException, BusinessValidationException 
├── repository    → TokenRepository, UserRepository, NoteRepository
├── service       → AuthService, JwtService,TokenService , TokenService, NoteService
└── JwtRefreshAuthApplication.java
```

# Configuration

Edit `application.properties`:

```properties
spring.application.name=jwtRefreshToken-demo
server.port=8080
# DB config
spring.datasource.url=jdbc:mariadb://localhost:3306/jwt_refresh_auth
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect
# JWT config
jwt.secret=084894c7d31418cee00e6958b1f63abf6ef192fb83d88ee3759ca7af4abfdf49
jwt.expiration=864000000
jwt.refresh-expiration=894000000
# Admin
admin.username=Admin
admin.password=u8SDDu322asy8DA
admin.email=admin@admin.es
```

# Database Structure


 1. **Users**

| # | Name      | type                                   | Description|
|---|------------|----------------------------------------|------------|
| 1 | id         | bigint(20)                             |    Unique identifier of the User|
| 2 | created_at | datetime(6)                            |    Date and time when the user was created        |
| 3 | email      | varchar(255)                           |  User’s email address, unique for each account          |
| 4 | password   | varchar(255)                           |  User’s encrypted password          |
| 5 | username   | varchar(255)                           |  Username displayed on the platform        |
| 6 | role       | enum('ROLE_ADMIN', 'ROLE_USER')        |    User role that determines access permissions        |
---
 2. **Tokens**

| # | Name      | type                                   | Description|
|---|------------|----------------------------------------|------------|
| 1 | id         | bigint(20)                             |    Unique identifier of the token|
| 2 | expired| bit(1))                            |    Indicates if the token has expired; set to `1` when the token’s lifetime is over time expired        |
| 3 | revoked| bit(1)                           |  Indicates if the token has been revoked, e.g., after user logout          |
| 4 | token| varchar(512)                           |  The actual token string used for authentication          |
| 5 | token_type| enum('ACCESS', 'REFRESH')                           |  Specifies the purpose of the token: `ACCESS` for API access, `REFRESH` for refreshing access tokens        |
| 6 | user_id| bigint(20)        |    Foreign key referencing the associated user (`users.id`)        |
---
 3. **Notes**

| # | Name      | type                                   | Description|
|---|------------|----------------------------------------|------------|
| 1 | id         | bigint(20)                             |    Unique identifier of the token|
| 2 | title| text                           |    Title of the note       |
| 3 | content|   varchar(255)                          |  Content or body of the note         |
| 4 | created_at| datetime(6)                           |  Date and time when the note was created        |
| 5 | user_id| bigint(20)        |    Foreign key referencing the associated user (`users.id`)        |

---
# Security Design

* Only **Access Tokens** can access protected endpoints (e.g. `/api/notes`)
* Only **Refresh Tokens** are accepted at `/api/auth/refresh`
* Tokens are persisted in the database
* Tokens can be:
  * Revoked
  * Marked as expired
* Access tokens are rotated on refresh
* Stateless authentication (no session storage)

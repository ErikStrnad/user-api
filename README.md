# User API

A simple REST API built with Java Spring Boot that provides endpoints for user registration, authentication using JWT tokens, and user management. The application leverages Spring Security, CORS, Hibernate with JPA, and an in-memory H2 database. Swagger is configured for API testing and documentation.

## Features

- **User Registration:** Register new users with secure password hashing (BCrypt).
- **User Login:** Authenticate users and generate JWT Bearer tokens.
- **Protected Endpoints:** Access user data and list all users using JWT-based authentication.
- **JWT Authentication Filter:** Secure endpoints by validating JWT tokens in incoming requests.
- **CORS Support:** Enable cross-domain requests for development and testing.
- **Swagger Integration:** Interactive API documentation and testing.
- **H2 Database:** In-memory database for rapid development and testing.

## API Endpoints

### Public Endpoints

#### **POST /register**
Registers a new user.

**Request Body:**
```json
{
  "username": "yourUsername",
  "password": "yourPassword"
}
```

**Response:**  
Success message or error if the username already exists.

#### **POST /login**
Authenticates the user and returns a JWT token.

**Request Body:**
```json
{
  "username": "yourUsername",
  "password": "yourPassword"
}
```

**Response:**  
A JSON object containing the JWT token:
```json
{
  "jwt": "your_jwt_token"
}
```

### Protected Endpoints (Require JWT Bearer Token)

#### **GET /getUser**
Returns data of the currently authenticated user.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response:**  
JSON object with user details.

#### **GET /getUsers**
Returns a list of all registered users.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response:**  
JSON array of user details.

## Configuration

The application uses an in-memory H2 database for development. You can access the H2 console at `/h2-console` with the following settings:

- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** *(empty)*

JWT settings are configured in `application.properties`:

- `jwt.secret`: Secret key used for signing JWT tokens.
- `jwt.expiration`: Token expiration time in milliseconds (e.g., 3600000 for 1 hour).

## Running the Application

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/your-repo-name.git
   ```
2. **Navigate to the project directory:**
   ```bash
   cd your-repo-name
   ```
3. **Build and run the application:**

   Using Maven:
   ```bash
   mvn spring-boot:run
   ```

   Or using Gradle:
   ```bash
   ./gradlew bootRun
   ```

4. **Access Swagger UI:**  
   Open your browser and navigate to:
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

## Testing

The project includes integration tests for user registration, login, and accessing protected endpoints. To run the tests, execute:

With Maven:
```bash
mvn test
```

With Gradle:
```bash
./gradlew test
```

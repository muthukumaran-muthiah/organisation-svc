# Organization Service

A Spring Boot microservice for managing organizational groups and user memberships with hierarchical structure and property inheritance.

## 📋 Overview

The Organization Service is a core microservice responsible for managing user groups and their hierarchy. It provides:

- **Full CRUD operations** for groups
- **Hierarchical group structure** with parent-child relationships
- **Property inheritance** - child groups inherit properties from their parent chain
- **User membership management** with atomic operations
- **Redis-based storage** for high performance
- **RESTful API** with comprehensive Swagger documentation

## 🏗️ Architecture

### Technology Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data Redis** for data persistence
- **Redis 7.2** as the primary data store
- **SpringDoc OpenAPI** for API documentation
- **Maven** for build management
- **Docker & Docker Compose** for containerization
- **Testcontainers** for integration testing

### Data Model

Groups are stored as Redis Hashes with the following properties:

| Field | Type | Description |
|-------|------|-------------|
| `uuid` | String | Unique identifier (Primary Key) |
| `parentUuid` | String | Reference to parent group (nullable) |
| `name` | String | Internal name (mandatory) |
| `displayName` | String | Public display name |
| `status` | Enum | ACTIVE or DEACTIVATED |
| `spaceId` | String | Linked Client Space ID (inheritable) |
| `location` | String | Geographic location (inheritable) |
| `language` | String | Language code (inheritable) |
| `segments` | List | Business verticals (inheritable) |

**Inheritable Fields**: `spaceId`, `location`, `language`, `segments`

### Inheritance Logic

When retrieving a group, if any inheritable field is `null`, the service:
1. Traverses up the parent hierarchy
2. Finds the first non-null value
3. Returns the resolved group with inherited properties

## 🚀 Getting Started

### Prerequisites

- **Docker** and **Docker Compose** installed
- **Java 17** (for local development)
- **Maven 3.9+** (for local development)

### Quick Start with Docker

1. **Clone the repository**
   ```bash
   cd /Users/muthiah/Documents/Mukun/Muthukumaran Muthiah_task
   ```

2. **Build and run with Docker Compose**
   ```bash
   docker-compose up --build
   ```

3. **Access the application**
   - API Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/api-docs`

### Local Development Setup

1. **Start Redis**
   ```bash
   docker run -d -p 6379:6379 redis:7.2-alpine
   ```

2. **Build the application**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Run tests**
   ```bash
   mvn test
   ```

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Group Management Endpoints

#### 1. Create a Group
**POST** `/groups`

Creates a new group with optional parent reference.

**Request Body:**
```json
{
  "parentUuid": "parent-uuid-123",
  "name": "Engineering Team",
  "displayName": "Engineering Team - US",
  "status": "ACTIVE",
  "spaceId": "space-123",
  "location": "USA",
  "language": "en-US",
  "segments": ["Corporate", "Education"]
}
```

**Response:** `201 Created`
```json
{
  "uuid": "generated-uuid",
  "parentUuid": "parent-uuid-123",
  "name": "Engineering Team",
  "displayName": "Engineering Team - US",
  "status": "ACTIVE",
  "spaceId": "space-123",
  "location": "USA",
  "language": "en-US",
  "segments": ["Corporate", "Education"]
}
```

**Business Rules:**
- `name` is mandatory
- `displayName` defaults to `name` if not provided
- `status` defaults to `ACTIVE`
- Parent UUID is validated (must exist)

#### 2. Get a Group with Inheritance
**GET** `/groups/{uuid}`

Retrieves a group with inherited properties from parent hierarchy.

**Response:** `200 OK`
```json
{
  "uuid": "child-uuid",
  "parentUuid": "parent-uuid",
  "name": "Frontend Team",
  "displayName": "Frontend Team",
  "status": "ACTIVE",
  "spaceId": "space-inherited",
  "location": "USA",
  "language": "en-US",
  "segments": ["Corporate"]
}
```

#### 3. Update a Group
**PUT** `/groups/{uuid}`

Updates an existing group's properties.

**Request Body:**
```json
{
  "name": "Updated Engineering Team",
  "location": "France",
  "segments": ["Healthcare", "Finance"]
}
```

**Response:** `200 OK`

#### 4. Delete a Group
**DELETE** `/groups/{uuid}`

Deletes a group if it has no child groups.

**Response:** `204 No Content`

**Business Rule:** A group can only be deleted if it has no sub-groups.

### User Membership Endpoints

#### 5. Add User to Group
**POST** `/groups/{uuid}/users`

Adds a user to the specified group.

**Request Body:**
```json
{
  "userId": "user-123"
}
```

**Response:** `200 OK`

#### 6. Remove User from Group
**DELETE** `/groups/{uuid}/users/{userId}`

Removes a user from the specified group.

**Response:** `204 No Content`

#### 7. Get Users in Group
**GET** `/groups/{uuid}/users`

Retrieves all users in the specified group.

**Response:** `200 OK`
```json
["user-123", "user-456", "user-789"]
```

#### 8. Move User Between Groups
**PUT** `/users/{userId}/move`

Atomically moves a user from their current group to a target group.

**Request Body:**
```json
{
  "targetGroupUuid": "target-group-uuid"
}
```

**Response:** `200 OK`

**Important:** This is an atomic operation using Redis transactions.

## 🧪 Testing

The application includes comprehensive integration tests:

### InheritanceIntegrationTest
Tests the core inheritance logic:
- Creates a 3-level hierarchy (Root → Parent → Child)
- Verifies that child groups inherit properties correctly
- Tests multiple levels of inheritance

### OrganizationServiceIntegrationTest
Tests all CRUD operations and business rules:
- Group creation with validation
- Update and delete operations
- User membership management
- Move user operations
- Error scenarios

**Run tests:**
```bash
mvn test
```

**Note:** Tests use Testcontainers to spin up a real Redis instance.

## 🐳 Docker

### Build Docker Image
```bash
docker build -t organization-service:latest .
```

### Run with Docker Compose
```bash
docker-compose up -d
```

### Stop Services
```bash
docker-compose down
```

### View Logs
```bash
docker-compose logs -f organization-service
```

## 📊 Redis Data Structure

### Groups
Stored as Redis Hashes:
```
Key: Group:{uuid}
Hash Fields:
  - uuid
  - parentUuid
  - name
  - displayName
  - status
  - spaceId
  - location
  - language
  - segments (JSON array)
```

### User Membership
Stored as Redis Sets:
```
Key: group:{uuid}:users
Type: Set
Members: [userId1, userId2, ...]
```

### User-to-Group Mapping
Stored as Redis Strings:
```
Key: user:{userId}:group
Type: String
Value: groupUuid
```

## 🔧 Configuration

### Application Properties

**Redis Configuration:**
```yaml
spring.data.redis.host: localhost
spring.data.redis.port: 6379
spring.data.redis.timeout: 2000ms
```

**Server Configuration:**
```yaml
server.port: 8080
```

**Environment Variables:**
- `REDIS_HOST`: Redis server hostname (default: `localhost`)
- `REDIS_PORT`: Redis server port (default: `6379`)

## 📖 Example Usage

### Create a Hierarchy

**1. Create Root Group:**
```bash
curl -X POST http://localhost:8080/api/v1/groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Global Organization",
    "spaceId": "space-global",
    "location": "USA",
    "language": "en-US",
    "segments": ["Corporate", "Education"]
  }'
```

**2. Create Child Group:**
```bash
curl -X POST http://localhost:8080/api/v1/groups \
  -H "Content-Type: application/json" \
  -d '{
    "parentUuid": "{root-uuid}",
    "name": "Engineering",
    "spaceId": "space-eng"
  }'
```

**3. Create Grandchild:**
```bash
curl -X POST http://localhost:8080/api/v1/groups \
  -H "Content-Type: application/json" \
  -d '{
    "parentUuid": "{child-uuid}",
    "name": "Frontend Team"
  }'
```

**4. Get Grandchild with Inheritance:**
```bash
curl -X GET http://localhost:8080/api/v1/groups/{grandchild-uuid}
```

The response will include inherited properties:
- `spaceId` from Engineering
- `location`, `language`, `segments` from Global Organization

## 🛠️ Project Structure

```
organization-service/
├── src/
│   ├── main/
│   │   ├── java/com/muthukumaran-muthiah/organization/
│   │   │   ├── config/              # Configuration classes
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── RedisConfig.java
│   │   │   ├── controller/          # REST controllers
│   │   │   │   ├── GroupController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── GroupCreateRequest.java
│   │   │   │   ├── GroupUpdateRequest.java
│   │   │   │   ├── GroupResponse.java
│   │   │   │   ├── AddUserRequest.java
│   │   │   │   └── MoveUserRequest.java
│   │   │   ├── exception/           # Custom exceptions
│   │   │   │   ├── GroupNotFoundException.java
│   │   │   │   ├── ParentGroupNotFoundException.java
│   │   │   │   ├── GroupHasChildrenException.java
│   │   │   │   ├── UserNotFoundException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── model/               # Domain entities
│   │   │   │   ├── Group.java
│   │   │   │   └── GroupStatus.java
│   │   │   ├── repository/          # Data access layer
│   │   │   │   └── GroupRepository.java
│   │   │   ├── service/             # Business logic
│   │   │   │   └── GroupService.java
│   │   │   └── OrganizationServiceApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       ├── java/com/muthukumaran-muthiah/organization/
│       │   ├── InheritanceIntegrationTest.java
│       │   └── OrganizationServiceIntegrationTest.java
│       └── resources/
│           └── application.yml
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## 🎯 Key Features Implemented

✅ **Full CRUD Operations** for groups  
✅ **Parent-child hierarchical structure**  
✅ **Property inheritance** with recursive traversal  
✅ **Parent validation** before group creation  
✅ **Child validation** before group deletion  
✅ **User membership management** with Redis Sets  
✅ **Atomic user move** operation  
✅ **Swagger/OpenAPI documentation**  
✅ **Docker containerization**  
✅ **Docker Compose orchestration**  
✅ **Comprehensive integration tests**  
✅ **Global exception handling**  
✅ **Input validation**  
✅ **Circular reference prevention**  

## 🚨 Error Handling

The application provides clear error messages:

- **404 Not Found**: Group or user not found
- **404 Not Found**: Parent group not found during creation
- **409 Conflict**: Cannot delete group with children
- **400 Bad Request**: Validation errors

**Example Error Response:**
```json
{
  "timestamp": "2025-12-30T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Group not found with UUID: abc-123",
  "path": "/api/v1/groups/abc-123"
}
```

## 📝 Notes

- Groups use UUID as primary key
- User membership is tracked using Redis Sets for O(1) operations
- The service prevents circular references in the hierarchy
- All timestamps and errors follow ISO-8601 format
- The inheritance logic traverses the full parent chain

## 🤝 Contributing

1. Ensure all tests pass
2. Follow the existing code style
3. Add tests for new features
4. Update documentation as needed

## 📄 License

This project is part of the Muthukumaran Muthiah technical assessment.

---

**Happy Coding! 🎉**

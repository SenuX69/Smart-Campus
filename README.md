# Smart Campus Sensor & Room Management API

A RESTful web service for managing campus rooms, IoT sensors, and sensor readings, built with **JAX-RS (Jersey 2.35)** and deployed on **Apache Tomcat**.

---

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Architecture & Design](#architecture--design)
- [Data Models](#data-models)
- [API Endpoints](#api-endpoints)
  - [Root Discovery](#root-discovery)
  - [Rooms](#rooms)
  - [Sensors](#sensors)
  - [Sensor Readings (Sub-Resource)](#sensor-readings-sub-resource)
- [Business Logic & Constraints](#business-logic--constraints)
- [Error Handling](#error-handling)
- [Cross-Cutting Concerns](#cross-cutting-concerns)
- [Sample Data](#sample-data)
- [Build & Deployment](#build--deployment)
- [Testing with cURL](#testing-with-curl)

---

## Overview

The **SmartCampusAPI** provides a centralised interface for monitoring and managing smart building infrastructure across a university campus. It enables facilities teams to:

- Register and manage **rooms** with capacity metadata.
- Deploy, relocate, and retire **sensors** (temperature, occupancy, CO₂, etc.) within rooms.
- Ingest and retrieve **sensor readings** — the time-series data produced by hardware devices.

All communication uses **JSON over HTTP**, following REST architectural constraints.

---

## Technology Stack

| Component           | Technology                          |
|---------------------|-------------------------------------|
| Language            | Java 8                              |
| Framework           | JAX-RS 2.1 (Jersey 2.35)            |
| DI Container        | HK2 (via `jersey-hk2`)              |
| JSON Binding        | Jackson (via `jersey-media-json-jackson`) |
| Build Tool          | Apache Maven                        |
| Servlet Container   | Apache Tomcat 9.x                   |
| Packaging           | WAR                                 |
| IDE                 | Apache NetBeans                      |

---

## Project Structure

```
SmartCampusAPI/
├── pom.xml                                         # Maven project descriptor
├── src/
│   └── main/
│       ├── java/com/smartcampus/
│       │   ├── config/
│       │   │   ├── ApplicationConfig.java          # JAX-RS application configuration
│       │   │   └── LoggingFilter.java              # Request/response logging filter
│       │   ├── exception/
│       │   │   ├── GenericExceptionMapper.java      # Global catch-all (500)
│       │   │   ├── LinkedResourceNotFoundException.java
│       │   │   ├── LinkedResourceNotFoundExceptionMapper.java  # 422 mapper
│       │   │   ├── RoomNotEmptyException.java
│       │   │   ├── RoomNotEmptyExceptionMapper.java            # 409 mapper
│       │   │   ├── SensorUnavailableException.java
│       │   │   └── SensorUnavailableExceptionMapper.java       # 403 mapper
│       │   ├── model/
│       │   │   ├── ErrorResponse.java               # Standardised error POJO
│       │   │   ├── Room.java                        # Room entity
│       │   │   ├── Sensor.java                      # Sensor entity
│       │   │   └── SensorReading.java               # Sensor reading entity
│       │   ├── resource/
│       │   │   ├── RootResource.java                # GET /api/v1 — API discovery
│       │   │   ├── RoomResource.java                # CRUD for /api/v1/rooms
│       │   │   ├── SensorResource.java              # CRUD for /api/v1/sensors
│       │   │   └── SensorReadingResource.java       # Sub-resource for readings
│       │   └── service/
│       │       ├── RoomService.java                 # Room business logic & data store
│       │       ├── SensorService.java               # Sensor business logic & data store
│       │       └── SensorReadingService.java        # Reading data store
│       └── webapp/
│           └── WEB-INF/
│               └── web.xml                          # Servlet mapping configuration
└── target/                                          # Build output (WAR file)
```

---

## Architecture & Design

### Layered Architecture

The application follows a **two-tier layered architecture**:

```
┌─────────────────────────────────────────────────┐
│                  Resource Layer                  │
│   (JAX-RS annotated classes — HTTP interface)    │
├─────────────────────────────────────────────────┤
│                  Service Layer                   │
│   (Business logic & in-memory data storage)      │
└─────────────────────────────────────────────────┘
```

- **Resource Layer** — Maps HTTP verbs and URIs to Java methods. Handles request validation, response construction, and HTTP status codes.
- **Service Layer** — Encapsulates business rules and manages the in-memory `LinkedHashMap` data stores.

### Key Design Patterns

| Pattern                   | Implementation                                                                 |
|---------------------------|--------------------------------------------------------------------------------|
| **Sub-Resource Locator**  | `SensorResource.getSensorReadings()` delegates to `SensorReadingResource`      |
| **Exception Mapper**      | Custom `ExceptionMapper<T>` implementations convert domain exceptions to HTTP responses |
| **Container Filter**      | `LoggingFilter` implements both `ContainerRequestFilter` and `ContainerResponseFilter` |
| **HATEOAS Discovery**     | `RootResource` returns resource collection URIs at the API root                |

### Configuration

The application is configured via two mechanisms:

1. **`ApplicationConfig.java`** — Programmatic JAX-RS configuration using `@ApplicationPath("/api/v1")`. Explicitly registers all resource classes, exception mappers, and filters.
2. **`web.xml`** — Maps the Jersey `ServletContainer` to the `/api/v1/*` URL pattern with `load-on-startup=1`.

---

## Data Models

### Room

| Field       | Type           | Description                                     |
|-------------|----------------|-------------------------------------------------|
| `id`        | `String`       | Unique identifier (e.g., `"LIB-301"`)           |
| `name`      | `String`       | Human-readable name (e.g., `"Library Quiet Study"`) |
| `capacity`  | `int`          | Maximum occupancy                                |
| `sensorIds` | `List<String>` | IDs of sensors deployed in this room             |

### Sensor

| Field          | Type     | Description                                          |
|----------------|----------|------------------------------------------------------|
| `id`           | `String` | Unique identifier (e.g., `"TEMP-001"`)               |
| `type`         | `String` | Category: `"Temperature"`, `"Occupancy"`, `"CO2"` etc. |
| `status`       | `String` | Current state: `"ACTIVE"`, `"MAINTENANCE"`, or `"OFFLINE"` |
| `currentValue` | `double` | Most recent measurement                               |
| `roomId`       | `String` | Foreign key linking to the parent Room                |

### SensorReading

| Field       | Type     | Description                                      |
|-------------|----------|--------------------------------------------------|
| `id`        | `String` | Auto-generated UUID                               |
| `timestamp` | `long`   | Epoch milliseconds when the reading was captured  |
| `value`     | `double` | The recorded metric value                         |

### ErrorResponse

| Field     | Type     | Description                                    |
|-----------|----------|------------------------------------------------|
| `error`   | `String` | Error category (e.g., `"Conflict"`)            |
| `message` | `String` | Human-readable description                     |
| `status`  | `int`    | HTTP status code                                |

---

## API Endpoints

**Base URL:** `http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1`

### Root Discovery

| Method | URI          | Description                                | Status  |
|--------|--------------|--------------------------------------------|---------|
| `GET`  | `/api/v1`    | Returns API metadata and resource links    | `200`   |

**Response Example:**
```json
{
  "name": "Smart Campus Sensor & Room Management API",
  "version": "1.0",
  "description": "RESTful API for managing campus rooms, sensors, and sensor readings",
  "contact": "admin@smartcampus.westminster.ac.uk",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

---

### Rooms

| Method   | URI                    | Description                 | Success Status | Error Statuses          |
|----------|------------------------|-----------------------------|----------------|-------------------------|
| `GET`    | `/api/v1/rooms`        | List all rooms              | `200 OK`       | —                       |
| `GET`    | `/api/v1/rooms/{id}`   | Get room by ID              | `200 OK`       | `404 Not Found`         |
| `POST`   | `/api/v1/rooms`        | Create a new room           | `201 Created`  | `400`, `409 Conflict`   |
| `PUT`    | `/api/v1/rooms/{id}`   | Update an existing room     | `200 OK`       | `404 Not Found`         |
| `DELETE` | `/api/v1/rooms/{id}`   | Delete a room               | `204 No Content` | `409 Conflict`        |

**POST Request Body:**
```json
{
  "id": "SCI-401",
  "name": "Science Lab B",
  "capacity": 40
}
```

**Business Rule:** A room cannot be deleted if it still has sensors assigned to it → `409 Conflict`.

---

### Sensors

| Method   | URI                       | Description                     | Success Status   | Error Statuses              |
|----------|---------------------------|---------------------------------|------------------|-----------------------------|
| `GET`    | `/api/v1/sensors`         | List all sensors                | `200 OK`         | —                           |
| `GET`    | `/api/v1/sensors?type=X`  | Filter sensors by type          | `200 OK`         | —                           |
| `GET`    | `/api/v1/sensors/{id}`    | Get sensor by ID                | `200 OK`         | `404 Not Found`             |
| `POST`   | `/api/v1/sensors`         | Create a new sensor             | `201 Created`    | `400`, `409`, `422`         |
| `PUT`    | `/api/v1/sensors/{id}`    | Update an existing sensor       | `200 OK`         | `404`, `422`                |
| `DELETE` | `/api/v1/sensors/{id}`    | Delete a sensor                 | `204 No Content` | —                           |

**POST Request Body:**
```json
{
  "id": "TEMP-002",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 21.0,
  "roomId": "LIB-301"
}
```

**Business Rule:** The `roomId` must reference an existing room → `422 Unprocessable Entity` if invalid.

---

### Sensor Readings (Sub-Resource)

Accessed via the **Sub-Resource Locator** pattern at `/api/v1/sensors/{sensorId}/readings`.

| Method | URI                                        | Description                    | Success Status   | Error Statuses        |
|--------|--------------------------------------------|--------------------------------|------------------|-----------------------|
| `GET`  | `/api/v1/sensors/{sensorId}/readings`      | Get all readings for a sensor  | `200 OK`         | `404 Not Found`       |
| `POST` | `/api/v1/sensors/{sensorId}/readings`      | Submit a new reading           | `201 Created`    | `403 Forbidden`, `404`|

**POST Request Body:**
```json
{
  "value": 23.7
}
```

> The `id` (UUID) and `timestamp` (epoch ms) are **auto-generated** by the server if not provided.

**Business Rule:** Sensors in `"MAINTENANCE"` status cannot accept new readings → `403 Forbidden`.

**Side Effect:** On successful creation, the parent sensor's `currentValue` is updated to the new reading's value.

---

## Business Logic & Constraints

| Constraint                       | Trigger                                                     | HTTP Response               |
|----------------------------------|-------------------------------------------------------------|-----------------------------|
| **Room Deletion Guard**          | `DELETE /rooms/{id}` when room still has sensors            | `409 Conflict`              |
| **Dependency Validation**        | `POST /sensors` or `PUT /sensors/{id}` with invalid `roomId`| `422 Unprocessable Entity`  |
| **Sensor State Constraint**      | `POST /sensors/{id}/readings` when sensor status = `MAINTENANCE` | `403 Forbidden`        |
| **Duplicate ID Prevention**      | `POST /rooms` or `POST /sensors` with an existing ID        | `409 Conflict`              |
| **Required Field Validation**    | `POST /rooms` without `id` or `name`; `POST /sensors` without `id` | `400 Bad Request`    |
| **Idempotent Delete**            | `DELETE /rooms/{id}` or `DELETE /sensors/{id}` for non-existent resource | `204 No Content` |

---

## Error Handling

The API uses a structured exception handling strategy with **three custom exception mappers** and one **global catch-all**:

| Exception                           | HTTP Status                 | Mapper Class                               |
|-------------------------------------|-----------------------------|--------------------------------------------|
| `RoomNotEmptyException`             | `409 Conflict`              | `RoomNotEmptyExceptionMapper`              |
| `LinkedResourceNotFoundException`   | `422 Unprocessable Entity`  | `LinkedResourceNotFoundExceptionMapper`    |
| `SensorUnavailableException`        | `403 Forbidden`             | `SensorUnavailableExceptionMapper`         |
| Any uncaught `Throwable`            | `500 Internal Server Error` | `GenericExceptionMapper`                   |

All error responses follow a **standardised JSON format** via the `ErrorResponse` model:

```json
{
  "error": "Conflict",
  "message": "Cannot delete room 'LIB-301' because it still has sensors assigned to it.",
  "status": 409
}
```

The `GenericExceptionMapper` acts as a safety net — it logs the full stack trace server-side while returning a generic message to the client, preventing information disclosure.

---

## Cross-Cutting Concerns

### Request & Response Logging

The `LoggingFilter` is a JAX-RS container filter that implements both `ContainerRequestFilter` and `ContainerResponseFilter`:

- **Request phase** — Logs the HTTP method and request URI; records a start timestamp.
- **Response phase** — Logs the HTTP method, URI, response status code, and calculated response duration in milliseconds.

**Example log output:**
```
[REQUEST] GET http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/rooms
[RESPONSE] GET http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/rooms — Status: 200 — Duration: 12ms
```

---

## Sample Data

The application ships with pre-populated data for immediate testing:

### Rooms

| ID        | Name                | Capacity |
|-----------|---------------------|----------|
| `LIB-301` | Library Quiet Study | 50       |
| `ENG-101` | Engineering Lab A   | 30       |
| `LEC-201` | Main Lecture Hall   | 200      |

### Sensors

| ID        | Type        | Status        | Current Value | Room ID   |
|-----------|-------------|---------------|---------------|-----------|
| `TEMP-001`| Temperature | ACTIVE        | 22.5          | `LIB-301` |
| `OCC-001` | Occupancy   | ACTIVE        | 35.0          | `LEC-201` |
| `CO2-001` | CO2         | MAINTENANCE   | 410.0         | `ENG-101` |

---

## Build & Deployment

### Prerequisites

- **Java 8+** (JDK)
- **Apache Maven 3.x**
- **Apache Tomcat 9.x**

### Build

```bash
cd SmartCampusAPI
mvn clean package
```

This produces the WAR file at:
```
target/SmartCampusAPI-1.0-SNAPSHOT.war
```

### Deploy to Tomcat

Copy the WAR file into Tomcat's `webapps` directory:

```bash
cp target/SmartCampusAPI-1.0-SNAPSHOT.war /path/to/tomcat/webapps/
```

Start (or restart) Tomcat:

```bash
/path/to/tomcat/bin/startup.sh
```

The API will be available at:
```
http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1
```

---

## Testing with cURL

### Discover the API

```bash
curl http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1
```

### List All Rooms

```bash
curl http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/rooms
```

### Create a Room

```bash
curl -X POST http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "SCI-401", "name": "Science Lab B", "capacity": 40}'
```

### Create a Sensor (with dependency validation)

```bash
curl -X POST http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "TEMP-002", "type": "Temperature", "status": "ACTIVE", "currentValue": 20.5, "roomId": "LIB-301"}'
```

### Filter Sensors by Type

```bash
curl http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/sensors?type=Temperature
```

### Submit a Sensor Reading

```bash
curl -X POST http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 23.7}'
```

### Attempt to Post Reading to Maintenance Sensor (triggers 403)

```bash
curl -X POST http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 500.0}'
```

### Delete a Room with Sensors (triggers 409)

```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI-1.0-SNAPSHOT/api/v1/rooms/LIB-301
```

---

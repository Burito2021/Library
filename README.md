# Library

## Project Overview

This project is a RESTFUL API application designed to manage user registration and profile update. Key features:
User registration: capture essential details such as name, user ID, contact information
(email, phone number), address. Library staff approval: enable library staff to confirm new user registration. User's
photo upload: allow users to attach their photos to their profile. Unique ID assignment: assign a unique id upon
registration Profile viewing: retrieve information on any user in the system Profile update: update non-sensitive
information on a user

### Requirements

This project requires the following software:

- Programming Language: Java 17
- Build Tool: Gradle 8.8
- Database: PostgreSQL latest
- Other: Docker 27.2.0+

## Installation

- docker-compose up to deploy Postgre
- ./gradlew build
- ./gradlew bootRun
- access the application at http://localhost:1280

## API endpoints

- POST /api/v1/users Description: Registers a new user with details like name, email, phone, and address. Request
  Example:
  POST /api/v1/users Content-Type: application/json X-Correlation-Id:0ea51ac3-7459-4bcc-adea-88c81cc24992 {
  "username":"KimChim",
  "name":"Kane",
  "surname":"Manrow",
  "email":"fsadfa@gmail.com",
  "phoneNumber":"380679920267",
  "address":"asfasfdsa"  
  }


- GET /api/v1/users Description: retrieving a user without filters (default params)
  Request Example:
  GET /api/v1/users Content-Type: application/json X-Correlation-Id:0ea51ac3-7459-4bcc-adea-88c81cc24992


- GET /api/v1/users?username=user&page=0&size=10&start_time=2012-01-01T00:00&end_time=2012-01-01T00:00 Description:
  retrieving a user with filters (username= filtering by username,page =0, size of the page = 10,range from start_time
  = (the time of createdAt)
  to end_time = (the time of createdAt))
  Request Example:
  GET /api/v1/users Content-Type: application/json X-Correlation-Id:0ea51ac3-7459-4bcc-adea-88c81cc24992


- DELETE /api/v1/users Description: delete all the users in the system Request Example:
  DELETE /api/v1/users Content-Type: application/json X-Correlation-Id:0ea51ac3-7459-4bcc-adea-88c81cc24992


- DELETE /api/v1/users/UUID Description: delete a single user by ID in the system Request Example:
  DELETE /api/v1/users/0ea51ac3-7459-4bcc-adea-88c81cc24992 Content-Type: application/json X-Correlation-Id:
  0ea51ac3-7459-4bcc-adea-88c81cc24992

## Configuration

application-local, application-stage in main depending on env

## Testing

./gradlew test

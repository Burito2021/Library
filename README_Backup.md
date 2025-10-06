# 📚 Library REST API

## 🚀 Project Overview

This is a RESTful API application built to manage user registration and profile updates in a library system.

### 🔑 Key Features

- **User Registration**: Capture essential details (name, username, contact info, address).
- **Library Staff Approval**: Allow staff to approve new user registrations.
- **Photo Upload**: Enable users to attach a profile photo.
- **Unique ID Assignment**: Assign a UUID to each new user.
- **Profile Viewing**: Retrieve user profile data.
- **Profile Updates**: Update non-sensitive information (e.g., address, phone).

---
## 🧰 Requirements

This project requires the following software:

| Tool              | Version |
|-------------------|---------|
| Java              | 21      |
| Gradle            | 8.8     |
| PostgreSQL        | Latest  |
| Docker            | 27.2.0+ |

---

## ⚙️ Installation & Running

1. **Start PostgreSQL**:
   ```bash
   docker-compose up
2. **Build the application**:  
   ./gradlew build
3. **Run the application**:
   ./gradlew bootRun
4. **Access the app**:
   Open http://localhost:1280 in your browser.


📡 API Endpoints

➕ Register User
POST /api/v1/users
Registers a new user with full profile information.

Headers:
Content-Type: application/json  
X-Correlation-Id: 0ea51ac3-7459-4bcc-adea-88c81cc24992
Request Body:
{
"username": "KimChim",
"name": "Kane",
"surname": "Manrow",
"email": "fsadfa@gmail.com",
"phoneNumber": "380679920267",
"address": "asfasfdsa"
}

📖 Get Users
GET /api/v1/users
Retrieves users with default pagination and no filters.

GET /api/v1/users?username=user&page=0&size=10&start_time=2012-01-01T00:00&end_time=2012-01-01T00:00
Retrieves users filtered by:

-username

-page and size for pagination

-start_time, end_time for createdAt range

Headers:
Content-Type: application/json  
X-Correlation-Id: 0ea51ac3-7459-4bcc-adea-88c81cc24992

🗑️ Delete All Users
DELETE /api/v1/users
Deletes all user records from the system.

❌ Delete User by ID
DELETE /api/v1/users/{uuid}
Deletes a user by their UUID.

Example:
DELETE /api/v1/users/0ea51ac3-7459-4bcc-adea-88c81cc24992

🛠️ Configuration
Environment-specific configurations:

application-local.yml

application-stage.yml

Located in the src/main/resources directory.

🧪 Testing
Run unit and integration tests:
./gradlew test
📬 Contact
For any questions, please contact the project maintainer.

---

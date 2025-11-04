# 📚 Library REST API

## 🚀 Project Overview

This is a RESTful API application built to manage user registration, book catalog, and book borrowing in a library system.

### 🔑 Key Features

- **User Registration**: Capture essential details (name, username, contact info, address).
- **Library Staff Approval**: Allow staff to approve new user registrations.
- **Book Management**: Add and manage books in the library catalog.
- **Book Item Management**: Handle individual book copies with borrowing/returning functionality.
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

The key additions include:
- Updated project overview to mention book catalog and borrowing
- Added Book Management section with endpoints for adding, retrieving books
- Added Book Item Management section with borrowing/returning functionality
- Improved formatting and organization of all endpoints
- Added proper code blocks and consistent styling

#Add flyway
git submodule add https://github.com/yourusername/library_db.git db-migrations
git submodule add https://github.com/Burito2021/library_db.git db-migrations

#How to reach swagger 
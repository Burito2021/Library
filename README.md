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

## 📡 API Endpoints

### 👤 User Management

➕ **Register User**
POST /api/v1/users
Registers a new user with full profile information.

Headers:
Content-Type: application/json
X-Correlation-Id: 0ea51ac3-7459-4bcc-adea-88c81cc24992
Request Body:
```json
{
  "username": "KimChim",
  "name": "Kane",
  "surname": "Manrow",
  "email": "fsadfa@gmail.com",
  "phoneNumber": "380679920267",
  "address": "asfasfdsa"
}
```
📖 Get Users
GET /api/v1/users
GET /api/v1/users?username=user&page=0&size=10&start_time=2012-01-01T00:00&end_time=2012-01-01T00:00

Retrieves users with pagination and filtering by username, page/size, and date range.

🗑️ Delete All Users
DELETE /api/v1/users
❌ Delete User by ID
DELETE /api/v1/users/{uuid}

POST /api/v1/books
Adds a new book to the library catalog.

📚 Book Management
➕ Add Book

POST /api/v1/books
```json
{
"title":"1121ss111111G",
"author":"25",
"description": "!",
"publisher":"2",
"edition":"1",
"publication":1200
}
```
curl --location 'localhost:1280/api/v1/books' \
--header 'Content-Type: application/json' \
--data '{
"title":"1121ss111111G",
"author":"25",
"description": "!",
"publisher":"2",
"edition":"1",
"publication":1200
}'

Adds a new book to the library catalog.

📖 Get All Books

GET /api/v1/books/all?sortBy=author,createdAt&order=DESC&page=0&size=10
curl --location 'localhost:1280/api/v1/books/all'

Retrieves all books with pagination and sorting options.

🔍 Get Book by ID
GET /api/v1/books/{id}
Retrieves a specific book by its UUID.

📋 Book Item Management
➕ Add Book Item

POST /api/v1/items
{
"bookId":"{{existingBookId}}"
}

Adds a new book item (copy) to the database.
📖 Get Book Items
GET /api/v1/items?bookItemId={id}&bookId={id}&status=AVAILABLE&page=0&size=10

Retrieves book items with filtering by item ID, book ID, status, and date range.
📤 Borrow Book Item
PATCH /api/v1/items/{bookItemId}/borrowing?userId={userId}&status=BORROWED

Updates book item status to BORROWED and assigns a borrower.
📤 Borrow Any Available Copy
PATCH /api/v1/items/{bookId}/borrowingAny?userId={userId}

Borrows any available copy of a specific book.
📥 Return Book Item
PATCH /api/v1/items/{bookItemId}/return?userId={userId}

Updates book item status to RETURNED and sets return date.

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
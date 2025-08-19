# Personal Finance Manager

A comprehensive web application for managing personal finances built with Spring Boot, Thymeleaf, and MySQL.

## Features

- User authentication and authorization
- Transaction management (income and expenses)
- Budget tracking and management
- Financial dashboard with visualizations
- Category-based expense tracking
- Monthly and yearly financial reports

## Technologies Used

- **Backend**: Spring Boot 3.2.0
- **Frontend**: Thymeleaf, Bootstrap 5
- **Database**: MySQL
- **Security**: Spring Security
- **Build Tool**: Maven
- **Java Version**: 17+

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+

## Getting Started

### 1. Database Setup

Create a MySQL database:
```sql
CREATE DATABASE personal_finance_manager;
```

### 2. Configure Database

Update the database configuration in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/personal_finance_manager
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build and Run

```bash
# Clean and install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on http://localhost:8080

## Project Structure

```
src/
├── main/
│   ├── java/com/finance/manager/
│   │   ├── controller/          # Web controllers
│   │   ├── model/              # Entity classes
│   │   ├── repository/         # Data access layer
│   │   ├── service/            # Business logic
│   │   ├── config/             # Configuration classes
│   │   └── PersonalFinanceManagerApplication.java
│   └── resources/
│       ├── templates/          # Thymeleaf templates
│       ├── static/             # CSS, JS, images
│       └── application.properties
└── test/                       # Test classes
```

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.

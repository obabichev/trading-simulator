# Trading Simulator

A Kotlin-based trading strategy simulator that tests strategies on historical market data.

## Quick Start

### Prerequisites

- JDK 17 or higher
- Docker and Docker Compose

### Setup

1. **Start the database:**
   ```bash
   docker-compose up -d
   ```

2. **Run tests:**
   ```bash
   ./gradlew test
   ```

3. **Build the project:**
   ```bash
   ./gradlew build
   ```

### Database Connection

The PostgreSQL database runs in Docker and is accessible at:
- **URL:** `localhost:5455`
- **Database:** `trading_dev`
- **Username:** `trading_user`
- **Password:** `trading_password`

### Database Migrations

Database schema is managed with Flyway. Migrations are in `src/main/resources/db/migration/`.

Tests automatically clean and migrate the database on startup for a fresh state.

## Project Structure

```
trading-simulator/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   ├── schema/          # Kodama table definitions
│   │   │   └── Main.kt
│   │   └── resources/
│   │       └── db/migration/    # Flyway SQL migrations
│   └── test/
│       ├── kotlin/              # Test sources
│       └── resources/
│           └── test.properties  # Test configuration
├── docker-compose.yml           # PostgreSQL database
└── ROADMAP.md                   # Project roadmap
```

## Technologies

- **Kotlin 2.2.21** - Programming language
- **Gradle** - Build tool
- **PostgreSQL 16** - Database
- **Flyway** - Database migrations
- **Kodama** - Type-safe SQL query builder
- **JUnit 5** - Testing framework

## Development

See [CLAUDE.md](CLAUDE.md) for detailed development instructions.

## Roadmap

See [ROADMAP.md](ROADMAP.md) for project phases and planned features.

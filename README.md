# Tracking Fleet: Real-Time Vehicle Tracking API

## Configuration

Database credentials are **not** stored in the repository. The application reads them from
environment variables:

| Variable | Description | Default |
| --- | --- | --- |
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/tracking_db` |
| `DB_USERNAME` | Database username | required |
| `DB_PASSWORD` | Database password | required |
| `SERVER_PORT` | HTTP port | `8081` |

Example:

```bash
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
./gradlew bootRun
```

### Local profile

For local development, copy the example file and fill in your credentials:

```bash
cp config/application-local.example.yaml config/application-local.yaml
```

`config/application-local.yaml` is ignored by Git. Run the application with the `local` profile:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

# Tracking Fleet: Real-Time Vehicle Tracking API

## API

### Vehicles

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/vehicles` | Create a vehicle |
| `GET` | `/api/v1/vehicles` | List vehicles (paged) |
| `GET` | `/api/v1/vehicles/{id}` | Get a vehicle |
| `PUT` | `/api/v1/vehicles/{id}` | Update a vehicle |
| `PATCH` | `/api/v1/vehicles/{id}/status` | Change vehicle status |
| `PATCH` | `/api/v1/vehicles/{id}/mileage` | Update vehicle mileage |
| `DELETE` | `/api/v1/vehicles/{id}` | Delete a vehicle and its location history |

### Locations

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/vehicles/{id}/locations` | Record a location |
| `GET` | `/api/v1/vehicles/{id}/locations/latest` | Get the latest location |
| `GET` | `/api/v1/vehicles/{id}/locations` | Get location history (paged, optional `from`/`to`) |
| `GET` | `/api/v1/vehicles/live` | Get the latest location of every vehicle |

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

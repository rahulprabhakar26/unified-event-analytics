# Unified Event Analytics Engine

A scalable backend API for web and mobile analytics that provides comprehensive event tracking, user analytics, and real-time reporting capabilities.

> ** Database Update**: Now using **MySQL 8.0** for wider compatibility and easier deployment. See [CHANGELOG.md](CHANGELOG.md) for details.

##  Features

- **API Key Management**
  - User registration via Google OAuth2
  - API key generation, revocation, and regeneration
  - API key expiration handling
  
- **Event Data Collection**
  - High-volume event ingestion
  - Support for clicks, visits, referrer data, device metrics
  - Custom metadata tracking
  - IP-based geolocation (ready for integration)

- **Analytics & Reporting**
  - Event-based aggregation with filtering
  - User-based statistics
  - Device and browser analytics
  - Time-range filtering
  - Cross-app analytics

- **Performance & Scalability**
  - Redis caching for frequently accessed data
  - Connection pooling for database efficiency
  - Batch processing for event insertion
  - Rate limiting to prevent abuse

- **Security**
  - Google OAuth2 authentication
  - JWT-based authorization
  - API key authentication for event collection
  - Rate limiting per API key/user/IP

##  Architecture

### Technology Stack
- **Backend Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MySQL 8.0
- **Cache**: Redis 7
- **Authentication**: Google OAuth2 + JWT
- **API Documentation**: OpenAPI 3.0 (Swagger)
- **Containerization**: Docker & Docker Compose

### Database Schema
- **Users**: OAuth2 authenticated users
- **Apps**: Registered applications/websites
- **ApiKeys**: Generated API keys for apps
- **AnalyticsEvents**: Event data with metadata

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0
- Redis 7
- Docker & Docker Compose (for containerized deployment)
- Google Cloud Console account (for OAuth2)

##  Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/unified-analytics-engine.git
cd unified-analytics-engine
```

### 2. Configure Google OAuth2

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable Google+ API
4. Create OAuth 2.0 credentials:
   - Application type: Web application
   - Authorized redirect URIs: 
     - `http://localhost:8080/login/oauth2/code/google`
     - `https://your-domain.com/login/oauth2/code/google` (for production)
5. Copy the Client ID and Client Secret

### 3. Environment Configuration

Create a `.env` file in the root directory:

```env
# Database Configuration
DATABASE_URL=jdbc:mysql://localhost:3306/analytics_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
DATABASE_USERNAME=root
DATABASE_PASSWORD=your_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Google OAuth Configuration
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# JWT Configuration
JWT_SECRET=your-secure-jwt-secret-key
```

### 4. Database Setup

```bash
# Create MySQL database
mysql -u root -p
CREATE DATABASE analytics_db;
exit;

# Or let the application create it automatically (configured in connection string)
```

### 5. Run with Docker Compose (Recommended)

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

### 6. Run Locally (Without Docker)

```bash
# Install dependencies and build
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/unified-analytics-engine-1.0.0.jar
```

##  API Documentation

Once the application is running, access the interactive API documentation at:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs

##  API Endpoints

### Authentication & API Key Management

#### 1. Google OAuth Login
```
GET /oauth2/authorization/google
```
Initiates Google OAuth2 login flow. After successful authentication, receive JWT token.

#### 2. Register App
```
POST /api/auth/register
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "name": "My Website",
  "domain": "https://example.com",
  "description": "My analytics app",
  "apiKeyName": "Production Key"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "appId": "app_1234567890abcdef",
    "name": "My Website",
    "domain": "https://example.com",
    "apiKey": "generated-api-key-here",
    "apiKeyExpiresAt": "2025-11-19T12:00:00",
    "createdAt": "2024-11-19T12:00:00",
    "message": "App registered successfully"
  }
}
```

#### 3. Get API Keys
```
GET /api/auth/api-keys?appId=app_1234567890abcdef
Authorization: Bearer <jwt_token>
```

#### 4. Revoke API Key
```
POST /api/auth/revoke
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "apiKey": "api-key-to-revoke"
}
```

#### 5. Regenerate API Key
```
POST /api/auth/regenerate
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "apiKey": "old-api-key",
  "validityDays": 365
}
```

### Event Collection

#### Collect Analytics Event
```
POST /api/analytics/collect
X-API-Key: your-api-key
Content-Type: application/json

{
  "event": "login_form_cta_click",
  "url": "https://example.com/page",
  "referrer": "https://google.com",
  "device": "mobile",
  "ipAddress": "192.168.1.1",
  "userId": "user123",
  "timestamp": "2024-11-19T12:34:56Z",
  "metadata": {
    "browser": "Chrome",
    "os": "Android",
    "screenSize": "1080x1920"
  },
  "userAgent": "Mozilla/5.0..."
}
```

### Analytics & Reporting

#### 1. Event Summary
```
GET /api/analytics/event-summary?event=login_form_cta_click&startDate=2024-11-15&endDate=2024-11-20&appId=app_123
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "event": "login_form_cta_click",
    "count": 3400,
    "uniqueUsers": 1200,
    "deviceData": {
      "mobile": 2200,
      "desktop": 1200
    }
  }
}
```

#### 2. User Statistics
```
GET /api/analytics/user-stats?userId=user123
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "totalEvents": 150,
    "deviceDetails": {
      "browser": "Chrome",
      "os": "Android"
    },
    "ipAddress": "192.168.1.1"
  }
}
```

## 🧪 Testing

Run the comprehensive test suite:

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=AnalyticsServiceTest
```

## 🚢 Deployment

### Deploy to Railway

1. Create account at [Railway.app](https://railway.app)
2. Install Railway CLI:
```bash
npm i -g @railway/cli
```

3. Login and deploy:
```bash
railway login
railway init
railway up
```

4. Add environment variables in Railway dashboard

### Deploy to Render

1. Create account at [Render.com](https://render.com)
2. Create new Web Service
3. Connect your GitHub repository
4. Configure:
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -jar target/unified-analytics-engine-1.0.0.jar`
5. Add environment variables
6. Create PostgreSQL and Redis instances

### Deploy to AWS ECS

1. Build and push Docker image:
```bash
docker build -t analytics-engine .
docker tag analytics-engine:latest <account-id>.dkr.ecr.<region>.amazonaws.com/analytics-engine:latest
docker push <account-id>.dkr.ecr.<region>.amazonaws.com/analytics-engine:latest
```

2. Create ECS cluster, task definition, and service
3. Configure RDS PostgreSQL and ElastiCache Redis
4. Set up Application Load Balancer
5. Configure environment variables

## 📊 Performance Optimization

- **Database Indexing**: Critical fields indexed for fast queries
- **Redis Caching**: Event summaries cached for 1 hour
- **Connection Pooling**: HikariCP with optimized pool size
- **Batch Processing**: Events inserted in batches of 50
- **Rate Limiting**: 1000 requests/minute for API keys

## 🔐 Security Features

- HTTPS recommended for production
- JWT tokens with 24-hour expiration
- API keys with configurable expiration
- Rate limiting to prevent abuse
- CORS configuration
- SQL injection prevention via JPA
- Input validation on all endpoints

## 🐛 Troubleshooting

### Database Connection Issues
```bash
# Check PostgreSQL is running
pg_isready -h localhost -p 5432

# Check connection
psql -U postgres -h localhost -d analytics_db
```

### Redis Connection Issues
```bash
# Check Redis is running
redis-cli ping

# Should return: PONG
```

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

## 📈 Monitoring & Logging

Application logs are configured with different levels:
- **INFO**: General application flow
- **DEBUG**: Detailed SQL queries (in development)
- **ERROR**: Error conditions

Access health endpoint:
```
GET /actuator/health
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 Challenges Faced & Solutions

### Challenge 1: High-Volume Event Ingestion
**Solution**: Implemented batch processing, connection pooling, and asynchronous processing for API key usage tracking.

### Challenge 2: Complex Aggregation Queries
**Solution**: Created indexed database queries with optimized JOIN operations and Redis caching for frequently accessed data.

### Challenge 3: Rate Limiting Across Distributed Systems
**Solution**: Implemented Bucket4j with in-memory storage (can be upgraded to Redis for distributed rate limiting).

### Challenge 4: OAuth2 Integration Complexity
**Solution**: Used Spring Security OAuth2 client with custom success handler for seamless JWT generation.

## 🔮 Future Enhancements

- [ ] Geolocation API integration for IP-based location tracking
- [ ] Real-time WebSocket updates for live analytics
- [ ] Data export functionality (CSV, JSON)
- [ ] Advanced filtering and custom date ranges
- [ ] Email notifications for anomalies
- [ ] Dashboard UI with charts and graphs
- [ ] Distributed rate limiting with Redis
- [ ] Elasticsearch integration for advanced search




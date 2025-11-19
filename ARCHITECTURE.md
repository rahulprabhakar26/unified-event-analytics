# System Architecture

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
├──────────────┬──────────────┬──────────────┬────────────────────┤
│   Web Apps   │  Mobile Apps │  React Apps  │  Native Apps       │
└──────┬───────┴──────┬───────┴──────┬───────┴────────┬───────────┘
       │              │              │                │
       │ HTTP/HTTPS   │ HTTP/HTTPS   │ HTTP/HTTPS    │ HTTP/HTTPS
       │              │              │                │
       └──────────────┴──────────────┴────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Load Balancer (Future)                      │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway / NGINX (Future)                  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                       │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                  Security Layer                          │   │
│  │  ┌────────────────┐  ┌──────────────────┐             │   │
│  │  │ OAuth2 Filter  │  │ JWT Filter       │             │   │
│  │  └────────┬───────┘  └────────┬─────────┘             │   │
│  │           │                    │                        │   │
│  │  ┌────────▼─────────────────────▼──────────┐          │   │
│  │  │     API Key Authentication Filter        │          │   │
│  │  └──────────────────┬───────────────────────┘          │   │
│  └─────────────────────┼──────────────────────────────────┘   │
│                        │                                        │
│  ┌─────────────────────▼──────────────────────────────────┐   │
│  │              Rate Limiting Interceptor                  │   │
│  │              (Bucket4j - 100-1000 req/min)             │   │
│  └─────────────────────┬──────────────────────────────────┘   │
│                        │                                        │
│  ┌─────────────────────▼──────────────────────────────────┐   │
│  │                 Controller Layer                        │   │
│  │  ┌──────────────────┐  ┌───────────────────────┐      │   │
│  │  │ AuthController   │  │ AnalyticsController   │      │   │
│  │  └────────┬─────────┘  └──────────┬────────────┘      │   │
│  └───────────┼────────────────────────┼───────────────────┘   │
│              │                        │                        │
│  ┌───────────▼────────────────────────▼───────────────────┐   │
│  │                  Service Layer                          │   │
│  │  ┌────────────┐  ┌────────────┐  ┌──────────────┐    │   │
│  │  │ AppService │  │ ApiKeySvc  │  │ AnalyticsSvc │    │   │
│  │  └─────┬──────┘  └─────┬──────┘  └──────┬───────┘    │   │
│  └────────┼───────────────┼────────────────┼─────────────┘   │
│           │               │                │                  │
│  ┌────────▼───────────────▼────────────────▼─────────────┐   │
│  │              Repository Layer (JPA)                    │   │
│  │  ┌──────────┐  ┌──────────┐  ┌────────────────┐     │   │
│  │  │ UserRepo │  │ AppRepo  │  │ EventRepo      │     │   │
│  │  └──────────┘  └──────────┘  └────────────────┘     │   │
│  └────────────────────┬───────────────────────────────────┘   │
└───────────────────────┼───────────────────────────────────────┘
                        │
            ┌───────────┴───────────┐
            │                       │
            ▼                       ▼
┌──────────────────────┐  ┌──────────────────┐
│   MySQL Database     │  │   Redis Cache    │
│                      │  │                  │
│  ┌───────────────┐  │  │ ┌──────────────┐ │
│  │ Users         │  │  │ │ Event Cache  │ │
│  │ Apps          │  │  │ │ User Cache   │ │
│  │ API Keys      │  │  │ │ Rate Limits  │ │
│  │ Events        │  │  │ └──────────────┘ │
│  └───────────────┘  │  │                  │
└──────────────────────┘  └──────────────────┘
```

## Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    External Services                         │
│  ┌──────────────────┐         ┌────────────────────┐       │
│  │ Google OAuth2    │         │ Geolocation API    │       │
│  │ (Authentication) │         │ (Future)           │       │
│  └────────┬─────────┘         └────────────────────┘       │
└───────────┼──────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│                  Security Components                         │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ OAuth2LoginSuccessHandler                            │  │
│  │ - Processes Google OAuth callback                    │  │
│  │ - Creates or updates user                            │  │
│  │ - Generates JWT token                                │  │
│  └──────────────────┬───────────────────────────────────┘  │
│                     │                                       │
│  ┌──────────────────▼───────────────────────────────────┐  │
│  │ JwtService                                           │  │
│  │ - Token generation                                   │  │
│  │ - Token validation                                   │  │
│  │ - Claims extraction                                  │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ CustomUserDetailsService                             │  │
│  │ - Load user by email                                 │  │
│  │ - User authentication                                │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   Business Logic Layer                       │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ AppService                                           │  │
│  │ - Register new applications                          │  │
│  │ - Generate app IDs                                   │  │
│  │ - Manage app lifecycle                               │  │
│  └──────────────────┬───────────────────────────────────┘  │
│                     │                                       │
│  ┌──────────────────▼───────────────────────────────────┐  │
│  │ ApiKeyService                                        │  │
│  │ - Generate secure API keys                           │  │
│  │ - Revoke/regenerate keys                            │  │
│  │ - Track usage statistics                             │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ AnalyticsService                                     │  │
│  │ - Collect events                                     │  │
│  │ - Aggregate analytics                                │  │
│  │ - Generate reports                                   │  │
│  │ - Cache management                                   │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     Data Access Layer                        │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐  │
│  │ UserRepo     │  │ AppRepo      │  │ ApiKeyRepo      │  │
│  │              │  │              │  │                 │  │
│  │ - findByEmail│  │ - findByAppId│  │ - findByKey     │  │
│  │ - findByGoogle│ │ - findByUser │  │ - increment     │  │
│  └──────────────┘  └──────────────┘  └─────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ AnalyticsEventRepository                             │  │
│  │ - countByAppAndEvent                                 │  │
│  │ - countDistinctUsers                                 │  │
│  │ - countByDeviceGrouped                               │  │
│  │ - findByUserId                                       │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

## Request Flow Diagrams

### 1. User Registration Flow

```
Client                   API                    Google           Database
  │                       │                        │                │
  │─── GET /oauth2/authorization/google ─────────→│                │
  │                       │                        │                │
  │                       │←─── Redirect to Google │                │
  │←──────────────────────│                        │                │
  │                       │                        │                │
  │─── Login with Google ─────────────────────────→│                │
  │                       │                        │                │
  │                       │←─── Auth Code ─────────│                │
  │←──────────────────────│                        │                │
  │                       │                        │                │
  │─── GET /callback ────→│                        │                │
  │                       │                        │                │
  │                       │─── Exchange Code ─────→│                │
  │                       │                        │                │
  │                       │←─── User Info ─────────│                │
  │                       │                        │                │
  │                       │─── Save/Update User ──────────────────→│
  │                       │                        │                │
  │                       │←─── User Saved ────────────────────────│
  │                       │                        │                │
  │                       │─── Generate JWT Token  │                │
  │                       │                        │                │
  │←─── JWT Token ────────│                        │                │
  │                       │                        │                │
```

### 2. App Registration Flow

```
Client                  API                  Services            Database
  │                      │                      │                   │
  │─── POST /api/auth/register (JWT) ──────────→│                   │
  │                      │                      │                   │
  │                      │─── Validate JWT ────→│                   │
  │                      │                      │                   │
  │                      │←─── User Info ───────│                   │
  │                      │                      │                   │
  │                      │─── Create App ───────────────────────────→│
  │                      │                      │                   │
  │                      │←─── App Created ─────────────────────────│
  │                      │                      │                   │
  │                      │─── Generate API Key ────────────────────→│
  │                      │                      │                   │
  │                      │←─── API Key Saved ───────────────────────│
  │                      │                      │                   │
  │←─── App + API Key ───│                      │                   │
  │                      │                      │                   │
```

### 3. Event Collection Flow

```
Client              API              Rate Limiter      Cache      Database
  │                  │                    │             │            │
  │─── POST /collect (API Key) ─────────→│             │            │
  │                  │                    │             │            │
  │                  │─── Check Rate ────→│             │            │
  │                  │                    │             │            │
  │                  │←─── OK ────────────│             │            │
  │                  │                    │             │            │
  │                  │─── Validate API Key ────────────────────────→│
  │                  │                    │             │            │
  │                  │←─── Valid ──────────────────────────────────│
  │                  │                    │             │            │
  │                  │─── Save Event ──────────────────────────────→│
  │                  │                    │             │            │
  │                  │←─── Event Saved ────────────────────────────│
  │                  │                    │             │            │
  │                  │─── Update API Key Stats ────────────────────→│
  │                  │                    │             │            │
  │←─── Success ─────│                    │             │            │
  │                  │                    │             │            │
```

### 4. Analytics Query Flow

```
Client              API              Cache            Database
  │                  │                 │                 │
  │─── GET /event-summary (JWT) ─────→│                 │
  │                  │                 │                 │
  │                  │─── Check Cache ────────────────→│ │
  │                  │                 │                 │
  │                  │←─── Cache Miss ─────────────────│ │
  │                  │                 │                 │
  │                  │─── Query Events ────────────────────────────→│
  │                  │                 │                 │
  │                  │←─── Event Data ─────────────────────────────│
  │                  │                 │                 │
  │                  │─── Aggregate Data                │
  │                  │                 │                 │
  │                  │─── Store in Cache ──────────────→│
  │                  │                 │                 │
  │←─── Summary ─────│                 │                 │
  │                  │                 │                 │
```

## Data Flow

### Event Data Pipeline

```
┌──────────────────────────────────────────────────────────────┐
│                      Event Sources                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────────┐ │
│  │ Website  │  │ Mobile   │  │ Desktop  │  │ IoT Device  │ │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └──────┬──────┘ │
└───────┼─────────────┼─────────────┼────────────────┼─────────┘
        │             │             │                │
        └─────────────┴─────────────┴────────────────┘
                              │
                              ▼
                    ┌───────────────────┐
                    │  API Validation   │
                    │  - Required fields│
                    │  - Data types     │
                    │  - API key        │
                    └─────────┬─────────┘
                              │
                              ▼
                    ┌───────────────────┐
                    │  Rate Limiting    │
                    │  - Check limits   │
                    │  - Update tokens  │
                    └─────────┬─────────┘
                              │
                              ▼
                    ┌───────────────────┐
                    │  Data Enrichment  │
                    │  - Parse UserAgent│
                    │  - Geo location   │
                    │  - Device info    │
                    └─────────┬─────────┘
                              │
                              ▼
                    ┌───────────────────┐
                    │  Database Storage │
                    │  - Batch insert   │
                    │  - Indexed write  │
                    └─────────┬─────────┘
                              │
                              ▼
                    ┌───────────────────┐
                    │  Cache Invalidate │
                    │  - Clear old cache│
                    └───────────────────┘
```

## Deployment Architecture

### Single Instance (Development)

```
┌────────────────────────────────────┐
│        Docker Host                 │
│                                    │
│  ┌──────────────────────────────┐ │
│  │  Analytics App Container     │ │
│  │  - Spring Boot               │ │
│  │  - Port 8080                 │ │
│  └──────────┬───────────────────┘ │
│             │                      │
│  ┌──────────▼───────────────────┐ │
│  │  MySQL Container             │ │
│  │  - Port 3306                 │ │
│  │  - Persistent Volume         │ │
│  └──────────────────────────────┘ │
│                                    │
│  ┌──────────────────────────────┐ │
│  │  Redis Container             │ │
│  │  - Port 6379                 │ │
│  │  - Persistent Volume         │ │
│  └──────────────────────────────┘ │
└────────────────────────────────────┘
```

### Production (Scalable)

```
┌────────────────────────────────────────────────────────────────┐
│                        Internet                                 │
└────────────────────────────┬───────────────────────────────────┘
                             │
                             ▼
                   ┌──────────────────┐
                   │  Load Balancer   │
                   │  (ALB/NGINX)     │
                   └─────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
              ▼              ▼              ▼
        ┌─────────┐    ┌─────────┐    ┌─────────┐
        │ App     │    │ App     │    │ App     │
        │ Instance│    │ Instance│    │ Instance│
        │ 1       │    │ 2       │    │ 3       │
        └────┬────┘    └────┬────┘    └────┬────┘
             │              │              │
             └──────────────┼──────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
  ┌───────────┐    ┌─────────────┐    ┌──────────┐
  │ MySQL     │    │   Redis     │    │ Metrics  │
  │ (RDS)     │    │ (ElastiCache│    │ Service  │
  │ - Primary │    │  Cluster)   │    │          │
  │ - Replica │    └─────────────┘    └──────────┘
  └───────────┘
```

## Security Architecture

```
┌────────────────────────────────────────────────────────────┐
│                     Security Layers                         │
│                                                             │
│  Layer 1: Network Security                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ - HTTPS/TLS                                          │ │
│  │ - CORS Policy                                        │ │
│  │ - Firewall Rules                                     │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                             │
│  Layer 2: Authentication                                   │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ - OAuth2 (Google)                                    │ │
│  │ - JWT Tokens                                         │ │
│  │ - API Keys                                           │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                             │
│  Layer 3: Authorization                                    │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ - Role-Based Access Control                          │ │
│  │ - Resource Ownership Validation                      │ │
│  │ - API Key Scope                                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                             │
│  Layer 4: Rate Limiting                                    │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ - Per-User Limits                                    │ │
│  │ - Per-IP Limits                                      │ │
│  │ - Per-API-Key Limits                                 │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                             │
│  Layer 5: Data Protection                                  │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ - Input Validation                                   │ │
│  │ - SQL Injection Prevention (JPA)                     │ │
│  │ - XSS Protection                                     │ │
│  │ - Encrypted Connections                              │ │
│  └──────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Technology Stack Details

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Stack                         │
├─────────────────────────────────────────────────────────────┤
│ Backend Framework      │ Spring Boot 3.2.0                  │
│ Language               │ Java 17                            │
│ Build Tool             │ Maven 3.9+                         │
├─────────────────────────────────────────────────────────────┤
│ Database               │ MySQL 8.0                          │
│ ORM                    │ Hibernate (JPA)                    │
│ Connection Pool        │ HikariCP                           │
├─────────────────────────────────────────────────────────────┤
│ Cache                  │ Redis 7                            │
│ Cache Client           │ Lettuce                            │
├─────────────────────────────────────────────────────────────┤
│ Authentication         │ Spring Security OAuth2             │
│ Token                  │ JWT (jjwt 0.12.3)                  │
├─────────────────────────────────────────────────────────────┤
│ Rate Limiting          │ Bucket4j                           │
│ API Documentation      │ SpringDoc OpenAPI 3.0              │
├─────────────────────────────────────────────────────────────┤
│ Testing                │ JUnit 5, Mockito                   │
│ Containerization       │ Docker, Docker Compose             │
└─────────────────────────────────────────────────────────────┘
```

## Performance Characteristics

### Expected Throughput

- **Event Collection**: 1000+ requests/second per instance
- **Analytics Queries**: 200+ requests/second (with caching)
- **Database Writes**: 10,000+ events/second (batch inserts)

### Response Times (p95)

- **Event Collection**: < 50ms
- **Cached Analytics**: < 10ms
- **Uncached Analytics**: < 200ms
- **Authentication**: < 100ms

### Scalability

- **Horizontal Scaling**: Stateless design allows infinite scaling
- **Database Scaling**: Read replicas for analytics queries
- **Cache Scaling**: Redis cluster for distributed caching
- **Rate Limiting**: Can be moved to Redis for distributed limits

---

## Monitoring Points

### Application Metrics
- Request count by endpoint
- Response times
- Error rates
- Cache hit/miss ratio

### System Metrics
- CPU usage
- Memory usage
- Database connections
- Cache connections

### Business Metrics
- Events collected per minute
- Unique users tracked
- API key usage
- Active applications

---


# Quick Setup Guide for Postman Testing

## 🚀 Quick Start

### 1. Import Collection and Environment

1. **Open Postman**
2. **Import Collection:**

   - Click "Import" button
   - Select `Projo_Backend_API_Collection.postman_collection.json`
   - Click "Import"

3. **Import Environment:**

   - Click "Import" button
   - Select `Projo_Backend_Environment.postman_environment.json`
   - Click "Import"

4. **Select Environment:**
   - Click the environment dropdown (top-right)
   - Select "Projo Backend Development"

### 2. Start the Application

```bash
cd /e/work/AllProjects/projo-backend
./mvnw spring-boot:run
```

Wait for the application to start (you should see "Started ProjoBackendApplication" in the logs).

### 3. Test Redis Connection

1. Run the "Check Redis Health" request from the "Cache Management" folder
2. You should get a response with `"status": "UP"` and `"redis": "Connected"`

### 4. Authentication Flow

1. **Register a new user:**

   - Run "Register User" from "Authentication" folder
   - This creates a test user with username "testuser"

2. **Login:**

   - Run "Login User" from "Authentication" folder
   - This automatically sets the `jwt_token` environment variable

3. **Test authentication:**
   - Run "Get User Profile" to verify the token works

### 5. Test Basic CRUD Operations

1. **Create a project:**

   - Run "Create Project" from "Projects" folder
   - This automatically sets the `project_id` environment variable

2. **Create a task:**

   - Run "Create Task" from "Tasks" folder
   - This automatically sets the `task_id` environment variable

3. **Test caching:**
   - Run "Get All Projects (Cached)" twice
   - Compare response times (second should be faster)

### 6. Test Cache Performance

Run the requests in the "Cache Performance Tests" folder in order:

1. "Clear Caches (Setup)"
2. "First Request (Cache Miss)"
3. "Second Request (Cache Hit)"

The test scripts will automatically compare performance and validate that caching is working.

## 📊 Cache Testing Scenarios

### Scenario 1: Project Caching

1. Clear all caches
2. Get project by ID (cache miss - slower)
3. Get same project by ID (cache hit - faster)
4. Update the project (cache eviction)
5. Get project by ID again (cache miss - slower again)

### Scenario 2: Dashboard Caching

1. Clear all caches
2. Get dashboard stats (cache miss)
3. Get dashboard stats again (cache hit)
4. Create a new project (triggers cache eviction)
5. Get dashboard stats (cache miss due to eviction)

### Scenario 3: Task Caching

1. Get tasks for Kanban board (cache miss)
2. Get same tasks (cache hit)
3. Create new task (cache eviction)
4. Get tasks again (cache miss)

## 🔧 Customization

### Environment Variables

You can modify these in the environment:

- `base_url`: Change to your server URL (default: http://localhost:8080)
- `project_id`, `task_id`: Set these manually for testing specific resources

### Test Data

Modify the request bodies in the collection to test with your own data:

- Project titles, descriptions, deadlines
- Task titles, priorities, status
- User credentials

## 🐛 Troubleshooting

### Common Issues

1. **401 Unauthorized errors:**

   - Make sure you've run the login request first
   - Check that `jwt_token` is set in the environment

2. **Connection refused:**

   - Ensure the Spring Boot application is running
   - Check that it's running on port 8080

3. **Redis connection issues:**

   - Check the Redis health endpoint
   - Verify Redis credentials in application.properties

4. **404 Not Found for resources:**
   - Make sure you've created the resources first (projects, tasks)
   - Check that the IDs are set correctly in environment variables

### Performance Testing Tips

1. **For accurate cache performance testing:**

   - Clear caches before each test run
   - Run requests multiple times to get average response times
   - Test with larger datasets for more noticeable differences

2. **Monitor cache statistics:**
   - Use the "Get Cache Statistics" endpoint
   - Check hit/miss ratios
   - Monitor cache sizes

## 📈 Expected Results

### Response Times (approximate)

- **Cache Miss:** 100-500ms (database query)
- **Cache Hit:** 10-50ms (Redis retrieval)
- **Cache Clear:** 20-100ms

### Cache Hit Ratios

After running several requests, you should see:

- Hit ratios > 70% for frequently accessed data
- Lower hit ratios initially, improving over time

### Redis Health Check

```json
{
  "status": "UP",
  "redis": "Connected",
  "ping": "PONG",
  "cacheManager": "Available"
}
```

## 🎯 Advanced Testing

### Load Testing

1. Use Postman's Collection Runner
2. Run the collection multiple times
3. Monitor cache performance under load

### Cache Invalidation Testing

1. Create data through one endpoint
2. Verify cache is evicted
3. Check that subsequent reads are cache misses

### Error Handling

1. Test with invalid authentication
2. Test with non-existent resources
3. Verify proper error responses

---

This setup provides comprehensive testing of the Projo Backend API with Redis caching functionality. The automated test scripts help validate both functional correctness and caching performance.

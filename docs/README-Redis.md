# Redis Stack Cluster Setup for Revquix Backend

This Docker Compose setup provides a production-ready Redis Stack cluster with high availability using Redis Sentinel and RedisInsight as the web-based GUI for management and monitoring.

## Architecture

- **1 Redis Master**: Primary Redis Stack instance (port 6379)
- **2 Redis Replicas**: Read replicas (ports 6380, 6381)
- **3 Redis Sentinels**: For automatic failover (ports 26379, 26380, 26381)
- **1 RedisInsight**: Web-based Redis GUI (port 8001)

## Quick Start

1. Start the Redis cluster:
   ```bash
   docker-compose -f docker/docker-compose.redis.yml up -d
   ```

2. Check the status:
   ```bash
   docker-compose -f docker/docker-compose.redis.yml ps
   ```

3. View logs:
   ```bash
   docker-compose -f docker/docker-compose.redis.yml logs -f
   ```

## RedisInsight Web GUI - Complete Guide

RedisInsight is a powerful, intuitive Redis GUI that provides real-time monitoring, data visualization, and management capabilities.

### Accessing RedisInsight

1. **Open RedisInsight**: Navigate to `http://localhost:8001` in your web browser
2. **First-time Setup**: RedisInsight will ask you to accept terms and create a database connection

### Adding Redis Database Connection

#### Method 1: Add Database Manually
1. Click **"Add Redis Database"** or **"+"** button
2. Select **"Connect to a Redis database"**
3. Fill in the connection details:
   ```
   Host: localhost (or redis-master if connecting from within Docker network)
   Port: 6379
   Name: Revquix Redis Master
   Username: default
   Password: redis123
   ```
4. Click **"Add Redis Database"**

#### Method 2: Auto-discover (if using Docker network)
1. Click **"Add Redis Database"**
2. Select **"Auto-discover Redis databases"**
3. RedisInsight may automatically detect your Redis instances

### RedisInsight Features & How to Use Them

#### 1. **Browser Tab** - Data Exploration
- **View all keys**: Browse through your Redis keys with pagination
- **Search keys**: Use pattern matching (e.g., `user:*`, `session:*`)
- **Key types**: View different data types (Strings, Lists, Sets, Hashes, etc.)
- **Add/Edit/Delete**: Manage your data directly from the interface
- **Key details**: View TTL, memory usage, and key information

**How to use:**
- Click on **"Browser"** tab
- Use the search bar to filter keys
- Click on any key to view/edit its value
- Right-click for context menu options (delete, rename, etc.)

#### 2. **Workbench Tab** - Command Execution
- **Redis CLI in browser**: Execute Redis commands directly
- **Command suggestions**: Auto-complete and syntax highlighting
- **Command history**: Access previously executed commands
- **Bulk operations**: Execute multiple commands at once

**How to use:**
```redis
# Examples of commands you can run:
INFO replication          # Check replication status
KEYS *                   # List all keys (use carefully in production)
GET user:123             # Get a specific key
SET user:456 "John Doe"  # Set a key-value pair
HGETALL user:profile:123 # Get all fields of a hash
```

#### 3. **Analysis Tab** - Performance Insights
- **Memory analysis**: See which keys consume the most memory
- **Key patterns**: Analyze key naming patterns
- **Data type distribution**: Understand your data structure
- **Recommendations**: Get suggestions for optimization

**How to use:**
- Click **"Analysis"** tab
- Click **"New Report"** to generate analysis
- Review memory usage by key patterns
- Use recommendations to optimize your Redis usage

#### 4. **Slow Log Tab** - Performance Monitoring
- **Slow queries**: View commands that took longer than expected
- **Execution time**: See how long each command took
- **Command frequency**: Identify performance bottlenecks

#### 5. **Pub/Sub Tab** - Real-time Messaging
- **Subscribe to channels**: Monitor real-time messages
- **Publish messages**: Send test messages to channels
- **Pattern subscriptions**: Subscribe to channel patterns

**How to use:**
```redis
# Subscribe to channels
SUBSCRIBE user:notifications
PSUBSCRIBE user:*

# Publish messages (from Workbench)
PUBLISH user:notifications "New message"
```

#### 6. **Streams Tab** - Stream Data Management
- **View stream entries**: Browse through Redis Streams data
- **Consumer groups**: Monitor consumer group status
- **Add entries**: Insert new stream entries

### Redis Cluster Monitoring with RedisInsight

#### Checking Replication Status
1. Go to **Workbench** tab
2. Run: `INFO replication`
3. You should see:
   ```
   role:master
   connected_slaves:2
   slave0:ip=172.20.0.3,port=6379,state=online
   slave1:ip=172.20.0.4,port=6379,state=online
   ```

#### Monitoring Sentinel Status
Connect to Sentinel (port 26379) and run:
```redis
SENTINEL masters
SENTINEL slaves mymaster
SENTINEL sentinels mymaster
```

#### Memory and Performance Monitoring
```redis
INFO memory          # Memory usage statistics
INFO stats           # General statistics
INFO clients         # Connected clients info
MONITOR             # Real-time command monitoring (use carefully)
```

### Advanced RedisInsight Features

#### 1. **Database Comparison**
- Compare data between different Redis instances
- Useful for verifying replication consistency

#### 2. **Bulk Operations**
- Import/Export data
- Bulk delete operations
- Data migration tools

#### 3. **Plugin System**
- Redis Stack modules visualization
- Custom plugin support

#### 4. **Profiler**
- Real-time command profiling
- Performance bottleneck identification

### Security Features in RedisInsight

- **Authentication**: Supports Redis AUTH and ACL
- **TLS/SSL**: Secure connections support
- **Read-only mode**: Safe production monitoring
- **Role-based access**: Different permission levels

### Tips for Production Use

1. **Use Read-Only Mode**: For production monitoring, create read-only Redis users
2. **Limit Key Scanning**: Avoid `KEYS *` command on large datasets
3. **Monitor Memory**: Use Analysis tab to track memory usage trends
4. **Set up Alerts**: Monitor slow log and memory usage
5. **Regular Backups**: Use RedisInsight to verify backup integrity

### Troubleshooting RedisInsight

#### Connection Issues
```bash
# Check if Redis is running
docker ps | grep redis

# Check Redis logs
docker logs revquix-redis-master

# Test Redis connection
docker exec -it revquix-redis-master redis-cli -a redis123 ping
```

#### Performance Issues
- Clear browser cache and cookies
- Restart RedisInsight container
- Check Docker resource limits

## Environment Variables for Your Spring Boot Application

Your application is already configured to use these Redis settings from `application.yml`:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:redis123}
      username: ${REDIS_USERNAME:default}
```

Set these environment variables when running your Spring Boot application:
- `REDIS_HOST=localhost`
- `REDIS_PORT=6379`
- `REDIS_PASSWORD=redis123`
- `REDIS_USERNAME=default`

## High Availability Features

- **Automatic Failover**: If the master fails, Sentinel will promote a replica to master
- **Data Persistence**: Both RDB snapshots and AOF logging are enabled
- **Authentication**: Redis instances are secured with password authentication
- **Health Checks**: Docker health checks ensure services are running properly

## Additional Monitoring Options

### 1. Redis CLI Monitoring
```bash
# Connect to master
docker exec -it revquix-redis-master redis-cli -a redis123

# Connect to replica
docker exec -it revquix-redis-replica-1 redis-cli -a redis123

# Connect to sentinel
docker exec -it revquix-redis-sentinel-1 redis-cli -p 26379 -a redis123
```

### 2. Sentinel Commands
```bash
# Check master status
docker exec -it revquix-redis-sentinel-1 redis-cli -p 26379 -a redis123 sentinel masters

# Check replica status
docker exec -it revquix-redis-sentinel-1 redis-cli -p 26379 -a redis123 sentinel slaves mymaster

# Force failover (for testing)
docker exec -it revquix-redis-sentinel-1 redis-cli -p 26379 -a redis123 sentinel failover mymaster
```

## Stopping the Cluster

```bash
# Stop all services
docker-compose -f docker/docker-compose.redis.yml down

# Stop and remove volumes (data will be lost)
docker-compose -f docker/docker-compose.redis.yml down -v

# Stop specific service
docker-compose -f docker/docker-compose.redis.yml stop redisinsight
```

## Production Notes

- **Data Persistence**: Data is persisted in Docker volumes
- **Memory Optimization**: Redis instances are configured with optimized settings
- **Security**: Sensitive commands like FLUSHDB, FLUSHALL are disabled
- **Network Isolation**: Uses custom Docker network for security
- **Automatic Restart**: All services have restart policies configured
- **Redis Stack**: Includes additional modules like RedisJSON, RedisSearch, etc.

## Redis Stack Additional Modules

Your Redis Stack includes these additional capabilities:
- **RedisJSON**: JSON data type support
- **RedisSearch**: Full-text search and indexing
- **RedisTimeSeries**: Time-series data support
- **RedisGraph**: Graph database capabilities
- **RedisBloom**: Probabilistic data structures

Access these features through RedisInsight or direct Redis commands.

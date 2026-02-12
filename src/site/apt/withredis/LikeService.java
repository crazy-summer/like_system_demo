package site.apt.withredis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class LikeService {
    // 静态连接池，全局唯一
    private static final JedisPool jedisPool;

    // 静态代码块初始化连接池（推荐方式）
    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 连接池基础配置，避免高并发下连接不足
        poolConfig.setMaxTotal(20); // 最大连接数
        poolConfig.setMaxIdle(10);  // 最大空闲连接
        poolConfig.setMinIdle(5);   // 最小空闲连接
        poolConfig.setTestOnBorrow(true); // 获取连接时测试可用性

        // 初始化连接池（替换为你的 Redis 地址/密码）
        jedisPool = new JedisPool(poolConfig, "192.168.1.100", 6379);
    }

    /**
     * 点赞操作（使用连接池，原子操作 incr）
     */
    public void like(String contentId) {
        // try-with-resources 自动关闭 jedis 连接，归还到连接池
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "like_count:" + contentId;
            jedis.incr(key); // Redis 的 incr 是原子操作，天然支持高并发
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消点赞操作（原子操作，避免竞态条件）
     */
    public void unlike(String contentId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "like_count:" + contentId;
            // 使用 Lua 脚本实现 "先判断再减1" 的原子操作，避免非原子的 get+decr
            String luaScript = "local count = tonumber(redis.call('get', KEYS[1]) or 0) " +
                    "if count > 0 then " +
                    "    return redis.call('decr', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";
            jedis.eval(luaScript, 1, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取点赞数
     */
    public int getLikeCount(String contentId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "like_count:" + contentId;
            String count = jedis.get(key);
            return count == null ? 0 : Integer.parseInt(count);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 关闭连接池（程序退出时调用）
     */
    public void close() {
        if (!jedisPool.isClosed()) {
            jedisPool.close();
        }
    }
}

package site.apt.withredis;

import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LikeSystemTest {
    public static void main(String[] args) throws InterruptedException {
        LikeService likeService = new LikeService();
        String contentId = "post_123";

        // --- 新增：重置数据 ---
        // 你可以在 LikeService 里加一个 delete 方法，或者直接用 Jedis 处理
        try (Jedis jedis = new Jedis("192.168.1.100", 6379)) {
            jedis.del("like_count:" + contentId);
            System.out.println("已重置 Redis 数据");
        }

        // 统计实际执行的点赞次数
        AtomicInteger actualLikeCount = new AtomicInteger(0);

        try {
            // 创建固定大小的线程池模拟并发
            ExecutorService executor = Executors.newFixedThreadPool(10);

            // 模拟 1000 次点赞操作
            for (int i = 0; i < 1000; i++) {
                executor.submit(() -> {
                    likeService.like(contentId);
                    actualLikeCount.incrementAndGet();});
            }

            // 关闭线程池并等待所有任务完成
            executor.shutdown();
            boolean isFinished = executor.awaitTermination(2, TimeUnit.MINUTES);

            // 打印实际执行的任务数
            System.out.println("实际执行的点赞任务数: " + actualLikeCount.get());
            System.out.println("最终点赞数: " + likeService.getLikeCount(contentId));

            if (!isFinished) {
                System.out.println("部分任务未执行完成");
            }

            // 输出最终点赞数
            System.out.println("最终点赞数: " + likeService.getLikeCount(contentId));
        } finally {
            // 确保关闭 Redis 连接
            likeService.close();
        }
    }
}

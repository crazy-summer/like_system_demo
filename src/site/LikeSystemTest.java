package site;// LikeSystemTest.java
import site.apt.withredis.LikeService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LikeSystemTest {
    public static void main(String[] args) throws InterruptedException {
        site.apt.withredis.LikeService likeService = new LikeService();
        String contentId = "post_123";

        // 创建固定大小的线程池模拟并发
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // 模拟 1000 次点赞操作
        for (int i = 0; i < 1000; i++) {
            executor.submit(() -> likeService.like(contentId));
        }

        // 关闭线程池并等待任务完成
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // 输出最终点赞数
        System.out.println("最终点赞数: " + likeService.getLikeCount(contentId));
    }
}

package site;// LikeService.java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LikeService {
    // 使用 ConcurrentHashMap 保证线程安全
    private final ConcurrentHashMap<String, AtomicInteger> likeCountMap = new ConcurrentHashMap<>();

    /**
     * 点赞操作
     */
    public void like(String contentId) {
        likeCountMap.computeIfAbsent(contentId, k -> new AtomicInteger(0)).incrementAndGet();
    }

    /**
     * 取消点赞操作
     */
    public void unlike(String contentId) {
        likeCountMap.computeIfPresent(contentId, (k, v) -> {
            if (v.get() > 0) {
                v.decrementAndGet();
            }
            return v;
        });
    }

    /**
     * 获取点赞数
     */
    public int getLikeCount(String contentId) {
        AtomicInteger count = likeCountMap.get(contentId);
        return count == null ? 0 : count.get();
    }
}

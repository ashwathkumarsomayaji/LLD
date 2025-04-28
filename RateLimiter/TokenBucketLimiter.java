package ratelimiter;


import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

interface ITokenBucketLimiter {
    Boolean allow(String userId);
}
public class TokenBucketLimiter implements  ITokenBucketLimiter {
    private final ReentrantLock lock = new ReentrantLock();

    private int capacity;
    private int refillRatePerSecond;

    public TokenBucketLimiter(int capacity, int refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
    }

    static class Bucket {
        double tokens;
        long lastRefilledTimeMs;
    }

    Map<String, Bucket> userBucket = new HashMap<>();


    @Override
    public Boolean allow(String userId) {
        lock.lock();
        long now = Instant.EPOCH.toEpochMilli();
        Bucket bucket = userBucket.computeIfAbsent(userId, s -> {
            Bucket b = new Bucket();
            b.tokens = capacity;
            b.lastRefilledTimeMs = now;
            return b;
        });

        try {
            refill(bucket, now);
            if (bucket.tokens >= 1) {
                bucket.tokens -= 1;
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    private void refill(Bucket bucket, long now) {
        long elapsedTimeMs = now - bucket.lastRefilledTimeMs;
        double tokensToAdd = (elapsedTimeMs / 1000) * refillRatePerSecond;

        if (tokensToAdd > 0) {

            bucket.tokens = Math.min(capacity, bucket.tokens + tokensToAdd);
            bucket.lastRefilledTimeMs = now;
        }
    }

    class Main {
        public static void main(String[] args) throws InterruptedException {
            ITokenBucketLimiter limiter = new TokenBucketLimiter(5, 2);  // 5 tokens max, refills 2 tokens/sec

            String user = "ashwath";

            System.out.println("Burst 6 requests:");
            for (int i = 1; i <= 6; i++) {
                System.out.println(i + ": " + limiter.allow(user));
            }

            System.out.println("\nWaiting 2.5 seconds...");
            Thread.sleep(2500);

            System.out.println("\nNext requests after refill:");
            for (int i = 7; i <= 10; i++) {
                System.out.println(i + ": " + limiter.allow(user));
            }
        }
    }
}

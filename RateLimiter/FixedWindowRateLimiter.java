package ratelimiter;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

interface  RateLimiter {
    boolean allow(String userId);
}
public class FixedWindowRateLimiter implements RateLimiter {

    final int maxRequests;
    final long windowSizeMs;

    public FixedWindowRateLimiter(int maxRequests, int windowSizeSeconds) {
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSizeSeconds * 1000L;
    }

    private final ReentrantLock lock = new ReentrantLock();

    final Map<String, Window> userWindows  = new ConcurrentHashMap<>();
        class Window {
            long windowStart;
            int count;

            public Window(long windowStart, int count) {
                this.windowStart = windowStart;
                this.count = count;
            }
        }

    @Override
    public boolean allow (String userId) {
        lock.lock();
        long now = Instant.EPOCH.toEpochMilli();

        try {
            Window userWindow = userWindows.get(userId);
            if (userWindow == null || now - userWindow.windowStart >= windowSizeMs) {
                //New Window
                userWindows.put(userId, new Window(now, 1));
                return true;
            } else {
                //same window
                if (userWindow.count < maxRequests) {
                    userWindow.count++;
                    return true;
                } else {
                    return false;
                }
            }
        }
        finally {
            lock.unlock();
        }
    }
    class Main {
        public static void main(String[] args) throws InterruptedException {
            RateLimiter limiter = new FixedWindowRateLimiter(3, 5); // Allow 3 requests per 5 seconds

            String user = "ashwath";

            System.out.println("First 3 calls:");
            for (int i = 1; i <= 3; i++) {
                System.out.println(i + ": " + limiter.allow(user));
            }

            System.out.println("\n4th call (should be blocked):");
            System.out.println("4: " + limiter.allow(user));

            System.out.println("\nWaiting for window reset...");
            Thread.sleep(6000);  // wait 6 seconds to reset window

            System.out.println("\nAfter window reset:");
            for (int i = 5; i <= 7; i++) {
                System.out.println(i + ": " + limiter.allow(user));
            }
        }
}
}


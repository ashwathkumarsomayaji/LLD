package keyvaluewithttl;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
/* -------------------------------------------------------------- *
 *  1.  Model: one Entry per key                                   *
 * -------------------------------------------------------------- */
final class Entry {
    final String value;
    final long   expiresAtMillis;          // Long.MAX_VALUE ⇒ immortal
    Entry(String value, long ttlMillis) {
        this.value        = value;
        this.expiresAtMillis = ttlMillis == 0
                ? Long.MAX_VALUE
                : System.currentTimeMillis() + ttlMillis;
    }
    /** true if value is still valid right now */
    boolean alive() { return System.currentTimeMillis() <= expiresAtMillis; }
}
/* -------------------------------------------------------------- *
 *  2.  Key–Value store with TTL                                   *
 * -------------------------------------------------------------- */
public class KeyValueStore {
    /** the whole DB (thread-safe) */
    private final Map<String, Entry> map = new ConcurrentHashMap<>();
    /** optional background GC that evicts dead keys every 10 s */
    private final ScheduledExecutorService gc =
            Executors.newSingleThreadScheduledExecutor();
    public KeyValueStore() {
        gc.scheduleAtFixedRate(
                () -> map.entrySet().removeIf(e -> !e.getValue().alive()),
                10, 10, TimeUnit.SECONDS);
    }
    /* ---- CRUD -------------------------------------------------------- */
    /** put key with TTL - `ttlMillis==0` means “never expire” */
    public void put(String key, String value, long ttlMillis) {
        map.put(key, new Entry(value, ttlMillis));
    }
    /** convenience overload for TTL in seconds */
    public void putSeconds(String key, String value, long ttlSeconds) {
        put(key, value, ttlSeconds * 1000);
    }
    /** get value or `null` if missing / expired */
    public String get(String key) {
        Entry e = map.get(key);
        return (e != null && e.alive()) ? e.value : null;
    }
    /** delete regardless of TTL */
    public void delete(String key) { map.remove(key); }
    /** visible size (may include keys the GC hasn’t swept yet) */
    public int size() { return map.size(); }
    /** call when your application shuts down */
    public void close() { gc.shutdownNow(); }
    /* -------------------------------------------------------------- *
     * quick demo                                                     *
     * -------------------------------------------------------------- */
    public static void main(String[] args) throws InterruptedException {
        KeyValueStore kv = new KeyValueStore();
        kv.putSeconds("token", "XYZ", 2);        // 2-second TTL
        kv.put("name", "Alice", 0);              // immortal
        System.out.println("t=0   token -> " + kv.get("token"));  // XYZ
        Thread.sleep(2_500);
        System.out.println("t=2.5 token -> " + kv.get("token"));  // null
        System.out.println("name  -> " + kv.get("name"));         // Alice
        kv.delete("name");
        System.out.println("name  -> " + kv.get("name"));         // null
        kv.close();      // stop GC thread
    }
}

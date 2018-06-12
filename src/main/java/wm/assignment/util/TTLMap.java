package wm.assignment.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * An implementation of a map in which entries expire after the provided TTL. Also supplies a way for clients to
 * a handler for expiration notifications
 * @param <K> Key for the map
 * @param <V> Value for the map
 */
public class TTLMap<K, V> extends AbstractMap<K, V> {
    private final static Log log = LogFactory.getLog(TTLMap.class);

    /**
     * Internal class used to wrap the value object. Keeps track of the timestamp when it was added to the map as well
     * as a reference to the notifier method
     */
    private class ValueWrapper {
        V value;
        LocalDateTime timestamp;
        Consumer<V> notifier;

        ValueWrapper(V value, Consumer<V> notifier) {
            this.value = value;
            this.timestamp = LocalDateTime.now();
            this.notifier = notifier;
        }
    }

    private long ttl;
    private Map<K, ValueWrapper> map;

    private ScheduledExecutorService executor;
    private final ReentrantLock lock = new ReentrantLock();

    public TTLMap(long ttl) {
        this(ttl, 5000);
    }

    TTLMap(long ttl, long expirationInterval) {
        this.ttl = ttl;
        this.map = new ConcurrentHashMap<>();

        executor = Executors.newSingleThreadScheduledExecutor();
        ExecutorRegistry.register(executor);

        startCleanupTask(expirationInterval);
    }

    /**
     * Produces an entrySet of the key/value Pairs. this is a bit more complicated than asking for the entrySet
     * directly from the map because the map actually contains K, ValueWrapper(V) pairs.
     * @return
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet().stream()
            .map(entry -> new SimpleEntry<>(entry.getKey(), entry.getValue().value))
            .collect(Collectors.toSet());
    }

    @Override
    public V get(Object key) {
        ValueWrapper result = this.map.get(key);

        if (result == null) {
            return null;
        }

        return result.value;
    }

    @Override
    public V put(K key, V value) {
        this.map.put(key, new ValueWrapper(value, null));
        return value;
    }

    public V put(K key, V value, Consumer<V> notifier) {
        this.map.put(key, new ValueWrapper(value, notifier));
        return value;
    }

    /**
     * Runs a scheduled task to periodically check for expired entries. If found, send a notification to the
     * notifier (if supplied) and remove the item from the map
     * @param expirationInterval
     */
    private void startCleanupTask(long expirationInterval) {
        Predicate<Entry<K,ValueWrapper>> isExpired = (entry) -> {
            long elapsedTime = ChronoUnit.MILLIS.between(entry.getValue().timestamp, LocalDateTime.now());
            return elapsedTime >= ttl;
        };

        Runnable expireLoop = () -> {
            lock.lock();
            log.debug("Invoking expireLoop() in TTLMap...");

            try {
                List<K> expiredKeys = map.entrySet().stream()
                    .filter(isExpired)
                    .map(Entry::getKey)
                    .collect(Collectors.toList());

                log.debug("Number of keys to expire: " + expiredKeys.size());

                expiredKeys.stream().map(map::get).forEach((v) -> {
                    if (v.notifier != null) {
                        v.notifier.accept(v.value);
                    }
                });
                expiredKeys.forEach(map::remove);
            }
            finally {
                lock.unlock();
            }
        };

        executor.scheduleWithFixedDelay(expireLoop, 0, expirationInterval, TimeUnit.MILLISECONDS);
    }

}

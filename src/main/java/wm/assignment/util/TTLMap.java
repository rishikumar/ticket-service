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


public class TTLMap<K, V> extends AbstractMap<K, V> {
    private final static Log log = LogFactory.getLog(TTLMap.class);

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

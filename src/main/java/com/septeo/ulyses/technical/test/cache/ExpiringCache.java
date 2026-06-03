package com.septeo.ulyses.technical.test.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A thread-safe cache whose entries expire after a fixed time-to-live.
 * The check and refresh are performed atomically per key, so concurrent callers
 * never race on an expired entry and the loader runs at most once per key per refresh.
 *
 * @param <K> the cache key type
 * @param <V> the cached value type
 */
public class ExpiringCache<K, V> {

    private final ConcurrentHashMap<K, CacheEntry<V>> store = new ConcurrentHashMap<>();
    private final long ttlMillis;

    public ExpiringCache(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    /**
     * Return the cached value for the key, loading and storing it if absent or expired.
     *
     * @param key    the cache key
     * @param loader the supplier used to load the value on a miss or expiry
     * @return the cached or freshly loaded value
     */
    public V get(K key, Supplier<V> loader) {
        long now = System.currentTimeMillis();
        CacheEntry<V> entry = store.compute(key, (k, current) -> {
            if (current != null && !current.isExpired(now)) {
                return current;
            }
            return new CacheEntry<>(loader.get(), now + ttlMillis);
        });
        return entry.value();
    }

    /**
     * Remove all entries from the cache.
     */
    public void invalidateAll() {
        store.clear();
    }

    private record CacheEntry<V>(V value, long expiresAt) {

        /**
         * Check whether this entry has expired.
         *
         * @param now the current time in milliseconds
         * @return {@code true} if the entry's time-to-live has elapsed
         */
        private boolean isExpired(long now) {
            return now >= expiresAt;
        }
    }
}

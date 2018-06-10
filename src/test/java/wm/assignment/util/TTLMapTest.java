package wm.assignment.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TTLMapTest {

    @Test
    void testNoExpirationEntry() {
        TTLMap<Integer, String> ttlMap = new TTLMap<>(100);
        ttlMap.put(1, "one");

        assertEquals("one", ttlMap.get(1));
    }

    @Test
    void testCacheExpiration() {
        TTLMap<Integer, String> ttlMap = new TTLMap<>(1, 1);
        ttlMap.put(1, "one");

        // TODO:
        // let the cache expire - yes, I know this isn't the best way to test the cache invalidation...
        // a better approach would be to mock the behavior of the expiration thread to expire the entry on demand
        // instead of relying on timing logic
        try {
            Thread.sleep(5);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertNull(ttlMap.get(1));
    }

    @Test
    void testNotificationOfExpiration() {
        final List<String> notifiedValues = new ArrayList<>();
        Consumer<String> notifyMe = notifiedValues::add;

        TTLMap<Integer, String> ttlMap = new TTLMap<>(1, 1);
        ttlMap.put(1, "one", notifyMe);


        // TODO:
        // let the cache expire - yes, I know this isn't the best way to test the cache invalidation...
        // a better approach would be to mock the behavior of the expiration thread to expire the entry on demand
        // instead of relying on timing logic
        try {
            Thread.sleep(5);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertNull(ttlMap.get(1));
        assertEquals(1, notifiedValues.size());
        assertEquals("one", notifiedValues.get(0));
    }


}

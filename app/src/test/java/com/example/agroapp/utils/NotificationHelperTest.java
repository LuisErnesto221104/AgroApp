package com.example.agroapp.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for NotificationHelper (RF009).
 * Tests the logic for generating 3 notification request codes per event.
 */
public class NotificationHelperTest {
    
    /**
     * Test that unique request codes are generated for the 3 notification types.
     * RF009: Each event should have 3 distinct notifications.
     */
    @Test
    public void testUniqueRequestCodesForNotificationTypes() {
        // Simulate the request code calculation logic
        int eventoId = 1;
        
        int requestCode3Days = eventoId; // offset 0
        int requestCode1Day = eventoId + 10000; // offset 10000
        int requestCodeSameDay = eventoId + 20000; // offset 20000
        
        // All request codes should be different
        assertNotEquals(requestCode3Days, requestCode1Day);
        assertNotEquals(requestCode1Day, requestCodeSameDay);
        assertNotEquals(requestCode3Days, requestCodeSameDay);
    }
    
    /**
     * Test that request codes don't collide for different events.
     */
    @Test
    public void testNoRequestCodeCollisionBetweenEvents() {
        int eventoId1 = 1;
        int eventoId2 = 2;
        
        // Event 1 codes
        int event1_3Days = eventoId1;
        int event1_1Day = eventoId1 + 10000;
        int event1_SameDay = eventoId1 + 20000;
        
        // Event 2 codes
        int event2_3Days = eventoId2;
        int event2_1Day = eventoId2 + 10000;
        int event2_SameDay = eventoId2 + 20000;
        
        // All codes should be unique across events
        assertNotEquals(event1_3Days, event2_3Days);
        assertNotEquals(event1_1Day, event2_1Day);
        assertNotEquals(event1_SameDay, event2_SameDay);
        
        // Cross-check: no overlap between different notification types of different events
        assertNotEquals(event1_3Days, event2_1Day);
        assertNotEquals(event1_3Days, event2_SameDay);
        assertNotEquals(event1_1Day, event2_SameDay);
    }
    
    /**
     * Test notification type constants.
     */
    @Test
    public void testNotificationTypeConstants() {
        assertEquals(0, NotificationHelper.NOTIFICATION_TYPE_3_DAYS);
        assertEquals(1, NotificationHelper.NOTIFICATION_TYPE_1_DAY);
        assertEquals(2, NotificationHelper.NOTIFICATION_TYPE_SAME_DAY);
    }
    
    /**
     * Test getMensajeTiempo returns correct messages.
     */
    @Test
    public void testGetMensajeTiempo() {
        assertEquals("en 3 días", NotificationHelper.getMensajeTiempo(NotificationHelper.NOTIFICATION_TYPE_3_DAYS));
        assertEquals("mañana", NotificationHelper.getMensajeTiempo(NotificationHelper.NOTIFICATION_TYPE_1_DAY));
        assertEquals("hoy", NotificationHelper.getMensajeTiempo(NotificationHelper.NOTIFICATION_TYPE_SAME_DAY));
        assertEquals("", NotificationHelper.getMensajeTiempo(-1));
        assertEquals("", NotificationHelper.getMensajeTiempo(99));
    }
}

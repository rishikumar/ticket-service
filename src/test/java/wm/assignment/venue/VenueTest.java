package wm.assignment.venue;

import org.junit.jupiter.api.Test;
import wm.assignment.exception.VenueException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static wm.assignment.util.TestUtil.assertBlock;

class VenueTest {

    @Test
    void testNumSeatsAvailable() {
        Venue v = new Venue(2, 10, 10000);
        assertEquals(20, v.numSeatsAvailable());
    }

    @Test
    void testFindAndHoldSeats() {
        Venue v = new Venue(2, 10, 10000);

        // Hold 1 - should get placed in the first row
        SeatHold hold = v.findAndHoldSeats(8, "a@a.com");
        assertBlock(hold.getBlock(), SeatBlockType.HOLD, 0, 0, 8);

        // Hold 2 - should get placed in the second row
        hold = v.findAndHoldSeats(4, "b@b.com");
        assertBlock(hold.getBlock(), SeatBlockType.HOLD, 1, 0, 4);

        // Hold 3 - should also get placed in the second row
        hold = v.findAndHoldSeats(4, "c@c.com");
        assertBlock(hold.getBlock(), SeatBlockType.HOLD, 1, 4, 4);

        // Confirm that we have two open blocks left - 2 seats in both rows
        List<SeatBlock> openBlocks = v.findOpenBlocks();

        assertEquals(2, openBlocks.size());
        assertBlock(openBlocks.get(0), SeatBlockType.UNRESERVED, 0, 8, 2);
        assertBlock(openBlocks.get(1), SeatBlockType.UNRESERVED, 1, 8, 2);
    }

    @Test
    void testHoldFailure() {
        Venue v = new Venue(2, 10, 10000);

        // test hold failure if the hold request is >= number of rows
        assertNull(v.findAndHoldSeats(11, "a@a.com"));

        // populate the venue with other holds and confirm that we cannot hold additional seats
        assertNotNull(v.findAndHoldSeats(8, "a@a.com"));
        assertNotNull(v.findAndHoldSeats(8, "a@a.com"));
        assertNull(v.findAndHoldSeats(3, "a@a.com"));
    }

    @Test
    void testSuccessfulConfirmation() {
        Venue v = new Venue(2, 10, 10000);

        SeatHold hold = v.findAndHoldSeats(8, "a@a.com");
        String confirmId = v.reserveSeats(hold.getId(), "a@a.com");
        assertNotNull(confirmId);

        SeatBlock reservedBlock = v.findReservation(confirmId);
        assertBlock(reservedBlock, SeatBlockType.RESERVED, 0, 0, 8);
    }

    @Test
    void testFailedConfirmation() {
        Venue v = new Venue(2, 10, 10000);

        // an invalid reservation
        assertNull(v.reserveSeats(1111, "a@a.com"));

        // mismatch on email
        SeatHold hold = v.findAndHoldSeats(8, "a@a.com");
        assertThrows(VenueException.class, () -> v.reserveSeats(hold.getId(), "b@b.com"));
    }

    @Test
    void testExpiredHold() {
        Venue v = new Venue(2, 10, 1);

        SeatHold hold = v.findAndHoldSeats(8, "a@a.com");

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

        assertNull(v.reserveSeats(hold.getId(), "a@a.com"));
    }
}

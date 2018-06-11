package wm.assignment.venue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static wm.assignment.util.TestUtil.assertBlock;

import java.util.List;
import java.util.stream.Collectors;

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
        List<SeatBlock> openBlocks = v.getRows().stream()
            .flatMap(r -> r.getBlocks().stream())
            .filter(b -> b.getBlockType() == SeatBlockType.UNRESERVED)
            .collect(Collectors.toList());

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
    void testHoldExpiration() {

    }

    @Test
    void testSuccessfulConfirmation() {

    }

    @Test
    void testFailedConfirmation() {

    }


}

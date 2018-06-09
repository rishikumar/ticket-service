package wm.assignment.venue;


import org.junit.jupiter.api.Test;
import wm.assignment.exception.VenueException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RowTest {
    @Test
    void testInitializationSeatsAvailable() {
        Row r = new Row(0, 50);
        assertEquals(50, r.totalAvailableSeatCount());
    }

    @Test
    void testBadInitializationData() {
        assertThrows(VenueException.class, () -> new Row(0,0));
        assertThrows(VenueException.class, () -> new Row(0,-1));
    }

    @Test
    void testSeatBlockSplitLogic() {
        Row r = new Row(0, 50);

        SeatBlock availableBlock = r.getBlocks().get(0);
        SeatHold hold = r.holdSeats(availableBlock, 10, "a@a.com");
        List<SeatBlock> blocks = r.getBlocks();

        assertEquals(10, blocks.get(0).getNumSeats());
        assertEquals(40, blocks.get(1).getNumSeats());

        availableBlock = r.getBlocks().get(1);
        hold = r.holdSeats(availableBlock, 20, "b@b.com");
        blocks = r.getBlocks();

        assertEquals(10, blocks.get(0).getNumSeats());
        assertEquals(20, blocks.get(1).getNumSeats());
        assertEquals(20, blocks.get(2).getNumSeats());

    }
}

package wm.assignment.venue;


import org.junit.jupiter.api.Test;
import wm.assignment.exception.VenueException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        Row.HoldUpdate hu = r.holdSeats(availableBlock, 10, "a@a.com");
        List<SeatBlock> blocks = hu.row.getBlocks();

        assertBlock(blocks.get(0), 10, 0);
        assertBlock(blocks.get(1), 40, 10);

        availableBlock = hu.row.getBlocks().get(1);
        hu = hu.row.holdSeats(availableBlock, 20, "b@b.com");
        blocks = hu.row.getBlocks();

        assertBlock(blocks.get(0), 10, 0);
        assertBlock(blocks.get(1), 20, 10);
        assertBlock(blocks.get(2), 20, 30);

    }

    private void assertBlock(SeatBlock block, int numSeats, int startPosition) {
        assertEquals(numSeats, block.getNumSeats());
        assertEquals(startPosition, block.getStartPosition());
    }

}

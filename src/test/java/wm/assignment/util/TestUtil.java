package wm.assignment.util;

import wm.assignment.venue.SeatBlock;
import wm.assignment.venue.SeatBlockType;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtil {
    public static void assertBlock(SeatBlock block,
                                   SeatBlockType blockType,
                                   int rowNum,
                                   int startPosition,
                                   int numSeats) {
        assertEquals(blockType, block.getBlockType());
        assertEquals(rowNum, block.getRowNum());
        assertEquals(startPosition, block.getStartPosition());
        assertEquals(numSeats, block.getNumSeats());
    }
}


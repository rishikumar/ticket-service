package wm.assignment.venue;

import wm.assignment.exception.VenueException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;


class Row {
    // filters
    private static Predicate<SeatBlock> onlyUnreserved = (SeatBlock b) -> b.getBlockType() == SeatBlockType.UNRESERVED;

    private List<SeatBlock> blocks = new ArrayList<>();

    Row(int rowNum, int numSeats) {
        if (numSeats <= 0) {
            throw new VenueException("Cannot create a row with less than one seat");
        }

        // initialize an empty (unreserved) block of seats from seat 0 to the number of seats
        blocks.add(new SeatBlock(SeatBlockType.UNRESERVED, rowNum, 0, numSeats));
    }

    public List<SeatBlock> getBlocks() {
        return blocks;
    }

    int totalAvailableSeatCount() {
        return blocks.stream()
            .filter(onlyUnreserved)
            .map(SeatBlock::getNumSeats)
            .reduce((a, b) -> a + b)
            .orElse(0);

    }

    Stream<SeatBlock> firstAvailableBlock(int numSeats) {
        return blocks.stream()
            .filter(onlyUnreserved)
            .filter(b -> b.getNumSeats() >= numSeats);
    }

    synchronized SeatHold holdSeats(SeatBlock block, int numSeats, String customerEmail) {
        // sanity check
        if (block.getNumSeats() < numSeats) {
            throw new VenueException("Cannot reserve seats - not enough are available in the block");
        }

        int blockIndex = blocks.indexOf(block);

        // create a block for this reservation and add it to the beginning of the list
        SeatBlock holdBlock = new SeatBlock(SeatBlockType.HOLD, block.getRowNum(), block.getStartPosition(), numSeats);

        // prepend the new block before the input availability block
        blocks.add(blockIndex, holdBlock);

        // remove the existing block
        blocks.remove(block);

        // if we have extra seats left over in the block, add it after the new hold block
        if (block.getNumSeats() > numSeats) {
            int startPosition = block.getStartPosition() + numSeats;
            SeatBlock remainingAvailableBlock
                = new SeatBlock(SeatBlockType.UNRESERVED, block.getRowNum(),
                startPosition, block.getNumSeats() - numSeats);

            blocks.add(blockIndex + 1, remainingAvailableBlock);
        }

        return new SeatHold(holdBlock, customerEmail);
    }

}

package wm.assignment.venue;

import wm.assignment.exception.VenueException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;


class Row {
    class HoldUpdate {
        Row row;
        SeatHold hold;

        HoldUpdate(Row row, SeatHold hold) {
            this.row = row;
            this.hold = hold;
        }
    }


    // filters
    private static Predicate<SeatBlock> onlyUnreserved = (SeatBlock b) -> b.getBlockType() == SeatBlockType.UNRESERVED;

    private List<SeatBlock> blocks;
    private int rowNum;
    private int numSeats;

    Row(int rowNum, int numSeats) {
        this(rowNum, numSeats, null);
    }

    private Row(int rowNum, int numSeats, List<SeatBlock> blocks) {
        this.rowNum = rowNum;
        this.numSeats = numSeats;

        if (numSeats <= 0) {
            throw new VenueException("Cannot create a row with less than one seat");
        }

        if (blocks != null) {
            this.blocks = blocks;
        }
        else {
            this.blocks = new ArrayList<>();

            // initialize an empty (unreserved) block of seats from seat 0 to the number of seats
            this.blocks.add(new SeatBlock(SeatBlockType.UNRESERVED, rowNum, 0, numSeats));
        }

    }

    int getRowNum() {
        return this.rowNum;
    }

    List<SeatBlock> getBlocks() {
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

    HoldUpdate holdSeats(SeatBlock block, int numSeats, String customerEmail) {
        // sanity check
        if (block.getNumSeats() < numSeats) {
            throw new VenueException("Cannot reserve seats - not enough are available in the block");
        }

        Row newRow = new Row(this.rowNum, this.numSeats, new ArrayList<>());
        SeatBlock holdBlock = new SeatBlock(SeatBlockType.HOLD, block.getRowNum(), block.getStartPosition(), numSeats);

        // loop through the current set of blocks and generate a new one
        for (SeatBlock sb : blocks) {
            // add all other blocks to the new row untouched, maintaining their order
            if (sb != block) {
                newRow.blocks.add(sb);
                continue;
            }

            // create a block for this reservation add add it to the right location in the row
            newRow.blocks.add(holdBlock);

            // if we have extra seats left over in the block, add it after the new hold block
            if (sb.getNumSeats() > numSeats) {
                int startPosition = sb.getStartPosition() + numSeats;
                SeatBlock remainingAvailableBlock = new SeatBlock(SeatBlockType.UNRESERVED, sb.getRowNum(),
                    startPosition, sb.getNumSeats() - numSeats);

                newRow.blocks.add(remainingAvailableBlock);
            }

        }

        return new HoldUpdate(newRow, new SeatHold(holdBlock, customerEmail));
    }

    Row withBlockUnreserved(SeatBlock blockToReplace) {
        SeatBlock newUnreservedBlock = new SeatBlock(SeatBlockType.UNRESERVED,
            blockToReplace.getRowNum(), blockToReplace.getStartPosition(), blockToReplace.getNumSeats());

        int blockIndex = blocks.indexOf(blockToReplace);
        List<SeatBlock> newBlocks = new ArrayList<>(blocks);
        newBlocks.set(blockIndex, newUnreservedBlock);

        return withBlocksMerged(rowNum, numSeats, newBlocks);
    }

    Row withBlockReserved(SeatBlock heldBlock) {
        SeatBlock newReservedBlock = new SeatBlock(SeatBlockType.RESERVED,
            heldBlock.getRowNum(), heldBlock.getStartPosition(), heldBlock.getNumSeats());

        int blockIndex = blocks.indexOf(heldBlock);
        List<SeatBlock> newBlocks = new ArrayList<>(blocks);
        newBlocks.set(blockIndex, newReservedBlock);

        return new Row(rowNum, numSeats, newBlocks);
    }

    private static Row withBlocksMerged(int rowNum, int numSeats, List<SeatBlock> unmergedBlocks) {
        List<SeatBlock> blocks = new ArrayList<>();

        SeatBlock currentUnreservedBlock = null;

        for (SeatBlock sb : unmergedBlocks) {
            // case 1: if we find a block that isn't unreserved, we no longer have a contiguous unreserved block
            if (sb.getBlockType() != SeatBlockType.UNRESERVED) {
                if (currentUnreservedBlock != null) {
                    // if we're currently processing an unreserved block and we've encountered another block that is reserved,
                    // we want to add it to the new list
                    blocks.add(currentUnreservedBlock);
                    currentUnreservedBlock = null;
                }

                blocks.add(sb);
                continue;
            }

            // case 2: here we've found an unreserved block - this block will be come the current unreserved block
            if (currentUnreservedBlock == null) {
                currentUnreservedBlock = sb;
                continue;
            }

            currentUnreservedBlock = SeatBlock.merge(currentUnreservedBlock, sb);
        }

        // add the last unreserved block if it hasn't been processed
        if (currentUnreservedBlock != null) {
            blocks.add(currentUnreservedBlock);
        }

        return new Row(rowNum, numSeats, blocks);
    }

}

package wm.assignment.venue;

class SeatBlock {
    private SeatBlockType blockType;
    private int rowNum;
    private int startPosition;
    private int numSeats;

    SeatBlock(SeatBlockType blockType, int rowNum, int startPosition, int numSeats) {
        this.blockType = blockType;
        this.rowNum = rowNum;
        this.startPosition = startPosition;
        this.numSeats = numSeats;
    }

    SeatBlockType getBlockType() {
        return this.blockType;
    }

    int getRowNum() {
        return this.rowNum;
    }

    int getStartPosition() {
        return this.startPosition;
    }

    int getNumSeats() {
        return this.numSeats;
    }
}

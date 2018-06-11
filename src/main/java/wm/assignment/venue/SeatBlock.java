package wm.assignment.venue;

public class SeatBlock {
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

    public SeatBlockType getBlockType() {
        return this.blockType;
    }

    public int getRowNum() {
        return this.rowNum;
    }

    public int getStartPosition() {
        return this.startPosition;
    }

    public int getNumSeats() {
        return this.numSeats;
    }
}

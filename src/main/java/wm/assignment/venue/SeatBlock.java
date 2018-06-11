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

    static SeatBlock merge(SeatBlock sb1, SeatBlock sb2) {
        if (sb1 == null || sb2 == null) {
            return null;
        }

        // sanity checks
        assert sb1.getBlockType() == sb2.getBlockType();
        assert sb1.getRowNum() == sb2.getRowNum();

        return new SeatBlock(sb1.blockType, sb1.rowNum, sb1.startPosition, sb1.numSeats + sb2.numSeats);
    }

}

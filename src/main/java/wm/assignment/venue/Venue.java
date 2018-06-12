package wm.assignment.venue;

import wm.assignment.exception.VenueException;
import wm.assignment.util.TTLMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a rectangular venue
 */
public class Venue {
    private List<Row> rows;
    private TTLMap<Integer, SeatHold> heldSeats;
    private Map<String, SeatBlock> reservedSeats;

    public Venue(int numRows, int numColumns, long ttlInMillis) {
        // create all the rows, each initialized with numColumn seats
        rows = IntStream.range(0, numRows)
            .mapToObj((rowNum) -> new Row(rowNum, numColumns))
            .collect(Collectors.toList());

        this.heldSeats = new TTLMap<>(ttlInMillis);
        this.reservedSeats = new ConcurrentHashMap<>();
    }

    /**
     * This method computes the number of seats that are current available.
     * @return number of seats
     */
    public int numSeatsAvailable() {
        // request that each row report its number of seats, and then combine them together
        return rows.stream()
            .map(Row::totalAvailableSeatCount)
            .reduce((a, b) -> a + b)
            .orElse(0);
    }

    /**
     * Find all blocks of a given type in any row
     * @param type the SeatBlockType to query for
     * @return
     */
    public List<SeatBlock> findBlocks(SeatBlockType type) {
        return rows.stream()
            .flatMap(r -> r.getBlocks().stream())
            .filter(b -> b.getBlockType() == type)
            .collect(Collectors.toList());
    }

    /**
     * This method attempts to locate a block of seats and hold them
     * @param numSeats
     * @param customerEmail
     * @return
     */
    public synchronized SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        SeatBlock firstAvailableBlock = rows.stream()
            .flatMap(r -> r.firstAvailableBlock(numSeats))
            .findFirst()
            .orElse(null);

        if (firstAvailableBlock == null) {
            // couldn't find an available block - return nothing
            return null;
        }

        // save the reservation in the first available row
        Row firstAvailableRow = rows.get(firstAvailableBlock.getRowNum());
        Row.HoldUpdate holdUpdate = firstAvailableRow.holdSeats(firstAvailableBlock, numSeats, customerEmail);

        Row row = holdUpdate.row;
        SeatHold hold = holdUpdate.hold;

        // replace the current row with the new one and save the hold to the hold map
        rows.set(row.getRowNum(), row);
        heldSeats.put(hold.getId(), hold, this::handleExpiredHold);

        return hold;
    }

    /**
     * If the held seats are still available (i.e. the hold hasn't expired) this method reserves them
     * @return
     */
    public synchronized String reserveSeats(int seatHoldId, String customerEmail) {
        SeatHold hold = heldSeats.get(seatHoldId);

        if (hold == null) {
            // seatHoldId couldn't be find - might have expired or the client passed a bad seatHoldId
            return null;
        }

        if (!hold.getCustomerEmail().equals(customerEmail)) {
            throw new VenueException("Email mismatch when attempting to reserve a held reservation");
        }

        String confirmId = UUID.randomUUID().toString();

        // move the reservation to the reserved map
        heldSeats.remove(seatHoldId);
        SeatBlock reservedBlock = getReservedBlock(hold.getBlock());
        reservedSeats.put(confirmId, reservedBlock);

        return confirmId;
    }

    public SeatBlock findReservation(String confirmId) {
        return reservedSeats.get(confirmId);
    }


    private void handleExpiredHold(SeatHold expiredHold) {
        Row existingRow = rows.get(expiredHold.getBlock().getRowNum());
        Row newRow = existingRow.withBlockUnreserved(expiredHold.getBlock());
        rows.set(existingRow.getRowNum(), newRow);
    }

    private SeatBlock getReservedBlock(SeatBlock heldBlock) {
        Row exitingRow = rows.get(heldBlock.getRowNum());
        int blockIndex = exitingRow.getBlocks().indexOf(heldBlock);

        Row newRow = exitingRow.withBlockReserved(heldBlock);
        rows.set(exitingRow.getRowNum(), newRow);
        return newRow.getBlocks().get(blockIndex);
    }

}

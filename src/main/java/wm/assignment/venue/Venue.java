package wm.assignment.venue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Venue {
    private List<Row> rows;
    private Map<Integer, SeatHold> heldReservations = new HashMap<>();

    public Venue(int numRows, int numColumns) {
        // create all the rows, each initialized with numColumn seats
        rows = IntStream.range(0, numRows)
            .mapToObj((rowNum) -> new Row(rowNum, numColumns))
            .collect(Collectors.toList());
    }

    /**
     * This method computes the number of seats that are current available. This method may return a dirty read of
     * the current state of the venue. This is by design.
     * @return number of seats
     */
    public int numSeatsAvailable() {
        // request that each row report its number of seats, and then combine them together
        return rows.stream()
            .map(Row::totalAvailableSeatCount)
            .reduce((a, b) -> a + b)
            .orElse(0);
    }

    public synchronized SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        SeatBlock firstAvailableBlock = rows.stream()
            .flatMap(r -> r.firstAvailableBlock(numSeats))
            .findFirst()
            .orElse(null);

        if (firstAvailableBlock == null) {
            // couldn't find an available block - return nothing
            return null;
        }

        Row firstAvailableRow = rows.get(firstAvailableBlock.getRowNum());

        SeatHold hold = firstAvailableRow.holdSeats(firstAvailableBlock, numSeats, customerEmail);
        heldReservations.put(hold.getId(), hold);
        return hold;
    }
}

package wm.assignment.venue;

import java.util.concurrent.ThreadLocalRandom;

public class SeatHold {

    private int id;
    private SeatBlock block;
    private String customerEmail;

    public SeatHold(SeatBlock block, String customerEmail) {
        // this is not the best way to maintain a randomly generated ID to maintain uniqueness of reservations,
        // but it fulfills the contract of the seatHoldId being an int
        // See TicketService.reserveSeats(int seatHoldId, String customerEmail)
        this.id = ThreadLocalRandom.current().nextInt();

        this.block = block;
        this.customerEmail = customerEmail;
    }

    public int getId() {
        return id;
    }

    public SeatBlock getBlock() {
        return block;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }
}

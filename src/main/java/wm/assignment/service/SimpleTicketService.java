package wm.assignment.service;

import wm.assignment.venue.SeatHold;
import wm.assignment.venue.Venue;

public class SimpleTicketService implements TicketService {

    private Venue venue;

    public SimpleTicketService(int numRows, int numColumns, long ttlInMillis) {
        this.venue = new Venue(numRows, numColumns, ttlInMillis);
    }

    /**
     * The number of seats in the venue that are neither held nor reserved
     *
     * @return the number of tickets available in the venue
     */
    public int numSeatsAvailable() {
        return venue.numSeatsAvailable();
    }

    /**
     * Find and hold the best available seats for a customer
     *
     * @param numSeats      the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return a SeatHold object identifying the specific seats and related
     * information
     */
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        return venue.findAndHoldSeats(numSeats, customerEmail);
    }

    /**
     * Commit seats held for a specific customer
     *
     * @param seatHoldId    the seat hold identifier
     * @param customerEmail the email address of the customer to which the
     *                      seat hold is assigned
     * @return a reservation confirmation code
     */
    public String reserveSeats(int seatHoldId, String customerEmail) {
        throw new UnsupportedOperationException();
    }

}

package wm.assignment.app;

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import wm.assignment.service.SimpleTicketService;
import wm.assignment.util.ExecutorRegistry;
import wm.assignment.venue.SeatBlockType;
import wm.assignment.venue.SeatHold;
import wm.assignment.venue.Venue;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * This class Simulates multiple clients attempting to hold and reserve seats using the TicketService.
 * Also provides a command line interface for invoking the simulation.
 */
public class AppSimulator {
    private final static Log log = LogFactory.getLog(AppSimulator.class);

    private SimpleTicketService ticketService;
    private int numWorkers;

    /**
     * Constructor that initializes the application
     * @param cl
     */
    private AppSimulator(CommandLine cl) {
        int numRows = Integer.parseInt(cl.getOptionValue("rows"));
        int numColumns = Integer.parseInt(cl.getOptionValue("columns"));
        long ttlInMillis = Long.parseLong(cl.getOptionValue("ttlInMillis"));

        this.numWorkers = Integer.parseInt(cl.getOptionValue("numWorkers"));

        ticketService = new SimpleTicketService(numRows, numColumns, ttlInMillis);
    }

    /**
     * Creates a task to print out statistics about the venue on a periodic basis
     */
    private void initMonitor() {
        Runnable monitor = () -> {
            Venue venue = ticketService.getVenue();
            log.info("Monitor: Current Venue Map");
            log.info(venue);

            log.info("Monitor: Number of open seats: " + ticketService.numSeatsAvailable());
            log.info("Monitor: Number of unreserved blocks: " + venue.findBlocks(SeatBlockType.UNRESERVED).size());
            log.info("Monitor: Number of hold blocks: " + venue.findBlocks(SeatBlockType.HOLD).size());
            log.info("Monitor: Number of reserved blocks: " + venue.findBlocks(SeatBlockType.RESERVED).size());
        };

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ExecutorRegistry.register(executor);

        executor.scheduleWithFixedDelay(monitor, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Schedules workers to periodically attempt to hold and reserve reservations.
     */
    private void initWorkers() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(numWorkers);
        ExecutorRegistry.register(executor);

        for (int i=0; i < numWorkers; i++) {
            Runnable worker = getWorkerTask();
            executor.scheduleWithFixedDelay(worker, 0, 2, TimeUnit.SECONDS);
        }

    }

    /**
     * Creates a worker task which attempts to hold a reservation with a random number of seats, waits a random
     * amount of time and then attempts to confirm the reservation
     * @return Runnable tha can be scheduled with an ExecutorService
     */
    private Runnable getWorkerTask() {
        Supplier<String> emailGenerator = () -> UUID.randomUUID().toString() + "@wl.com";

        Random r = new Random();

        return () -> {
            int numSeats = r.nextInt(5);

            String email = emailGenerator.get();

            SeatHold hold = ticketService.findAndHoldSeats(numSeats, email);

            if (hold == null) {
                log.info("Could not hold " + numSeats + " for user: " + email);
                return;
            }

            log.info("Seat Hold: " + numSeats + " by user: " + email);

            try {
                TimeUnit.SECONDS.sleep(r.nextInt(10));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            String confirmId = ticketService.reserveSeats(hold.getId(), email);

            if (confirmId != null) {
                log.info("Reservation: " + numSeats + " by user: " + email);
            }
            else {
                log.info("Could not reserve seats for user: " + email);
            }

        };
    }


    /**
     * Main execution loop of the simulator
     * @param args command line arguments
     */
    public static void main(String... args) {
        CommandLine cl = parseArgs(args);

        // my version of with statements in python :-)
        ExecutorRegistry.runInRegistryContext(() -> {
            AppSimulator simulator = new AppSimulator(cl);
            simulator.initMonitor();
            simulator.initWorkers();

            int openSeats;

            do {
                openSeats = simulator.ticketService.numSeatsAvailable();

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            while (openSeats > 0);

            log.info("Sold out!");
        });
    }

    private static CommandLine parseArgs(String... args) {
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            log.error("Unable to parse input arguments", e);

            // wrap and throw a Runtime exception - we want the application to fail in this case
            throw new RuntimeException(e);
        }
    }

    private static Options getOptions() {
        Options options = new Options();

        options.addOption(Option.builder().longOpt("rows").hasArg().type(Integer.class).build());
        options.addOption(Option.builder().longOpt("columns").hasArg().type(Integer.class).build());
        options.addOption(Option.builder().longOpt("ttlInMillis").hasArg().type(Long.class).build());
        options.addOption(Option.builder().longOpt("numWorkers").hasArg().type(Integer.class).build());

        return options;
    }


}

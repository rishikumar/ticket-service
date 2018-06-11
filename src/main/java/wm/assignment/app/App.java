package wm.assignment.app;

import wm.assignment.service.SimpleTicketService;
import wm.assignment.service.TicketService;

public class App {

    public static void main(String... args) {
        TicketService ts = new SimpleTicketService(50, 50, 10000);

    }
}

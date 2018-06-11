package wm.assignment.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import wm.assignment.service.SimpleTicketService;
import wm.assignment.service.TicketService;

public class App {
    private final static Log log = LogFactory.getLog(App.class);

    public static void main(String... args) {
        TicketService ts = new SimpleTicketService(50, 50, 10000);

    }
}

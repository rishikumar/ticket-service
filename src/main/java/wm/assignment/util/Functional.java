package wm.assignment.util;

public class Functional {

    public static void wrapWithRuntimeException(Runnable r) {
        try {
            r.run();
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

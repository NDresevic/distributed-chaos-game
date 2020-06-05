package app.lamport_mutex;

public class LamportClock {

    private int d;
    private int clock;

    public LamportClock(int d) {
        this.d = d;
        this.clock = 0;
    }

    public LamportClock() {
        this(1);
    }

    public synchronized void localEvent() {   // local event -- local_event
        clock += d;
    }

    public synchronized void messageEvent(int messageClock) {
        this.localEvent();
        clock = Math.max(clock, messageClock + d);
    }

    public int getClock() { return clock; }
}

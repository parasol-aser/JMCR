package edu.tamu.aser.scheduling.events;

public class ParkUnparkEventDesc extends EventDesc {

    private Thread thread;

    public ParkUnparkEventDesc(EventType eventType, Thread thread) {
        super(eventType);
        if (!(eventType.equals(EventType.PARK) || eventType.equals(EventType.UNPARK))) {
            throw new IllegalArgumentException("Event type has to be park or unpark");
        }
        this.thread = thread;
    }

    public Thread getThread() {
        return thread;
    }

    @Override
    public String toString() {
        return "ParkUnparkEvent [eventType=" + getEventType() + ", thread=" + thread.getId() + "]";
    }
}

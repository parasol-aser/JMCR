package edu.tamu.aser.scheduling.events;

public class ThreadLifeEventDesc extends EventDesc {

    private final Thread childThread;

    public ThreadLifeEventDesc(EventType eventType, Thread childThread) {
        super(eventType);
        if (!(eventType.equals(EventType.FORK) || eventType.equals(EventType.BEGIN) || eventType.equals(EventType.END))) {
            throw new IllegalArgumentException("Event type has to be fork, begin or end");
        }
        this.childThread = childThread;
    }

    public ThreadLifeEventDesc(EventType eventType) {
        this(eventType, null);
    }

    public Thread getChildThread() {
        return childThread;
    }

    @Override
    public String toString() {
        return "ThreadLifeEventDesc [eventType=" + getEventType() + ", childThread=" + childThread + "]";
    }

}

package edu.tamu.aser.scheduling.events;

public class JoinEventDesc extends EventDesc {

    private Thread joinThread;

    public JoinEventDesc(EventType eventType, Thread joinThread) {
        super(eventType);
        if (!(eventType.equals(EventType.JOIN) || eventType.equals(EventType.TIMED_JOIN))) {
            throw new IllegalArgumentException("Event type has to be join or timed join");
        }
        this.joinThread = joinThread;
    }

    public Thread getJoinThread() {
        return joinThread;
    }

    @Override
    public String toString() {
        return "JoinEvent [eventType=" + getEventType() + ", joinThread=" + joinThread + "]";
    }

}

package edu.tamu.aser.scheduling.events;


public class BlockedForIMUnitEventDesc extends EventDesc {

    private String event;

    public BlockedForIMUnitEventDesc(String event) {
        super(EventType.BLOCKED_FOR_IMUNIT_EVENT);
        this.event = event;
    }

    public String getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return "BlockedForThreadBlockDesc [eventType=" + getEventType() + ", event=" + event + "]";
    }

}

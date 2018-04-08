package edu.tamu.aser.scheduling.events;

public class ArrayAccessEventDesc extends EventDesc {

    public ArrayAccessEventDesc(EventType eventType) {
        super(eventType);
        if (!(eventType.equals(EventType.READ) || eventType.equals(EventType.WRITE))) {
            throw new IllegalArgumentException("Event type has to be read or write");
        }
    }

    @Override
    public String toString() {
        return "ArrayAccessEvent [eventType=" + getEventType() + "]";
    }
    
}

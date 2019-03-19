package edu.tamu.aser.tests.scheduling.events;

public abstract class EventDesc {

    private EventType eventType;

    public EventDesc(EventType eventType) {
        super();
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

}

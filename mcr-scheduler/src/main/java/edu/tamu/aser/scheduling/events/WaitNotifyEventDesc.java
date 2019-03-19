package edu.tamu.aser.scheduling.events;

public class WaitNotifyEventDesc extends EventDesc {

    private Object waitNotifyObject;

    public WaitNotifyEventDesc(EventType eventType, Object waitNotifyObject) {
        super(eventType);
        if (!(eventType.equals(EventType.WAIT) || eventType.equals(EventType.TIMED_WAIT) || eventType.equals(EventType.NOTIFY) || eventType
                .equals(EventType.NOTIFY_ALL))) {
            throw new IllegalArgumentException("Event type has to be wait, timed wait, notify or notify all");
        }
        this.waitNotifyObject = waitNotifyObject;
    }

    public Object getWaitObject() {
        return waitNotifyObject;
    }

    @Override
    public String toString() {
        return "WaitEvent [eventType=" + getEventType() + ", waitNotifyObject=" + System.identityHashCode(waitNotifyObject) + "]";
    }

}

package edu.tamu.aser.scheduling.events;

public class LockEventDesc extends EventDesc {

    private Object lockObject;

    public LockEventDesc(EventType eventType, Object lockObject) {
        super(eventType);
        if (!(eventType.equals(EventType.LOCK) || eventType.equals(EventType.UNLOCK))) {
            throw new IllegalArgumentException("Event type has to be lock or unlock");
        }
        this.lockObject = lockObject;
    }

    public Object getLockObject() {
        return lockObject;
    }

    @Override
    public String toString() {
        return "LockEvent [eventType=" + getEventType() + ", lockObject=" + System.identityHashCode(lockObject) + "]";
    }

}

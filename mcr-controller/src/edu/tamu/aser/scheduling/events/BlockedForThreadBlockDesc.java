package edu.tamu.aser.scheduling.events;

public class BlockedForThreadBlockDesc extends EventDesc {

    private Thread thread;

    public BlockedForThreadBlockDesc(Thread thread) {
        super(EventType.BLOCKED_FOR_THREAD_BLOCK);
        this.thread = thread;
    }

    public Thread getThread() {
        return thread;
    }

    @Override
    public String toString() {
        return "BlockedForThreadBlockDesc [eventType=" + getEventType() + ", thread=" + thread + "]";
    }

}

package edu.tamu.aser.tests.scheduling.events;

public enum EventType {

    FORK, BEGIN, END, READ, WRITE, LOCK, UNLOCK, WAIT, TIMED_WAIT, JOIN, SLEEP, TIMED_JOIN, NOTIFY, NOTIFY_ALL, 
        PARK, UNPARK, BLOCKED_FOR_IMUNIT_EVENT, BLOCKED_FOR_THREAD_BLOCK;

}

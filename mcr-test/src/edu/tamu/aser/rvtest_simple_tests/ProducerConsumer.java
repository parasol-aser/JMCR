package edu.tamu.aser.rvtest_simple_tests;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexecution.JUnit4MCRRunner;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

@RunWith(JUnit4MCRRunner.class)
public class ProducerConsumer {

    public static void main(String[] args) throws InterruptedException {
        new ProducerConsumer().test();
    }

    @Test
    public void test() throws InterruptedException {
        Drop drop = new Drop();
        Producer producer = new Producer(drop, "Mares eat oats, Does eat oats, Little lambs eat ivy, A kid will eat ivy too");
        Thread producerThread = new Thread(producer);
        Consumer consumer = new Consumer(drop);
        Thread consumerThread = new Thread(consumer);
        producerThread.start();
        consumerThread.start();
        producerThread.join();
        consumerThread.join();
        Assert.assertEquals("Mares eat oats Does eat oats Little lambs eat ivy A kid will eat ivy too", consumer.getFullMessage());
    }

}

class Consumer implements Runnable {
    private Drop drop;

    private String fullMessage;

    public Consumer(Drop drop) {
        this.drop = drop;
        this.fullMessage = "";
    }

    public void run() {
        Random random = new Random();
        for (String message = drop.take(); !message.equals("DONE"); message = drop.take()) {
            System.out.format("MESSAGE RECEIVED: %s%n", message);
            fullMessage += message;
            try {
                Thread.sleep(random.nextInt(500));
            } catch (InterruptedException e) {
            }
        }
    }

    public String getFullMessage() {
        return fullMessage;
    }
}

class Producer implements Runnable {
    private Drop drop;
    private final String fullMessage;

    public Producer(Drop drop, String fullMessage) {
        this.drop = drop;
        this.fullMessage = fullMessage;
    }

    public void run() {
        String subMessages[] = fullMessage.split("\\,");
        Random random = new Random();
        for (int i = 0; i < subMessages.length; i++) {
            drop.put(subMessages[i]);
            try {
                Thread.sleep(random.nextInt(500));
            } catch (InterruptedException e) {
            }
        }
        drop.put("DONE");
    }
}

class Drop {
    // Message sent from producer to consumer.
    private String message;
    // True if consumer should wait for producer to send message, false
    // if producer should wait for consumer to retrieve message.
    private boolean empty = true;

    public synchronized String take() {
        // Wait until message is available.
        while (empty) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        // Toggle status.
        empty = true;
        // Notify producer that status has changed.
        notifyAll();
        return message;
    }

    public synchronized void put(String message) {
        // Wait until message has been retrieved.
        while (!empty) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        // Toggle status.
        empty = false;
        // Store message.
        this.message = message;
        // Notify consumer that status has changed.
        notifyAll();
    }
}
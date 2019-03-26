package edu.tamu.aser.tests.account;


public class Failable {

    public Failable() {
        super();
    }

    protected void fail(String reason) {
        throw new RuntimeException(reason);
    }

}
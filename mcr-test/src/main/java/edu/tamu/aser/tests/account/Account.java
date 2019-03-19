package edu.tamu.aser.tests.account;

public class Account {
    double amount;
    String name;

    public Account(String nm, double amnt) {
        amount = amnt;
        name = nm;
    }

    // functions
    //synchronized 
    void depsite(double money) {
        amount += money;
    }

     synchronized 
     void withdraw(double money) {
        amount -= money;
    }
     
synchronized    
void transfer(Account ac, double mn) {
        amount -= mn;
        synchronized (ac) 
        {
        	ac.amount += mn; // now yes. no acquire for the other lock!!
		}
            
    }

    public void print() {
        // TODO Auto-generated method stub
    }

}
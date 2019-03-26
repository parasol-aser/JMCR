package edu.tamu.aser.tests.misc;

import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Alan on 6/20/18.
 */

@RunWith(JUnit4MCRRunner.class)
public class HelloWorld {

    public static int x=0;
    public static void main(String []args){
        x = 1;
        System.out.println("Hello World");
    }

    @Test
    public void test(){
        HelloWorld.main(null);
    }
}

package external.jpf_test_cases;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: ksen
 * Date: May 30, 2007
 * Time: 8:16:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyRandom {
    public static Random rand = new Random(1);

    public static boolean nextRandom(){
        return rand.nextBoolean();
    }

    public static int nextInt(int max){
        return rand.nextInt(max);
    }
}

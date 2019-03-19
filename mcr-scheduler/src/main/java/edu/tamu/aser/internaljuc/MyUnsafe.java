package edu.tamu.aser.internaljuc;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class MyUnsafe {

    @SuppressWarnings("restriction")
    public
    static Unsafe getUnsafe() {
        try {

            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            return (Unsafe) singleoneInstanceField.get(null);
        } catch (Exception e) {
            return null;
        }
    }

}

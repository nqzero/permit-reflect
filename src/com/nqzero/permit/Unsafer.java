// copyright 2021 nqzero - offered under the terms of the MIT License

package com.nqzero.permit;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class Unsafer {

    public static final Unsafe uu;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            uu = (Unsafe) field.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}

package com.nqzero.unflect;

public class UnsafeWrapper {
    public static final sun.misc.Unsafe uu =
            (sun.misc.Unsafe) Unflect.getField(sun.misc.Unsafe.class,"theUnsafe");
    
}

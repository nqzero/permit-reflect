package com.nqzero.permit;


import static com.nqzero.permit.Unsafer.uu;




public abstract class Safer<TT,VV> {


    public interface Meth<VV> {
        public VV meth(Object obj,long offset);
    }


    
    protected abstract long offset();
    protected abstract Object resolve(TT o);
    
    public int getInt(TT o) {
        return uu.getInt(resolve(o),offset());
    }

    public void putInt(TT o,int x) {
        uu.putInt(resolve(o),offset(),x);
    }

    public VV getObject(TT o) {
        return (VV) uu.getObject(resolve(o),offset());
    }

    public void putObject(TT o,VV x) {
        uu.putObject(resolve(o),offset(),x);
    }

    public boolean getBoolean(TT o) {
        return uu.getBoolean(resolve(o),offset());
    }

    public void putBoolean(TT o,boolean x) {
        uu.putBoolean(resolve(o),offset(),x);
    }

    public byte getByte(TT o) {
        return uu.getByte(resolve(o),offset());
    }

    public void putByte(TT o,byte x) {
        uu.putByte(resolve(o),offset(),x);
    }

    public short getShort(TT o) {
        return uu.getShort(resolve(o),offset());
    }

    public void putShort(TT o,short x) {
        uu.putShort(resolve(o),offset(),x);
    }

    public char getChar(TT o) {
        return uu.getChar(resolve(o),offset());
    }

    public void putChar(TT o,char x) {
        uu.putChar(resolve(o),offset(),x);
    }

    public long getLong(TT o) {
        return uu.getLong(resolve(o),offset());
    }

    public void putLong(TT o,long x) {
        uu.putLong(resolve(o),offset(),x);
    }

    public float getFloat(TT o) {
        return uu.getFloat(resolve(o),offset());
    }

    public void putFloat(TT o,float x) {
        uu.putFloat(resolve(o),offset(),x);
    }

    public double getDouble(TT o) {
        return uu.getDouble(resolve(o),offset());
    }

    public void putDouble(TT o,double x) {
        uu.putDouble(resolve(o),offset(),x);
    }

    public void setMemory(TT o,long bytes,byte value) {
        uu.setMemory(resolve(o),offset(),bytes,value);
    }

    public void copyMemory(TT srcBase,long srcOffset,Object destBase,long destOffset,long bytes) {
        uu.copyMemory(resolve(srcBase),srcOffset,destBase,destOffset,bytes);
    }

    public final boolean compareAndSwapObject(TT o,VV expected,VV x) {
        return uu.compareAndSwapObject(resolve(o),offset(),expected,x);
    }

    public final boolean compareAndSwapInt(TT o,int expected,int x) {
        return uu.compareAndSwapInt(resolve(o),offset(),expected,x);
    }

    public final boolean compareAndSwapLong(TT o,long expected,long x) {
        return uu.compareAndSwapLong(resolve(o),offset(),expected,x);
    }

    public VV getObjectVolatile(TT o) {
        return (VV) uu.getObjectVolatile(resolve(o),offset());
    }

    public void putObjectVolatile(TT o,VV x) {
        uu.putObjectVolatile(resolve(o),offset(),x);
    }

    public int getIntVolatile(TT o) {
        return uu.getIntVolatile(resolve(o),offset());
    }

    public void putIntVolatile(TT o,int x) {
        uu.putIntVolatile(resolve(o),offset(),x);
    }

    public boolean getBooleanVolatile(TT o) {
        return uu.getBooleanVolatile(resolve(o),offset());
    }

    public void putBooleanVolatile(TT o,boolean x) {
        uu.putBooleanVolatile(resolve(o),offset(),x);
    }

    public byte getByteVolatile(TT o) {
        return uu.getByteVolatile(resolve(o),offset());
    }

    public void putByteVolatile(TT o,byte x) {
        uu.putByteVolatile(resolve(o),offset(),x);
    }

    public short getShortVolatile(TT o) {
        return uu.getShortVolatile(resolve(o),offset());
    }

    public void putShortVolatile(TT o,short x) {
        uu.putShortVolatile(resolve(o),offset(),x);
    }

    public char getCharVolatile(TT o) {
        return uu.getCharVolatile(resolve(o),offset());
    }

    public void putCharVolatile(TT o,char x) {
        uu.putCharVolatile(resolve(o),offset(),x);
    }

    public long getLongVolatile(TT o) {
        return uu.getLongVolatile(resolve(o),offset());
    }

    public void putLongVolatile(TT o,long x) {
        uu.putLongVolatile(resolve(o),offset(),x);
    }

    public float getFloatVolatile(TT o) {
        return uu.getFloatVolatile(resolve(o),offset());
    }

    public void putFloatVolatile(TT o,float x) {
        uu.putFloatVolatile(resolve(o),offset(),x);
    }

    public double getDoubleVolatile(TT o) {
        return uu.getDoubleVolatile(resolve(o),offset());
    }

    public void putDoubleVolatile(TT o,double x) {
        uu.putDoubleVolatile(resolve(o),offset(),x);
    }

    public void putOrderedObject(TT o,VV x) {
        uu.putOrderedObject(resolve(o),offset(),x);
    }

    public void putOrderedInt(TT o,int x) {
        uu.putOrderedInt(resolve(o),offset(),x);
    }

    public void putOrderedLong(TT o,long x) {
        uu.putOrderedLong(resolve(o),offset(),x);
    }

    public final int getAndAddInt(TT o,int delta) {
        return uu.getAndAddInt(resolve(o),offset(),delta);
    }

    public final long getAndAddLong(TT o,long delta) {
        return uu.getAndAddLong(resolve(o),offset(),delta);
    }

    public final int getAndSetInt(TT o,int newValue) {
        return uu.getAndSetInt(resolve(o),offset(),newValue);
    }

    public final long getAndSetLong(TT o,long newValue) {
        return uu.getAndSetLong(resolve(o),offset(),newValue);
    }

    public final VV getAndSetObject(TT o,VV newValue) {
        return (VV) uu.getAndSetObject(resolve(o),offset(),newValue);
    }
    
}

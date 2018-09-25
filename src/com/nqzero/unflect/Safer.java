package com.nqzero.unflect;

import static com.nqzero.unflect.Unflect.*;
import static com.nqzero.unflect.UnsafeWrapper.uu;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Safer<TT,VV> extends SaferUnsafe<TT,VV> {

    String name;
    Class klass;
    Field field;
    boolean isStatic;
    boolean isArray;
    long offset;
    long scale;
        Safer chain;
    Safer last = this, first = null;
    Object base;
    int rowPosition;
    boolean isKnown = true;

    protected Safer(Class<TT> klass,String name) throws FieldNotFound {
        this.name = name;
        this.klass = klass;
        isArray = klass.isArray();
        
        if (isArray) {
            offset = uu.arrayBaseOffset(klass);
            scale = uu.arrayIndexScale(klass);
        }
        else {
            Exception ex = null;
            field = getSuperField(klass,name);
            if (field==null)
                throw new FieldNotFound(name,ex);
            isStatic = Modifier.isStatic(field.getModifiers());
            if (isStatic) {
                base = uu.staticFieldBase(field);
                offset = uu.staticFieldOffset(field);
            }
            else
                offset = uu.objectFieldOffset(field);
        }
    }
    public Safer<TT,VV> chain(Class klass,String name) throws FieldNotFound, IncompatibleClasses {
        Class nominal = last.klass();
        if (!nominal.isAssignableFrom(klass) & !klass.isAssignableFrom(nominal))
            throw new IncompatibleClasses(klass,nominal);
        return chain(klass,name,false);
    }
    public Safer chain(String name) throws FieldNotFound {
        return chain(last.klass(),name,true);
    }
    protected Safer<TT,VV> chain(Class klass,String name,boolean known) throws FieldNotFound {
        Safer ref = new Safer(klass,name);
        ref.isKnown = known;
        ref.rowPosition = last.rowPosition + (last.isArray ? 1:0);
        if (ref.isStatic)
            first = ref;
        last.chain = ref;
        last = ref;
        return this;
    }
    public <XX> Safer<TT,XX> target(Class<XX> klass) {
        return (Safer<TT,XX>) this;
    }
    protected Class klass() {
        if (isArray) return klass.getComponentType();
        else return field.getType();
    }

    public class Linked extends SaferUnsafe<TT,VV> {
        int [] rows;
        protected long offset() {
            return Safer.this.last.addy(rows);
        }
        protected Object resolve(Object o) {
            return Safer.this.resolve(o,rows);
        }
        public Linked(int ... rows) {
            this.rows = rows;
        }
    }
    public Linked link(int ... rows) {
        return new Linked(rows);
    }
    
    protected Object resolve(Object o) {
        return resolve(o,new int[0]);
    }    
    protected Object resolve(Object o,int [] rows) {
        if (first != null)
            return first.resolve(o);
        if (isStatic)
            o = base;
        for (Safer ref=this; ref.chain != null; ref=ref.chain) {
            assert(ref.isKnown | ref.klass.isInstance(o));
            o = uu.getObject(o,ref.addy(rows));
        }
        return o;
    }
    
    protected long addy(int [] rows) {
        return isArray
                ? offset+scale*rows[rowPosition]
                : offset;
    }
    
    protected long offset() { return last.offset; }

    
    
    }

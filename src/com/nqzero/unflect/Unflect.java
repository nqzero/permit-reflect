package com.nqzero.unflect;

import com.nqzero.unflect.Safer.Meth;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import static com.nqzero.unflect.Unsafer.uu;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Unflect<TT,VV> extends Safer<TT,VV> {
    public static final String splitChar = "\\.";

    static Unflect<AccessibleObject,Boolean> override = build(AccessibleObject.class,"override");

    
    public static void setAccessible(AccessibleObject accessor) {
        override.putBoolean(accessor,true);
    }

    public static void unLog() {
        try {
            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            build(cls,"logger").putObjectVolatile(null,null);
        }
        catch (ClassNotFoundException ex) {}
    }


    static boolean dbg = false;

    public static void godMode() {
        try {
            // this will fail on java 8 and lower
            // but load with at least java 9-11
            Class base = Unflect.class;
            Class klass = base.getClassLoader().loadClass(base.getPackageName() + ".Support9");
            Method method = klass.getMethod("godMode");
            method.invoke(null);
        }
        catch (Throwable ex) {}
            
    }

    
    public static Object getObject(Object cl,String name) {
        try {
            Field field = getSuperField(cl.getClass(),name);
            long offset = uu.objectFieldOffset(field);
            return uu.getObject(cl,offset);
        }
        catch (Exception ex) {}
        return null;
    }
    public static <VV> VV getField(Object cl,Meth<VV> meth,String ... names) throws FieldNotFound {
        if (names.length==1)
            names = names[0].split(splitChar);
        try {
            int num = names.length-1;
            for (int ii = 0; ii <= num; ii++) {
                Field field = getSuperField(cl.getClass(),names[ii]);
                long offset = uu.objectFieldOffset(field);
                if (ii < num)
                    cl = uu.getObject(cl,offset);
                else
                    return meth.meth(cl,offset);
            }
        }
        catch (Throwable ex) {
            throw new FieldNotFound(String.join(splitChar,names),ex);
        }
        return null;
    }

    public static class FieldNotFound extends RuntimeException {
        String name;
        public FieldNotFound(String name,Throwable ex) {
            super("field \"" + name + "\" not found",ex);
            this.name = name;
        }
    }

    public static class IncompatibleClasses extends RuntimeException {
        Class klass, nominal;
        public IncompatibleClasses(Class klass,Class nominal) {
            super(klass + " and " + nominal + " aren't assignable");
            this.klass = klass;
            this.nominal = nominal;
        }
    }

    public static <TT,VV> Unflect<TT,VV> build(Class<TT> klass,String name) throws FieldNotFound {
        String [] names = name.split(splitChar);
        String firstName = names.length==0 ? name : names[0];
        Unflect<TT,VV> ref = new Unflect(klass,firstName);
        for (int ii=1; ii < names.length; ii++)
            ref.chain(names[ii]);
        return ref;
    }

    public static <TT,VV> Unflect<TT,VV> build(TT sample,String name) throws FieldNotFound {
        return build((Class<TT>) sample.getClass(),name);
    }
    
    public static Field getSuperField(Class klass,String name) {
        for (; klass != Object.class; klass = klass.getSuperclass()) {
            try {
                return klass.getDeclaredField(name);
            }
            catch (NoSuchFieldException ex) {}
            catch (SecurityException ex) {}
        }
        return null;
    }
    
    private static String[] processArgs(String[] args) {
        String[] ret = new String[args.length-1];
        if (ret.length > 0) 
            System.arraycopy(args, 1, ret, 0, ret.length);
        return ret;
    }

    /**
     * invoke the main method in a named class in god mode,
     * ie with all packages in all modules open to all modules
     * @param args the first element is the name of the class to invoke
     *        and the remaining elements are passed as arguments
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("usage: java Unflect className [args ...]");
            System.out.println("  invoke the main method in the named class in god mode with the remaining args");
            System.out.println("  ie, run it with all packages in all modules open to all modules");
            System.out.println("");
            System.exit(1);
        }
        godMode();
        String className = args[0];
        args = processArgs(args);
        Class mainClass = Unflect.class.getClassLoader().loadClass(className);
        Method mainMethod = mainClass.getMethod("main", new Class[]{String[].class});
        mainMethod.invoke(null,new Object[] {args});
        
    }
    

    String name;
    Class klass;
    Field field;
    boolean isStatic;
    boolean isArray;
    long offset;
    long scale;
    Unflect chain;
    Unflect last = this;
    Unflect first;
    Object base;
    int rowPosition;
    boolean isKnown = true;

    protected Unflect(Class<TT> klass,String name) throws FieldNotFound {
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
    public Unflect<TT,VV> chain(Class klass,String name) throws FieldNotFound, IncompatibleClasses {
        Class nominal = last.klass();
        if (!nominal.isAssignableFrom(klass) & !klass.isAssignableFrom(nominal))
            throw new IncompatibleClasses(klass,nominal);
        return chain(klass,name,false);
    }
    public Unflect chain(String name) throws FieldNotFound {
        return chain(last.klass(),name,true);
    }
    protected Unflect<TT,VV> chain(Class klass,String name,boolean known) throws FieldNotFound {
        Unflect ref = new Unflect(klass,name);
        ref.isKnown = known;
        ref.rowPosition = last.rowPosition + (last.isArray ? 1:0);
        if (ref.isStatic)
            first = ref;
        last.chain = ref;
        last = ref;
        return this;
    }
    public <XX> Unflect<TT,XX> target(Class<XX> klass) {
        return (Unflect<TT,XX>) this;
    }
    protected Class klass() {
        if (isArray) return klass.getComponentType();
        else return field.getType();
    }

    public class Linked extends Safer<TT,VV> {
        int [] rows;
        protected long offset() {
            return Unflect.this.last.addy(rows);
        }
        protected Object resolve(Object o) {
            return Unflect.this.resolve(o,rows);
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
        for (Unflect ref=this; ref.chain != null; ref=ref.chain) {
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

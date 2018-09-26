package com.nqzero.permit;

import com.nqzero.permit.Safer.Meth;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import static com.nqzero.permit.Unsafer.uu;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Permit<TT,VV> extends Safer<TT,VV> {
    public static final String splitChar = "\\.";

    static Exception savedEx;
    static Permit<AccessibleObject,Boolean> override;
    static {
        try { override = build(AccessibleObject.class,"override"); }
        catch (Exception ex) { savedEx = ex; }
    }

    public static void setAccessible(AccessibleObject accessor) throws InitializationFailed {
        if (savedEx != null)
            throw new InitializationFailed();
        override.putBoolean(accessor,true);
    }
    
    public static boolean initSucceeded(boolean rethrow) {
        if (savedEx==null)
            return true;
        if (rethrow)
            throw new RuntimeException(savedEx);
        return false;
    }

    public static void unLog() {
        try {
            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            build(cls,"logger").putObjectVolatile(null,null);
        }
        catch (ClassNotFoundException ex) {}
    }


    static String jigsaw = "JigsawImpl";
    public static void godMode() throws RuntimeException {
        if (savedEx != null)
            throw new InitializationFailed();
        try {
            // this will fail on java 8 and lower
            // but load with at least java 9-11
            Class base = Permit.class;
            Class klass = base.getClassLoader().loadClass(base.getPackageName() + "." + jigsaw);
            Method method = klass.getMethod("godMode");
            method.invoke(null);
        }
        catch (NoSuchMethodError ex) { /* expected for java 8 or older */ }
        catch (Throwable ex) {
            throw new RuntimeException("jigsaw appears active, but unable to open packages",ex);
        }
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

    public static class InitializationFailed extends RuntimeException {
        public InitializationFailed() {
            super("initialization failed, perhaps you're running with a security manager",savedEx);
        }
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

    public static <TT,VV> Permit<TT,VV> build(Class<TT> klass,String name) throws FieldNotFound {
        String [] names = name.split(splitChar);
        String firstName = names.length==0 ? name : names[0];
        Permit<TT,VV> ref = new Permit(klass,firstName);
        for (int ii=1; ii < names.length; ii++)
            ref.chain(names[ii]);
        return ref;
    }

    public static <TT,VV> Permit<TT,VV> build(TT sample,String name) throws FieldNotFound {
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
    
    private static String[] processArgs(String[] args,int start) {
        String[] ret = new String[args.length-start];
        if (ret.length > 0) 
            System.arraycopy(args, start, ret, 0, ret.length);
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
        int start = 0;
        boolean verbose = false;
        if (args.length > 0 && args[0].equals("-v")) {
            start++;
            verbose = true;
        }

        String name = Permit.class.getName();
        if (args.length == start) {
            System.out.format("\nusage: java %s [-v] className [args ...]\n",name);
            System.out.println("  invoke the main method in the named class in god mode with the remaining args");
            System.out.println("  ie, run it with all packages in all modules open to all modules");
            System.out.println("where options are:");
            System.out.println("  -v  : verbose, rethrow exceptions when setting god mode");
            System.out.println("  args: the remaining args are passed to the named class main method");
            System.out.println("");
            System.exit(1);
        }

        Exception problem = savedEx;
        try {
            if (problem==null)
                godMode();
        }
        catch (Exception ex) { problem = ex; }
        if (problem != null) {
            System.out.println("unable to initialize godMode - cowardly exiting");
            System.out.println("  perhaps you have a security manager running");
            if (verbose) {
                System.out.format("  rethrowing exception:\n\n");
                throw problem;
            }
            System.exit(1);
        }
        String className = args[start];
        args = processArgs(args,start+1);
        Class mainClass = Permit.class.getClassLoader().loadClass(className);
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
    Permit chain;
    Permit last = this;
    Permit first;
    Object base;
    int rowPosition;
    boolean isKnown = true;

    protected Permit(Class<TT> klass,String name) throws FieldNotFound {
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
    public Permit<TT,VV> chain(Class klass,String name) throws FieldNotFound, IncompatibleClasses {
        Class nominal = last.klass();
        if (!nominal.isAssignableFrom(klass) & !klass.isAssignableFrom(nominal))
            throw new IncompatibleClasses(klass,nominal);
        return chain(klass,name,false);
    }
    public Permit chain(String name) throws FieldNotFound {
        return chain(last.klass(),name,true);
    }
    protected Permit<TT,VV> chain(Class klass,String name,boolean known) throws FieldNotFound {
        Permit ref = new Permit(klass,name);
        ref.isKnown = known;
        ref.rowPosition = last.rowPosition + (last.isArray ? 1:0);
        if (ref.isStatic)
            first = ref;
        last.chain = ref;
        last = ref;
        return this;
    }
    public <XX> Permit<TT,XX> target(Class<XX> klass) {
        return (Permit<TT,XX>) this;
    }
    protected Class klass() {
        if (isArray) return klass.getComponentType();
        else return field.getType();
    }

    public class Linked extends Safer<TT,VV> {
        int [] rows;
        protected long offset() {
            return Permit.this.last.addy(rows);
        }
        protected Object resolve(Object o) {
            return Permit.this.resolve(o,rows);
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
        for (Permit ref=this; ref.chain != null; ref=ref.chain) {
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

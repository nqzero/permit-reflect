package com.nqzero.unflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import static com.nqzero.unflect.UnsafeWrapper.uu;
import java.lang.reflect.Method;

public class Unflect {
    public static final String splitChar = "\\.";
    static Safer<AccessibleObject,Boolean> override = build(AccessibleObject.class,"override");
    static void makeAccessible(AccessibleObject accessor) {
        override.putBoolean(accessor,true);
    }
    static void unLog() {
        try {
            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            build(cls,"logger").putObjectVolatile(null,null);
        }
        catch (ClassNotFoundException ex) {}
    }


    static boolean dbg = false;

    public static void godMode() {
        try {
            Class base = Unflect.class;
            Class klass = base.getClassLoader().loadClass(base.getPackageName() + ".Support11");
            Method method = klass.getMethod("godMode");
            method.invoke(null);
        }
        catch (Throwable ex) {}
            
    }

    
    public static Object getObject(Object cl,String name) {
        try {
            Field field = cl.getClass().getDeclaredField(name);
            long offset = uu.objectFieldOffset(field);
            return uu.getObject(cl,offset);
        }
        catch (Exception ex) {}
        return null;
    }
    public interface Meth<VV> {
        public VV meth(Object obj,long offset);
    }
    public static <VV> VV getField(Object cl,Meth<VV> meth,String ... names) {
        if (names.length==1)
            names = names[0].split(splitChar);
        try {
            int num = names.length-1;
            for (int ii = 0; ii <= num; ii++) {
                Field field = cl.getClass().getDeclaredField(names[ii]);
                long offset = uu.objectFieldOffset(field);
                if (ii < num)
                    cl = uu.getObject(cl,offset);
                else
                    return meth.meth(cl,offset);
            }
        }
        catch (Exception ex) {}
        return null;
    }

    public static <TT,VV> Safer<TT,VV> build(Class<TT> klass,String name) {
        String [] names = name.split(splitChar);
        String firstName = names.length==0 ? name : names[0];
        Safer<TT,VV> ref = new Safer(klass,firstName);
        for (int ii=1; ii < names.length; ii++)
            ref.chain(names[ii]);
        return ref;
    }

    public static <TT,VV> Safer<TT,VV> build(TT sample,String name) {
        return build((Class<TT>) sample.getClass(),name);
    }
    
    static Field getSuperField(Class klass,String name) {
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
    
    
}

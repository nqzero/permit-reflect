package com.nqzero.unflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import static com.nqzero.unflect.UnsafeWrapper.uu;

public class Unflect {
    public static final String splitChar = "\\.";
    static Unreflect.Unreflect2<AccessibleObject,Boolean> override = build(AccessibleObject.class,"override");
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
            Method export = Module.class.getDeclaredMethod("implAddOpens",String.class);
            makeAccessible(export);
            HashSet<Module> modules = new HashSet();
            Module base = Unreflect.class.getModule();
            if (base.getLayer() != null)
                modules.addAll(base.getLayer().modules());
            modules.addAll(ModuleLayer.boot().modules());
            for (ClassLoader cl = Unreflect.class.getClassLoader(); cl != null; cl = cl.getParent()) {
                modules.add(cl.getUnnamedModule());
            }
            for (Module module : modules) {
                if (dbg) System.out.println("mod: " + module);
                for (String name : module.getPackages()) {
                    if (dbg) System.out.println("   " + name);
                    try {
                        export.invoke(module,name);
                    }
                    catch (Exception ex) {
                        if (dbg) System.out.println("ex: " + ex);
                    }
                }
            }
        }
        catch (NoSuchMethodException ex) {}
        catch (SecurityException ex) {}
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

    public static <TT,VV> Unreflect.Unreflect2<TT,VV> build(Class<TT> klass,String name) {
        String [] names = name.split(splitChar);
        String firstName = names.length==0 ? name : names[0];
        Unreflect.Unreflect2<TT,VV> ref = new Unreflect.Unreflect2(klass,firstName);
        for (int ii=1; ii < names.length; ii++)
            ref.chain(names[ii]);
        return ref;
    }

    public static <TT,VV> Unreflect.Unreflect2<TT,VV> build(TT sample,String name) {
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
    
    public static Object getField(Class klass,String name) {
        try {
            Field f = klass.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(null);
        }
        catch (Exception e) { return null; }
    }
    
    
}

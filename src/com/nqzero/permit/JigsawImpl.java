package com.nqzero.permit;

import java.lang.reflect.Method;
import java.util.HashSet;

public class JigsawImpl {
    static boolean dbg = false;

    public static void godMode() {
        try {
            Method export = Module.class.getDeclaredMethod("implAddOpens",String.class);
            Permit.setAccessible(export);
            HashSet<Module> modules = new HashSet();
            Module base = Safer.class.getModule();
            if (base.getLayer() != null)
                modules.addAll(base.getLayer().modules());
            modules.addAll(ModuleLayer.boot().modules());
            for (ClassLoader cl = Safer.class.getClassLoader(); cl != null; cl = cl.getParent()) {
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
    
}

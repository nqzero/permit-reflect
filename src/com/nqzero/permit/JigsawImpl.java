// copyright 2018 nqzero - offered under the terms of the MIT License

package com.nqzero.permit;

import java.lang.reflect.Method;
import java.util.HashSet;

public class JigsawImpl {
    static boolean dbg = false;

    public static void godMode() {
        godMode(Safer.class);
    }
    public static void godMode(Class first) {
        try {
            Method export = Module.class.getDeclaredMethod("implAddOpens",String.class);
            Permit.setAccessible(export);
            HashSet<Module> modules = new HashSet();
            Module base = first.getModule();
            if (base.getLayer() != null)
                modules.addAll(base.getLayer().modules());
            modules.addAll(ModuleLayer.boot().modules());
            for (ClassLoader cl = first.getClassLoader(); cl != null; cl = cl.getParent()) {
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

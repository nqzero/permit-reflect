package com.nqzero.unflect;

import static com.nqzero.unflect.Unflect.dbg;
import java.lang.reflect.Method;
import java.util.HashSet;
import static com.nqzero.unflect.Unflect.setAccessible;

public class Support11 {
    public static void godMode() {
        try {
            Method export = Module.class.getDeclaredMethod("implAddOpens",String.class);
            setAccessible(export);
            HashSet<Module> modules = new HashSet();
            Module base = SaferUnsafe.class.getModule();
            if (base.getLayer() != null)
                modules.addAll(base.getLayer().modules());
            modules.addAll(ModuleLayer.boot().modules());
            for (ClassLoader cl = SaferUnsafe.class.getClassLoader(); cl != null; cl = cl.getParent()) {
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

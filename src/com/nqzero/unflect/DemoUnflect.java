package com.nqzero.unflect;

import static com.nqzero.unflect.Unflect.getField;
import static com.nqzero.unflect.Unflect.makeAccessible;
import static com.nqzero.unflect.Unflect.unLog;
import static com.nqzero.unflect.Unflect.build;
import static com.nqzero.unflect.SaferUnsafe.logger;
import java.io.FileDescriptor;
import java.io.RandomAccessFile;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import static com.nqzero.unflect.UnsafeWrapper.uu;

public class DemoUnflect {
    public static void main(String[] args) throws Exception {
        int [] vals = new int[10];
        int ii = 0;
        RandomAccessFile raf = new RandomAccessFile("/etc/hosts","r");
        FileDescriptor fd = raf.getFD();
        Field field = FileDescriptor.class.getDeclaredField("fd");
        Class ka = AccessibleObject.class;
        Method export = Module.class.getDeclaredMethod("implAddOpens",String.class);
        makeAccessible(export);
        Class log = Class.forName("jdk.internal.module.IllegalAccessLogger");
        export.invoke(log.getModule(),"jdk.internal.module");
        System.out.println("logger: " + logger(true));
        unLog();
        try {
            field.setAccessible(true);
            vals[ii++] = field.getInt(fd);
        }
        catch (Throwable ex) {
            vals[ii++] = -1;
        }
        makeAccessible(field);
        vals[ii++] = field.getInt(fd);
        
        vals[ii++] = getField(fd,uu::getInt,"fd");
        vals[ii++] = getField(raf,uu::getInt,"fd","fd");
        vals[ii++] = getField(raf,uu::getInt,"fd.fd");

        Safer<FileDescriptor,?> ref = build(FileDescriptor.class,"fd");
        Safer<RandomAccessFile,?> ref2 = build(RandomAccessFile.class,"fd").chain("fd");
        Safer<RandomAccessFile,?> ref3 = build(RandomAccessFile.class,"fd.fd");
        Safer tmp = build(RandomAccessFile.class,"O_TEMPORARY");
        
        
        vals[ii++] = ref.getInt(fd);
        vals[ii++] = ref2.getInt(raf);
        vals[ii++] = ref3.getInt(raf);
        vals[ii++] = tmp.getInt(null); // 16
        for (int jj=0; jj < ii; jj++)
            System.out.format("ufd %2d: %4d\n",jj,vals[jj]);

        ClassLoader cl = SaferUnsafe.class.getClassLoader();
        
        Safer<ClassLoader,String> app = build(cl,"ucp")
                .chain("path")
                .chain(java.util.ArrayList.class,"elementData")
                .chain("")
                .chain(URL.class,"path")
                .target(String.class);
        String path = app.link(0).getObject(cl);
        System.out.println("path: " + path);
        Unflect.godMode();



        jdk.internal.jshell.tool.JShellToolBuilder obj = new jdk.internal.jshell.tool.JShellToolBuilder();
        jdk.internal.jshell.tool.JShellTool tool = obj.rawTool();
        if (args.length > 0)
            tool.start(args);

        System.out.println("tool: " + obj);
    }
    
}

package com.nqzero.unflect;

import static com.nqzero.unflect.Unflect.getField;
import static com.nqzero.unflect.Unflect.makeAccessible;
import static com.nqzero.unflect.Unflect.unLog;
import static com.nqzero.unflect.Unflect.build;
import java.io.FileDescriptor;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.net.URL;
import static com.nqzero.unflect.UnsafeWrapper.uu;

// duplicate the full demo since it uses java 11 classes for some of the tests (which are removed here)
public class Demo8 {
    public static void main(String[] args) throws Exception {
        int [] vals = new int[10];
        int ii = 0;
        RandomAccessFile raf = new RandomAccessFile("/etc/hosts","r");
        FileDescriptor fd = raf.getFD();
        Field field = FileDescriptor.class.getDeclaredField("fd");
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
        
        
        vals[ii++] = ref.getInt(fd);
        vals[ii++] = ref2.getInt(raf);
        vals[ii++] = ref3.getInt(raf);
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



    }
    
}

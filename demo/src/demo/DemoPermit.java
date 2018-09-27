// copyright 2018 nqzero - offered under the terms of the MIT License

package demo;

import com.nqzero.permit.Permit;
import com.nqzero.permit.Safer;
import com.nqzero.permit.Safer.Meth;
import static com.nqzero.permit.Permit.getField;
import static com.nqzero.permit.Permit.unLog;
import static com.nqzero.permit.Permit.build;
import java.io.FileDescriptor;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import static com.nqzero.permit.Unsafer.uu;
import static com.nqzero.permit.Permit.setAccessible;

public class DemoPermit {
    static Object logger(boolean expected) {
        Object obj = null;
        try {
            obj = jdk.internal.module.IllegalAccessLogger.illegalAccessLogger();
        }
        catch (Throwable ex) {}
        if (expected & obj==null)
            throw new RuntimeException("mismatch: " + obj);
        return obj;
    }
    
    void safe() {}
    
    public static void main(String[] args) throws Exception {
        int [] vals = new int[10];
        int ii = 0;

        // check whether Permit initialized first, since this is what a real user might see
        //   with the security manager, local is null but that's ok since Permit bombs out before seeing it
        Method local = null;
        try { local = DemoPermit.class.getDeclaredMethod("safe"); }
        catch (Throwable ex) {}
        setAccessible(local);
        
        RandomAccessFile raf = new RandomAccessFile("/etc/hosts","r");
        FileDescriptor fd = raf.getFD();
        Field field = FileDescriptor.class.getDeclaredField("fd");
        Method export = Module.class.getDeclaredMethod("implAddOpens",String.class);
        setAccessible(export);
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
        setAccessible(field);
        vals[ii++] = field.getInt(fd);


        // for java 8 and later, use uu::getInt instead of meth
        // but want this to compile with java 6
        
        Meth<Integer> meth = new Meth() {
            public Object meth(Object arg0,long arg1) {
                return uu.getInt(arg0,arg1);
            }
        };
        
        vals[ii++] = getField(fd,meth,"fd");
        vals[ii++] = getField(raf,meth,"fd","fd");
        vals[ii++] = getField(raf,meth,"fd.fd");

        Permit<FileDescriptor,?> ref = build(FileDescriptor.class,"fd");
        Permit<RandomAccessFile,?> ref2 = build(RandomAccessFile.class,"fd").chain("fd");
        Permit<RandomAccessFile,?> ref3 = build(RandomAccessFile.class,"fd.fd");
        Permit tmp = build(RandomAccessFile.class,"O_TEMPORARY");
        
        
        vals[ii++] = ref.getInt(fd);
        vals[ii++] = ref2.getInt(raf);
        vals[ii++] = ref3.getInt(raf);
        vals[ii++] = tmp.getInt(null); // 16
        for (int jj=0; jj < ii; jj++)
            System.out.format("ufd %2d: %4d\n",jj,vals[jj]);

        ClassLoader cl = Safer.class.getClassLoader();
        
        Permit<ClassLoader,String> app = build(cl,"ucp")
                .chain("path")
                .chain(java.util.ArrayList.class,"elementData")
                .chain("")
                .chain(URL.class,"path")
                .target(String.class);
        String path = app.link(0).getObject(cl);
        System.out.println("path: " + path);
        Permit.godMode();



        jdk.internal.jshell.tool.JShellToolBuilder obj = new jdk.internal.jshell.tool.JShellToolBuilder();
        jdk.internal.jshell.tool.JShellTool tool = obj.rawTool();
        if (args.length > 0)
            tool.start(args);

        System.out.println("tool: " + obj);
    }
    
}


import java.io.FileDescriptor;
import java.io.RandomAccessFile;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashSet;
import static org.srlutils.Unsafe.uu;




public abstract class Unreflect<TT,VV> {
    public static final String splitChar = "\\.";

    static Unreflect2<AccessibleObject,Boolean> override = build(AccessibleObject.class,"override");
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

    public static class FieldNotFound extends RuntimeException {
        public FieldNotFound(Exception ex) { super(ex); }
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
    
    public static <TT,VV> Unreflect2<TT,VV> build(Class<TT> klass,String name) {
        String [] names = name.split(splitChar);
        String firstName = names.length==0 ? name : names[0];
        Unreflect2<TT,VV> ref = new Unreflect2(klass,firstName);
        for (int ii=1; ii < names.length; ii++)
            ref.chain(names[ii]);
        return ref;
    }

    public static <TT,VV> Unreflect2<TT,VV> build(TT sample,String name) {
        return build((Class<TT>) sample.getClass(),name);
    }
    
    static class Unreflect2<TT,VV> extends Unreflect<TT,VV> {

    String name;
    Class klass;
    Field field;
    boolean isStatic;
    boolean isArray;
    long offset;
    long scale;
    Unreflect2 chain, last = this, first = null;
    Object base;
    int rowPosition;

    Unreflect2(Class<TT> klass,String name) {
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
                throw new FieldNotFound(ex);
            isStatic = Modifier.isStatic(field.getModifiers());
            if (isStatic) {
                base = uu.staticFieldBase(field);
                offset = uu.staticFieldOffset(field);
            }
            else
                offset = uu.objectFieldOffset(field);
        }
    }
    public Unreflect2<TT,VV> chain(Class klass,String name) {
        return chain(klass,name,false);
    }
    public Unreflect2 chain(String name) {
        return chain(klass(),name,true);
    }
    private Unreflect2<TT,VV> chain(Class klass,String name,boolean safe) {
        Unreflect2 ref = new Unreflect2(klass,name);
        // fixme -- if not safe, check class compatibility
        ref.rowPosition = last.rowPosition + (last.isArray ? 1:0);
        if (ref.isStatic)
            first = ref;
        last.chain = ref;
        last = ref;
        return this;
    }
    public <XX> Unreflect2<TT,XX> target(Class<XX> klass) {
        return (Unreflect2<TT,XX>) this;
    }
    public Class klass() {
        if (isArray) return klass.getComponentType();
        else return last.field.getType();
    }

    public class Link extends Unreflect<TT,VV> {
        int [] rows;
        long offset() {
            return Unreflect2.this.last.addy(rows);
        }
        Object resolve(Object o) {
            return Unreflect2.this.resolve(o,rows);
        }
        public Link(int ... rows) {
            this.rows = rows;
        }
    }
    public Link link(int ... rows) {
        return new Link(rows);
    }
    
    Object resolve(Object o) {
        return resolve(o,new int[0]);
    }    
    Object resolve(Object o,int [] rows) {
        if (first != null)
            return first.resolve(o);
        if (isStatic)
            o = base;
        for (Unreflect2 ref=this; ref.chain != null; ref=ref.chain)
            o = uu.getObject(o,ref.addy(rows));
        return o;
    }
    
    long addy(int [] rows) {
        return isArray
                ? offset+scale*rows[rowPosition]
                : offset;
    }
    
    long offset() { return last.offset; }
    
    }
    
    abstract long offset();
    abstract Object resolve(TT o);
    
    public int getInt(TT o) {
        return uu.getInt(resolve(o),offset());
    }

    public void putInt(TT o,int x) {
        uu.putInt(resolve(o),offset(),x);
    }

    public VV getObject(TT o) {
        return (VV) uu.getObject(resolve(o),offset());
    }

    public void putObject(TT o,VV x) {
        uu.putObject(resolve(o),offset(),x);
    }

    public boolean getBoolean(TT o) {
        return uu.getBoolean(resolve(o),offset());
    }

    public void putBoolean(TT o,boolean x) {
        uu.putBoolean(resolve(o),offset(),x);
    }

    public byte getByte(TT o) {
        return uu.getByte(resolve(o),offset());
    }

    public void putByte(TT o,byte x) {
        uu.putByte(resolve(o),offset(),x);
    }

    public short getShort(TT o) {
        return uu.getShort(resolve(o),offset());
    }

    public void putShort(TT o,short x) {
        uu.putShort(resolve(o),offset(),x);
    }

    public char getChar(TT o) {
        return uu.getChar(resolve(o),offset());
    }

    public void putChar(TT o,char x) {
        uu.putChar(resolve(o),offset(),x);
    }

    public long getLong(TT o) {
        return uu.getLong(resolve(o),offset());
    }

    public void putLong(TT o,long x) {
        uu.putLong(resolve(o),offset(),x);
    }

    public float getFloat(TT o) {
        return uu.getFloat(resolve(o),offset());
    }

    public void putFloat(TT o,float x) {
        uu.putFloat(resolve(o),offset(),x);
    }

    public double getDouble(TT o) {
        return uu.getDouble(resolve(o),offset());
    }

    public void putDouble(TT o,double x) {
        uu.putDouble(resolve(o),offset(),x);
    }

    public void setMemory(TT o,long bytes,byte value) {
        uu.setMemory(resolve(o),offset(),bytes,value);
    }

    public void copyMemory(TT srcBase,long srcOffset,Object destBase,long destOffset,long bytes) {
        uu.copyMemory(resolve(srcBase),srcOffset,destBase,destOffset,bytes);
    }

    public final boolean compareAndSwapObject(TT o,VV expected,VV x) {
        return uu.compareAndSwapObject(resolve(o),offset(),expected,x);
    }

    public final boolean compareAndSwapInt(TT o,int expected,int x) {
        return uu.compareAndSwapInt(resolve(o),offset(),expected,x);
    }

    public final boolean compareAndSwapLong(TT o,long expected,long x) {
        return uu.compareAndSwapLong(resolve(o),offset(),expected,x);
    }

    public VV getObjectVolatile(TT o) {
        return (VV) uu.getObjectVolatile(resolve(o),offset());
    }

    public void putObjectVolatile(TT o,VV x) {
        uu.putObjectVolatile(resolve(o),offset(),x);
    }

    public int getIntVolatile(TT o) {
        return uu.getIntVolatile(resolve(o),offset());
    }

    public void putIntVolatile(TT o,int x) {
        uu.putIntVolatile(resolve(o),offset(),x);
    }

    public boolean getBooleanVolatile(TT o) {
        return uu.getBooleanVolatile(resolve(o),offset());
    }

    public void putBooleanVolatile(TT o,boolean x) {
        uu.putBooleanVolatile(resolve(o),offset(),x);
    }

    public byte getByteVolatile(TT o) {
        return uu.getByteVolatile(resolve(o),offset());
    }

    public void putByteVolatile(TT o,byte x) {
        uu.putByteVolatile(resolve(o),offset(),x);
    }

    public short getShortVolatile(TT o) {
        return uu.getShortVolatile(resolve(o),offset());
    }

    public void putShortVolatile(TT o,short x) {
        uu.putShortVolatile(resolve(o),offset(),x);
    }

    public char getCharVolatile(TT o) {
        return uu.getCharVolatile(resolve(o),offset());
    }

    public void putCharVolatile(TT o,char x) {
        uu.putCharVolatile(resolve(o),offset(),x);
    }

    public long getLongVolatile(TT o) {
        return uu.getLongVolatile(resolve(o),offset());
    }

    public void putLongVolatile(TT o,long x) {
        uu.putLongVolatile(resolve(o),offset(),x);
    }

    public float getFloatVolatile(TT o) {
        return uu.getFloatVolatile(resolve(o),offset());
    }

    public void putFloatVolatile(TT o,float x) {
        uu.putFloatVolatile(resolve(o),offset(),x);
    }

    public double getDoubleVolatile(TT o) {
        return uu.getDoubleVolatile(resolve(o),offset());
    }

    public void putDoubleVolatile(TT o,double x) {
        uu.putDoubleVolatile(resolve(o),offset(),x);
    }

    public void putOrderedObject(TT o,VV x) {
        uu.putOrderedObject(resolve(o),offset(),x);
    }

    public void putOrderedInt(TT o,int x) {
        uu.putOrderedInt(resolve(o),offset(),x);
    }

    public void putOrderedLong(TT o,long x) {
        uu.putOrderedLong(resolve(o),offset(),x);
    }

    public final int getAndAddInt(TT o,int delta) {
        return uu.getAndAddInt(resolve(o),offset(),delta);
    }

    public final long getAndAddLong(TT o,long delta) {
        return uu.getAndAddLong(resolve(o),offset(),delta);
    }

    public final int getAndSetInt(TT o,int newValue) {
        return uu.getAndSetInt(resolve(o),offset(),newValue);
    }

    public final long getAndSetLong(TT o,long newValue) {
        return uu.getAndSetLong(resolve(o),offset(),newValue);
    }

    public final VV getAndSetObject(TT o,VV newValue) {
        return (VV) uu.getAndSetObject(resolve(o),offset(),newValue);
    }

    
    static Object logger(boolean expected) {
        Object obj = null;
        try {
            obj = jdk.internal.module.IllegalAccessLogger.illegalAccessLogger();
        }
        catch (Exception ex) {}
        boolean success = obj != null;
        if (success != expected)
            throw new RuntimeException("mismatch: " + obj);
        return obj;
    }
    
    public static void main(String[] args) throws Exception {
        int [] vals = new int[10];
        int ii = 0;
        RandomAccessFile raf = new RandomAccessFile("/etc/hosts","r");
        FileDescriptor fd = raf.getFD();
        Field field = FileDescriptor.class.getDeclaredField("fd");
        Class ka = AccessibleObject.class;
//        logger(false);
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

        Unreflect2<FileDescriptor,?> ref = build(FileDescriptor.class,"fd");
        Unreflect2<RandomAccessFile,?> ref2 = build(RandomAccessFile.class,"fd").chain("fd");
        Unreflect2<RandomAccessFile,?> ref3 = build(RandomAccessFile.class,"fd.fd");
        Unreflect2 tmp = build(RandomAccessFile.class,"O_TEMPORARY");
        
        
        vals[ii++] = ref.getInt(fd);
        vals[ii++] = ref2.getInt(raf);
        vals[ii++] = ref3.getInt(raf);
        vals[ii++] = tmp.getInt(null); // 16
        for (int jj=0; jj < ii; jj++)
            System.out.format("ufd %2d: %4d\n",jj,vals[jj]);

        ClassLoader cl = Unreflect.class.getClassLoader();
        
        Unreflect2<ClassLoader,String> app = build(cl,"ucp")
                .chain("path")
                .chain(java.util.ArrayList.class,"elementData")
                .chain("")
                .chain(URL.class,"path")
                .target(String.class);
        String path = app.link(0).getObject(cl);
        System.out.println("path: " + path);
        godMode();



        jdk.internal.jshell.tool.JShellToolBuilder obj = new jdk.internal.jshell.tool.JShellToolBuilder();
        jdk.internal.jshell.tool.JShellTool tool = obj.rawTool();
        if (args.length > 0)
            tool.start(args);

        System.out.println("tool: " + obj);
    }

    
}

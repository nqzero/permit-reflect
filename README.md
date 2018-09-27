# Permit Reflection

utilities for working with java 11 modules
* `Permit.setAccessible` enables access for a `Field` or `Method` without regard for modules
* `Permit.godMode` opens all packages in all modules to all modules
* `Permit.main` runs a specified class's main method in god mode.
     this should allow many java 8 programs to run with later versions of java
* `Permit.build` is a fluent api for semi-typesafe access to unsafe


in summary, it bypasses most of the runtime module checks put in place by project jigsaw and the JPMS


see the `demo` and `pshell` directories for some usage examples




## installation

```
        <dependency>
            <groupId>com.nqzero</groupId>
            <artifactId>permit-reflect</artifactId>
            <version>0.2</version>
        </dependency>
```


## examples


using `Permit.main` to run another classes main method in god mode, eg from the demo directory run:
```
JAVA_HOME=$java11 mvn clean package exec:java -Dexec.mainClass=com.nqzero.permit.Permit \
        -Dexec.args="demo.DemoNormal --class-path target/classes"
```


a RAF file descriptor is private, but this works (and `Field.setAccessible` doesn't):
```
        FileDescriptor fd = new RandomAccessFile("/etc/hosts","r").getFD();
        Field field = FileDescriptor.class.getDeclaredField("fd");
        Permit.setAccessible(field);
        field.getInt(fd);
```


a fluent, semi-typesafe api for unsafe:
```
        ClassLoader cl = new Object() {}.getClass().getClassLoader();
        
        Permit<ClassLoader,String> app = build(cl,"ucp")
                .chain("path")
                .chain(java.util.ArrayList.class,"elementData")
                .chain("")
                .chain(URL.class,"path")
                .target(String.class);
        String path = app.link(0).getObject(cl);
```


access the JShell directly
* god mode fixes `Field.setAccessible` at runtime
* compile with java 8 source and target (to bypass compile-time checks)
* full source is in the `pshell` directory
* `JAVA_HOME=$java11 mvn package exec:java -Dexec.mainClass=demo.Pshell`
* wait 4 seconds after jshell starts ... will print "hello world" and foobar will be defined
* these are injected from a separate thread **after** jshell is started
```
        Permit.godMode();

        jdk.internal.jshell.tool.JShellTool tool
                = new jdk.internal.jshell.tool.JShellToolBuilder().rawTool();
        Field field = tool.getClass().getDeclaredField("state");
        field.setAccessible(true);

        // inject some values into the shell
        new Thread(() -> { try {

            Thread.sleep(4000);
            JShell jshell = (JShell) field.get(tool);
            jshell.eval("String foobar = \"hello world\";");
            jshell.eval("System.out.println(foobar);");

        } catch (Exception ex) {} }).start();

        tool.start(new String[0]);        
```


## a word about security

nothing about this library makes java less secure

* when running with a security manager, `Permit` fails
* code running without this library can access your home directory, the network, etc
* this library breaks encapsulation, not security


## the freedom to tinker

part of what makes java free is the ability to use apis
 in a manner not anticipated by the companies that produce them.
project jigsaw undermines this freedom.
help keep the freedom in java by exercising reflection !



## details

* all files in this repo copyright 2018 nqzero, and are offered under the terms of the MIT License
* the build targets java 6, but must be built with at least java 9 (needs `Module.class` and etc)
* should run on java 7, java 8, java 9, java 10, java 11, and (at least early builds of) java 12
* features not available on a given platform, eg god mode on java 8, should degrade gracefully
* using `javac -source 1.8 -target 1.8` bypasses the compile-time checks




## indications

starting with java 9, most reflection is considered "illegal". eg, running `demo.DemoNormal` **without a security manager** you'll get the following warning and exception:

```
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by demo.DemoNormal
         (file:/.../permit-reflect/demo/target/classes/) to field java.io.FileDescriptor.fd
WARNING: Please consider reporting this to the maintainers of demo.DemoNormal
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release

Exception in thread "main" java.lang.IllegalAccessError:
    class demo.DemoNormal (in unnamed module @0xb065c63)
    cannot access class jdk.internal.jshell.tool.JShellToolBuilder (in module jdk.jshell)
    because module jdk.jshell does not export jdk.internal.jshell.tool to unnamed module @0xb065c63
    at demo.DemoNormal.main(DemoNormal.java:27)
```

this was access that was fine with java 8 and earlier

modules can also prevent compiling code with java 11 (or 9 or 10), eg:
```
src/demo/Pshell.java:12: error: package jdk.internal.jshell.tool is not visible
        jdk.internal.jshell.tool.JShellTool tool
                           ^
  (package jdk.internal.jshell.tool is declared in module jdk.jshell, which does not export it)
```

a developer can work around these problems with `--add-exports` or by compiling with java 8.
`permit-reflect` can't help with these compilation issues



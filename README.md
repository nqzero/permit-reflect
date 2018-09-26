# Permit Reflection

utilities for working with java 9+ modules
* `Permit.setAccessible` enables access without regard for modules
* `Permit.godMode` opens all packages in all modules to all modules
* `Permit.build` is a fluent api for semi-typesafe access to unsafe


in summary, it bypasses most of the module checks put in place by project jigsaw and the JPMS


see the `demo` directory for some usage examples



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


## a word about security

nothing about this library makes java less secure

* when running with a security manager, `Permit` fails
* code running without this library can access your home directory etc
* this library breaks encapsulation, not security






## details

all files in this repo copyright 2018 nqzero, and are offered under the terms of the MIT License




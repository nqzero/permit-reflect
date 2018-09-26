# Jigsaw Utils

utilities for working with java 9+ modules
- `Unflect.godMode` opens all packages in all modules to all modules
- `Unflect.build` uses a fluent api to create an unsafe-proxy that's somewhat typesafe
- `Unflect.setAccessible` enables access without regard for modules


in summary, it bypasses most of the module checks put in place by project jigsaw and the JPMS


see the `demo` directory for some usage examples

all files in this repo:
* copyright 2018 nqzero
* MIT License




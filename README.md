Kommon
=====================================================

Modules
-----------------------------------------------------

This library consists of several modules
- [*log*](modules/log/README.md): kotlinish logging style
- [*concurrent*](modules/concurrent/README.md): concurrent primitives for coroutines
- [*state*](modules/concurrent/README.md): not sure, but there is bean jar
- [*delegates*](modules/concurrent/README.md): useful delegates
- [*bom*](modules/bom/README.md): bill of materials project for modules of this library

Notice
-----------------------------------------------------

One important thing to notice. This project is multiplatform and uses Gradle
Metadata. This feature allows you to declare dependencies without specifying
target platform. If this is not working for you, this means that you use an
old version of Gradle.

SET proj=..\
SET rep=..\..\java-advanced-2024
SET lib=%rep%\lib\*
SET test=%rep%\artifacts\info.kgeorgiy.java.advanced.implementor.jar
SET file=%proj%\java-solutions\info\kgeorgiy\ja\vysotin\implementor\Implementor.java
SET dst=%proj%\out\production\JarImplementor
SET man=MANIFEST.MF\
SET dep=info\kgeorgiy\ja\vysotin\implementor\*.class

javac -d . -cp %lib%;%test%; %file%
jar cfm Implementor.jar %man% %dep%

pause
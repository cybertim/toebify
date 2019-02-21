@echo off
REM set JAVA_HOME=C:\Users\VR\scoop\apps\openjdk11\current
call "%JAVA_HOME%\bin\java.exe" -Xmx512M --module-path "%JAVA_HOME%\jmods" --add-opens jdk.jlink/jdk.tools.jlink.internal.packager=jdk.packager -m jdk.packager/jdk.packager.Main create-image --jvm-args "--add-opens javafx.base/com.sun.javafx.reflect=ALL-UNNAMED" --verbose --input ".\shade" --description "toebify" --echo-mode --icon "Toebify.ico" "--output ".\target" --name "Toebify" --main-jar "toebify.jar" --class "com.dskwrk.java.Launcher" --add-modules "java.base,java.datatransfer,java.desktop,java.scripting,java.xml,jdk.jsobject,jdk.unsupported,jdk.unsupported.desktop,jdk.xml.dom,java.naming,java.sql,jdk.charsets"
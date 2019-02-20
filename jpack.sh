#!/usr/bin/env bash
export JAVA_HOME=$(/usr/libexec/java_home)
jpackager create-image --verbose -i ./shade -o ./target -n "Toebify" -j "toebify.jar" -c "com.dskwrk.java.Launcher" --add-modules java.base,java.datatransfer,java.desktop,java.scripting,java.xml,jdk.jsobject,jdk.unsupported,jdk.unsupported.desktop,jdk.xml.dom,java.naming,java.sql,jdk.charsets
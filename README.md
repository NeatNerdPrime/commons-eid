# README for Commons eID Project


## 1. Introduction

This project contains the source code tree of Commons eID.

The source code is hosted at: https://github.com/e-Contract/commons-eid


## 2. Requirements

The following is required for compiling the Commons eID software:

* Oracle Java 1.7.0_72
* Apache Maven 3.3.3
* Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy


## 3. Build

The project can be build via:

```shell
mvn clean install
```


## 4. License

The source code of the Commons eID Project is licensed under GNU LGPL v3.0.
All source code files remain under control of the GNU LGPL v3.0 license 
unless otherwise decided in the future by _ALL_ Commons eID Project 
copyright holders.
The license conditions can be found in the file: LICENSE.txt


## 5. Style Guide

Summary: "Eclipse Default Java Formatting"

Rationale:

Different code formatting wreaks havoc on diffs after commit:
Everything looks changed all the time.
I've added the java-formatter plugin to the main pom for this reason.

http://maven-java-formatter-plugin.googlecode.com/svn/site/0.3.1/plugin-info.html

Usage:

```shell
mvn -e com.googlecode.maven-java-formatter-plugin:maven-java-formatter-plugin:format
```

Using this leaves everyone free to have their own favorite formatting
in e.g. IDE's while still having a common formatting in the repository.
This is a great relief to someone like me to whom code formatted using
the default Eclipse/Sun styles looks like an RFC with modem noise.

If required, we could include a custom config XML at some point..

When using the Eclipse code formatter on default settings, according to
the docs, you should be OK without this plugin, as well. In practice
I've noticed there are small differences, mostly in empty line handling.

utf8validate
============

UTF8 file validator. This small utility will help to check if UTF-8 files are really properly encoded.

## Usage

Find the ready-built tool `Utf8Validate.jar` in bin/ subdirectory.
Run the tool and check for exit code:

```shell
java -jar Utf8Validate.jar < "somefile.txt" > /dev/null
```

Exit codes:
* 0: UTF-8
* 1: UTF-8 with BOM
* 2: Plain ASCII
* 10: Not valid UTF-8

You can find ready-made example script in scripts/ directory.

## Building the tool

This mini-tool is written in Java. You will need
* Java compiler, 
* JUNIT jar library located here: $JUNIT_HOME/junit.jar
* ant 

To build the tool:

```shell
ant clean jar
```

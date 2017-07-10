echo off
cls

set ROOT_PATH=%~dp0
SET BIN_HOME=%ROOT_PATH%\bin
SET LIB_HOME=%ROOT_PATH%\lib
SET CLASSPATH=%BIN_HOME%;%LIB_HOME%\mysql-connector-java-bin.jar;

java server.Main

@pause
@echo off
SET OLDCLASSPATH=%CLASSPATH%
call setenv.bat

REM -------------------------------------
start javaw com.l2jfree.tools.dbinstaller.LauncherLS
REM -------------------------------------

SET CLASSPATH=%OLDCLASSPATH%
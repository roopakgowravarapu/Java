@echo off
title Campus Placement Database Inspector
color 0b
echo ==========================================================
echo Reading live database records from campus_placement.db...
echo ==========================================================

set JARS=C:\Users\roopa\.m2\repository\org\xerial\sqlite-jdbc\3.45.3.0\sqlite-jdbc-3.45.3.0.jar;C:\Users\roopa\.m2\repository\org\slf4j\slf4j-api\2.0.13\slf4j-api-2.0.13.jar;target\classes

java -cp "%JARS%" com.campusplacement.ViewDatabase

echo.
pause

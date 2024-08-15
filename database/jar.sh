#!/bin/bash

mvn package

mvn install:install-file \
   -Dfile=./target/database-alpha.jar \
   -DgroupId=com.stambul \
   -DartifactId=database \
   -Dversion=alpha \
   -Dpackaging=jar \
   -DgeneratePom=true

rm ../arbitrageur/src/database-alpha.jar
rm ../initializer/src/database-alpha.jar
rm ../scanner/src/database-alpha.jar

cp ./target/database-alpha.jar ../arbitrageur/src/
cp ./target/database-alpha.jar ../initializer/src/
cp ./target/database-alpha.jar ../scanner/src/
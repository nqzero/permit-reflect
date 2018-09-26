#! /bin/bash


if [ -n "$MAVEN_HOME" ]; then
    echo "adding MAVEN_HOME to path: $MAVEN_HOME/bin"
    PATH="$MAVEN_HOME/bin:$PATH"
fi

yes "" | head
JAVA_HOME=$java9 mvn clean package
cp=$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/fd/1)
echo "classpath: $cp"


yes "" | head
echo "demo using only java 8 features - 7, 8, 9, 10, 11"
for ii in $java7 $java8 $java9 $java10 $java11; do 
    $ii/bin/java -cp target/classes:$cp demo.Demo8
done

yes "" | head
echo "demo Permit - 9, 10, 11"
for ii in $java9 $java10 $java11; do 
    $ii/bin/java -cp target/classes:$cp demo.DemoPermit
done

yes "" | head
echo "demo normal with godmode"
$java9/bin/java -cp target/classes:$cp com.nqzero.permit.Permit demo.DemoNormal

yes "" | head
echo "demo normal without godmode - should fail"
$java9/bin/java -cp target/classes:$cp demo.DemoNormal

yes "" | head
echo "demo how Permit handles a security manager - should fail gracefully"
$java9/bin/java -Djava.security.manager -cp target/classes:$cp demo.DemoPermit

yes "" | head

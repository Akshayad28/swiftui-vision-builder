java -cp target/Lho-etl-automation-1.0-SNAPSHOT.jar org.testng.TestNG testng.xml
java -cp target/Lho-etl-automation-1.0-SNAPSHOT-shaded.jar org.testng.TestNG testng.xml
jar tf target/Lho-etl-automation-1.0-SNAPSHOT.jar | findstr TestNG

mvn help:evaluate -Dexpression=project.packaging -q -DforceStdout
java -cp "target/lho-etl-automation-1.0-SNAPSHOT.jar;target/test-classes;target/classes;target/dependency/*" org.testng.TestNG testng.xml

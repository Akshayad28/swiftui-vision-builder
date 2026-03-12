<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.barclays.testautomation</groupId>
    <artifactId>lho-etl-automation</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>lho-etl-automation</name>

    <!-- ========================= -->
    <!-- JAVA VERSION -->
    <!-- ========================= -->

    <properties>

        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>

        <cucumber.version>7.15.0</cucumber.version>
        <testng.version>7.9.0</testng.version>
        <selenium.version>4.13.0</selenium.version>
        <poi.version>5.2.5</poi.version>
        <log4j.version>2.20.0</log4j.version>

    </properties>

    <!-- ========================= -->
    <!-- DEPENDENCIES -->
    <!-- ========================= -->

    <dependencies>

        <!-- Cucumber Java -->
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>${cucumber.version}</version>
        </dependency>

        <!-- Cucumber TestNG -->
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-testng</artifactId>
            <version>${cucumber.version}</version>
        </dependency>

        <!-- TestNG -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>${testng.version}</version>
        </dependency>

        <!-- Selenium (optional but typical for automation frameworks) -->
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>${selenium.version}</version>
        </dependency>

        <!-- Apache POI (Excel Handling) -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi</artifactId>
            <version>${poi.version}</version>
        </dependency>

        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>${poi.version}</version>
        </dependency>

        <!-- Log4j2 -->
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-api</artifactId>
            <version>${log4j.version}</version>
        </dependency>

        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>${log4j.version}</version>
        </dependency>

    </dependencies>

    <!-- ========================= -->
    <!-- BUILD -->
    <!-- ========================= -->

    <build>

        <plugins>

            <!-- Java Compiler -->
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>

            <!-- Surefire (for mvn test) -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
                <configuration>
                    <suiteXmlFiles>
                        <suiteXmlFile>testng.xml</suiteXmlFile>
                    </suiteXmlFiles>
                </configuration>
            </plugin>

            <!-- SHADE PLUGIN (Creates Fat Jar) -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.0</version>

                <executions>

                    <execution>

                        <phase>package</phase>

                        <goals>
                            <goal>shade</goal>
                        </goals>

                        <configuration>

                            <createDependencyReducedPom>false</createDependencyReducedPom>

                            <transformers>

                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">

                                    <mainClass>com.barclays.testautomation.runner.RunnerIT_TestNG</mainClass>

                                </transformer>

                            </transformers>

                        </configuration>

                    </execution>

                </executions>

            </plugin>

        </plugins>

    </build>

</project>

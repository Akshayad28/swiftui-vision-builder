<plugin>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>3.6.0</version>

    <configuration>
        <archive>
            <manifest>
                <mainClass>com.barclays.testautomation.runner.RunnerIT_TestNG</mainClass>
            </manifest>
        </archive>

        <descriptors>
            <descriptor>src/main/assembly/assembly.xml</descriptor>
        </descriptors>
    </configuration>

    <executions>
        <execution>
            <id>make-assembly</id>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>

</plugin>

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
                        <mainClass>org.testng.TestNG</mainClass>
                    </transformer>
                </transformers>

            </configuration>
        </execution>
    </executions>
</plugin>

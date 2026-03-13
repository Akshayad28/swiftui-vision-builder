<plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <includes>
                        **/RunCukesByFeatureWeb.java
                    </includes>
                    <!--					<suiteXmlFiles>-->
                    <!--						<suiteXmlFile>testng.xml</suiteXmlFile>-->
                    <!--					</suiteXmlFiles>-->
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <!-- <version>3.7</version> -->
                <configuration>

                    <descriptors><descriptor>src/test/resources/assembly.xml</descriptor></descriptors>

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

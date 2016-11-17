# Builder Pattern Enforcer [![Build Status](https://travis-ci.org/mle-enso/build-pattern-enforcer.svg?branch=master)](https://travis-ci.org/mle-enso/build-pattern-enforcer)

Rule for the [Maven Enforcer Plugin](https://maven.apache.org/enforcer/maven-enforcer-plugin/) to find violations against the [builder pattern](https://en.wikipedia.org/wiki/Builder_pattern) contract in conjunction with [Lombok](https://projectlombok.org/) annotations.


## Usage

Implement the following snippet in your pom.xml

```xml
<project>
	[…]
	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-enforcer-plugin</artifactId>
				<version>1.4.1</version>
				<dependencies>
					<dependency>
						<groupId>de.mle</groupId>
						<artifactId>build-pattern-enforcer</artifactId>
						<version>0.0.1</version>
					</dependency>
				</dependencies>
				<configuration>
					<rules>
						<myCustomRule implementation="de.mle.enforcer.BuilderPatternEnforcer" />
					</rules>
				</configuration>
				<executions>
					<execution>
						<id>enforce-builder-pattern</id>
						<phase>validate</phase>
						<goals>
							<goal>enforce</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
		</build>
	[…]
</project>
```

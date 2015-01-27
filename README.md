# jmeter-spock-sampler
Run Spock specs in JMeter

## Status
JMeter runs hardcoded spec classname.
The tests for this adapter run in IntelliJ, not yet in maven.

### Needs
* GUI
* support for running a single method.
* running setup & cleanup outside of sample timer.
* failure output


## To Use
Run `mvn package` to create the jar file.
Copy `jmeter-spock-sampler-0.1.0.jar` to `$JMETER_HOME/lib/ext`

and the spock jar and groovy-all.jar to `$JMETER_HOME/lib`

copy your jar file of Spock specs to `$JMETER_HOME/lib/unit`
see also http://jmeter.apache.org/usermanual/junitsampler_tutorial.pdf


After packaging this project, the spock & groovy jars may be found at
`~/.m2/repository/org/spockframework/spock-core/0.7-groovy-2.0/spock-core-0.7-groovy-2.0.jar`
`~/.m2/repository/org/codehaus/groovy/groovy-all/2.0.8/groovy-all-2.0.8.jar`
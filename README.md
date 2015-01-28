# jmeter-spock-sampler
Run Spock specs in JMeter

## Status
Spock Sampler runs Spock Specs in JMeter

### Todo
* run setup & cleanup outside of sample timer.
* failure output

## To Use
Run `mvn package` to create the jar file.
Copy `jmeter-spock-sampler-0.1.0.jar` to `$JMETER_HOME/lib/ext`

and the spock jar and groovy-all.jar to `$JMETER_HOME/lib`

copy your jar file of Spock specs to `$JMETER_HOME/lib/junit`
see also http://jmeter.apache.org/usermanual/junitsampler_tutorial.pdf


After running `mvn package` with this project, the spock & groovy jars may be found at
`~/.m2/repository/org/spockframework/spock-core/0.7-groovy-2.0/spock-core-0.7-groovy-2.0.jar`
`~/.m2/repository/org/codehaus/groovy/groovy-all/2.0.8/groovy-all-2.0.8.jar`

### Creating a JMeter test plan
. Add a Thread Group  `Threads (Users)` -> `Thread Group`
. Add a Spock Sampler to the Thread Group  `Sampler` -> `Spock Sampler`
. Select the spec and method to run
. Add a result viewer to the Sampler  `Listener` -> `View Results Tree`

## Credits
Thanks to the authors of the pieces this was assembled from:
* JMeter JUnit Sampler http://jmeter.apache.org/
* https://code.google.com/p/jmeter-groovy-sampler/
* http://arquillian.org/modules/spock-test-runner/

### License
Apache License 2.0